package ninja.options.opscan.tdameritrade.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TDAOptionChain {

    private String symbol;
    private String status;
    private TDAOptionStrategy strategy;
    private TDAOptionChainUnderlying underlying;
    private double interval;
    @JsonProperty("isDelayed")
    private boolean delayed;
    @JsonProperty("isIndex")
    private boolean index;
    private double daysToExpiration;
    private double interestRate;
    private double underlyingPrice;
    private double volatility;
    private int numberOfContracts;
    private Map<String, Map<String, List<TDAOption>>> callExpDateMap;
    private Map<String, Map<String, List<TDAOption>>> putExpDateMap;

    public Optional<TDAOption> getOption(LocalDate expiration, TDAPutCall putOrCall, float strikePrice) {
        Map<String, Map<String, List<TDAOption>>> optionMap = putOrCall == TDAPutCall.CALL ? callExpDateMap
                : putExpDateMap;

        String expKey = expiration.atStartOfDay(ZoneId.of("America/New_York"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ":";

        Optional<Map<String, List<TDAOption>>> strikeMap = optionMap.entrySet().stream()
                .filter((e) -> e.getKey().startsWith(expKey)).map(Entry::getValue).findFirst();

        if (strikeMap.isEmpty()) {
            return Optional.empty();
        }

        Optional<List<TDAOption>> optionList = strikeMap.get().entrySet().stream()
                .filter((e) -> Float.parseFloat(e.getKey()) == strikePrice).map(Entry::getValue).findFirst();

        if (optionList.isEmpty() || optionList.get().isEmpty()) {
            return Optional.empty();
        }

        if (optionList.get().size() > 1) {
            throw new RuntimeException("optionList had more than 1 entry: " + optionList);
        }

        return Optional.of(optionList.get().get(0));

    }

}
