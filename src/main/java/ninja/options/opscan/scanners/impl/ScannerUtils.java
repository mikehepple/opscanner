package ninja.options.opscan.scanners.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ninja.options.opscan.tdameritrade.model.TDAOption;
import ninja.options.opscan.tdameritrade.model.TDAPutCall;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScannerUtils {

    static Map<String, Map<String, List<TDAOption>>> filterByDate(Map<String, Map<String, List<TDAOption>>> options,
                                                                  int minDte, int maxDte) {

        // TDA provides dates as yyyy-MM-dd:123, where 123 is the DTE

        return options.entrySet().stream().filter(e -> {
                    String[] tokens = e.getKey().split(":");
                    if (tokens.length != 2) {
                        throw new IllegalArgumentException(String.format("Unknown key format: %s", e.getKey()));
                    }
                    int dte = Integer.parseInt(tokens[1]);
                    return dte >= minDte && dte <= maxDte;
                }
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    static Stream<TDAOption> fixITMStatus(float stockprice, Stream<TDAOption> contracts) {
        return contracts.map(c -> {
            if (c.getPutCall() == TDAPutCall.CALL && c.getStrikePrice() < stockprice) {
                c.setInTheMoney(true);
            } else if (c.getPutCall() ==TDAPutCall.PUT && c.getStrikePrice() > stockprice) {
                c.setInTheMoney(true);
            } else {
                c.setInTheMoney(false);
            }
            return c;
        });
    }

    static float ensurePositive(float input) {
        return Math.max(input, input*-1);
    }

}
