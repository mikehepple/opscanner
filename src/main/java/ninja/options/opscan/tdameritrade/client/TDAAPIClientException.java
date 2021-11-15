package ninja.options.opscan.tdameritrade.client;

import lombok.Getter;
import lombok.ToString;
import okhttp3.Response;

@ToString(exclude = "response")
public class TDAAPIClientException extends RuntimeException {

    @Getter
    private Response response;
    @Getter
    private String body;

    public TDAAPIClientException(String message, String body, Response response) {
        super(message);
        this.body = body;
        this.response = response;
    }

}
