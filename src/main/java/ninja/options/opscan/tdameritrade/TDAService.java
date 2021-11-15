package ninja.options.opscan.tdameritrade;

import lombok.AllArgsConstructor;
import ninja.options.opscan.tdameritrade.api.TDAOptionsChainsAPI;
import ninja.options.opscan.tdameritrade.api.TDAQuotesAPI;
import ninja.options.opscan.tdameritrade.model.TDAOptionChain;
import ninja.options.opscan.tdameritrade.model.TDAOptionChainUnderlying;
import ninja.options.opscan.tdameritrade.model.TDAQuote;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TDAService {

    private final TDAOptionsChainsAPI optionChainsApi;
    private final TDAQuotesAPI quotesAPI;

    @Cacheable("optionChains")
    public TDAOptionChain getOptionChain(String symbol) {
        TDAOptionChain chain = optionChainsApi.getOptionsChain(symbol);
        if (chain.getStatus().equals("FAILED")) {
            // This isn't an optionable security
            // TODO: Fix this temp workaround

            TDAQuote quote = this.quotesAPI.getQuote(symbol);
            TDAOptionChainUnderlying underlying = new TDAOptionChainUnderlying();
            BeanUtils.copyProperties(quote, underlying);
            chain.setUnderlying(underlying);

        }

        return chain;
    }

}
