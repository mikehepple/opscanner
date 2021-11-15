package ninja.options.opscan.tdameritrade.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import ninja.options.opscan.tdameritrade.client.TDAClient;
import ninja.options.opscan.tdameritrade.model.TDAOptionChain;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TDAOptionsChainsAPI {

    private final TDAClient client;

    @SneakyThrows(UnsupportedEncodingException.class)
    public TDAOptionChain getOptionsChain(String symbol) {

        Request req = new Request.Builder()
                .url(String.format("https://api.tdameritrade.com/v1/marketdata/chains?includeQuotes=TRUE&symbol=%s",
                        URLEncoder.encode(symbol, StandardCharsets.UTF_8.toString())))
                .build();

        return client.execute(req, TDAOptionChain.class);

    }


}
