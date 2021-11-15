package ninja.options.opscan.tdameritrade.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import okhttp3.Request.Builder;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

@RequiredArgsConstructor
public class TDAClient {

    @NonNull
    private final String refreshToken;
    @NonNull
    private final String clientId;

    private ReentrantReadWriteLock accessTokenLock = new ReentrantReadWriteLock();
    private String accessToken;
    private ScheduledExecutorService refreshTokenService = Executors.newSingleThreadScheduledExecutor();

    private ThreadLocal<OkHttpClient> client = ThreadLocal.withInitial(() -> {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(Level.HEADERS);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        return client;
    });
    private ThreadLocal<ObjectMapper> mapper = ThreadLocal.withInitial(ObjectMapper::new);

    public void authenticate() {

        doAuthenticate();

        refreshTokenService.schedule(this::doAuthenticate, 1200, TimeUnit.SECONDS);

    }

    private void doAuthenticate() {

        WriteLock wl = this.accessTokenLock.writeLock();
        wl.lock();
        try {

            this.accessToken = null;

            String bodyStr;
            try {
                bodyStr = String.format(
                        "grant_type=refresh_token&" + "refresh_token=%s&" + "client_id=%s%%40AMER.OAUTHAP&"
                                + "redirect_uri=",

                        URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(clientId, StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            RequestBody body = RequestBody.create(bodyStr, MediaType.get("application/x-www-form-urlencoded"));
            Request request = new Builder().url("https://api.tdameritrade.com/v1/oauth2/token").post(body)
                    .build();

            PostAccessTokenResponse respBody = (PostAccessTokenResponse) this.executeRaw(request, PostAccessTokenResponse.class);

            this.accessToken = respBody.getAccessToken();
        } finally {
            wl.unlock();
        }
    }

    public <T> T execute(Request request, TypeReference<T> responseBody) {
        return (T) executeInternal(request, responseBody);
    }

    public <T> T execute(Request request, Class<T> responseBody) {
        return (T) executeInternal(request, responseBody);
    }

    protected Object executeInternal(Request request, Object responseBody) {
        ReadLock rl = this.accessTokenLock.readLock();
        rl.lock();

        try {

            if (StringUtils.isEmpty(this.accessToken)) {
                rl.unlock();
                this.doAuthenticate();
                rl.lock();
            }

            request = request.newBuilder().header("Authorization", String.format("Bearer %s", this.accessToken))
                    .build();
        } finally {
            rl.unlock();
        }


        try {
            return executeRaw(request, responseBody);
        } catch (NotAuthorizedException e) {
            this.doAuthenticate();

            try {
                rl.lock();
                request = request.newBuilder().header("Authorization", String.format("Bearer %s", this.accessToken))
                        .build();
                return executeRaw(request, responseBody);
            } finally {
                rl.unlock();
            }
        }
    }

    protected Object executeRaw(Request request, Object responseBody) {
        request = request.newBuilder().header("User-Agent", "OpTrack, mike@hepple.dev").build();

        try (Response response = client.get().newCall(request).execute()) {
            if (!response.isSuccessful()) {

                if (response.code() == 401) {
                    throw new NotAuthorizedException(String.format("Not Authorized: %s %s", request.method(), request.url().toString()),
                            response.body().string(), response);
                }

                throw new TDAAPIClientException(
                        String.format("API error: %s %s", request.method(), request.url().toString()),
                        response.body().string(), response);
            }

            if (responseBody instanceof Class) {
                return mapper.get().readValue(response.body().byteStream(), (Class<?>) responseBody);
            } else if (responseBody instanceof TypeReference) {
                return mapper.get().readValue(response.body().byteStream(), (TypeReference<?>) responseBody);
            } else {
                throw new IllegalArgumentException("unprocessable type: " + responseBody.getClass());
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
