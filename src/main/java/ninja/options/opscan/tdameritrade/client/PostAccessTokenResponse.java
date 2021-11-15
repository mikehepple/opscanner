package ninja.options.opscan.tdameritrade.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class PostAccessTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("token_type")
    private String tokenType;
}
