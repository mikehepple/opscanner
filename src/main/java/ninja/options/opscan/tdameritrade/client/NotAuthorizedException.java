package ninja.options.opscan.tdameritrade.client;

import okhttp3.Response;

public class NotAuthorizedException extends TDAAPIClientException {
    public NotAuthorizedException(String message, String body, Response response) {
        super(message, body, response);
    }
}
