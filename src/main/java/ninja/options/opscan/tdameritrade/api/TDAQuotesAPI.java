package ninja.options.opscan.tdameritrade.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import ninja.options.opscan.tdameritrade.client.TDAClient;
import ninja.options.opscan.tdameritrade.model.TDAQuote;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TDAQuotesAPI {

    private final TDAClient client;

    @SneakyThrows(UnsupportedEncodingException.class)
    public TDAQuote getQuote(String symbol) {

        Request req = new Request.Builder()
                .url(String.format("https://api.tdameritrade.com/v1/marketdata/%s/quotes",
                        URLEncoder.encode(symbol, StandardCharsets.UTF_8.toString())))
                .build();



        Map<String, TDAQuote> resp = client.execute(req, new TypeReference< HashMap<String, TDAQuote> >(){});

        return resp.get(symbol);

    }

}
