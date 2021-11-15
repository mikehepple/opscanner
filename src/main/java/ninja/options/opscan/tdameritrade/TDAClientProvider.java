package ninja.options.opscan.tdameritrade;

import ninja.options.opscan.tdameritrade.client.TDAClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
public class TDAClientProvider {

    @Bean
    @Profile("!test")
    public TDAClient createApiClient(@Value("${tda.client_id}") String clientId,
                                     @Value("${tda.refresh_token}") String refreshToken) {
        TDAClient client = new TDAClient(refreshToken, clientId);

        client.authenticate();

        return client;

    }

}
