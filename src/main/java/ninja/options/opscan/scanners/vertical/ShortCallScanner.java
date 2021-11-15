package ninja.options.opscan.scanners.vertical;

import lombok.extern.slf4j.Slf4j;
import ninja.options.opscan.scanners.ScanResult;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.strategy.ShortCall;
import ninja.options.opscan.tdameritrade.model.TDAOption;
import ninja.options.opscan.tdameritrade.model.TDAOptionChain;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ninja.options.opscan.scanners.vertical.ScannerUtils.*;

@Component
@Slf4j
public class ShortCallScanner implements Scanner<ShortCallScanner.Settings> {

    public static record Settings(int minDte,
                                  int maxDte,
                                  float minRoi,
                                  float minAnnualizedRoi,
                                  float maxDelta,
                                  float minDelta,
                                  float costBasis,
                                  boolean allowITM) implements ScannerSettings {

    }

    @Override
    public List<ScanResult> scan(TDAOptionChain optionChain, Settings settings) {
        log.info("Scanning for short calls with settings: {}", settings);

        List<ScanResult> results = new ArrayList<>();

        var callMap = filterByDate(optionChain.getCallExpDateMap(),
                settings.minDte, settings.maxDte);

        filterChain((float) optionChain.getUnderlyingPrice(), settings, results, callMap);

        return results;
    }

    private void filterChain(float stockprice, Settings settings, List<ScanResult> results,
                             Map<String, Map<String, List<TDAOption>>> optionMap) {

        optionMap.forEach((d, strikes) -> {
            findMatchingSpreads(
                    stockprice,
                    strikes.values().stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList()),
                    settings
            )
                    .map(ScanResult::new)
                    .forEach(results::add);
        });

    }

    private Stream<ShortCall> findMatchingSpreads(float stockprice, List<TDAOption> contracts, Settings settings) {

        float costBasis = settings.costBasis == 0f ? stockprice : settings.costBasis;

        return fixITMStatus(stockprice, contracts.stream())
                .filter((c) -> settings.allowITM || !c.isInTheMoney())
                .filter((c) -> settings.maxDelta == 0f || (ensurePositive(c.getDelta()) < ensurePositive(settings.maxDelta)))
                .filter((c) -> settings.minDelta == 0f || (ensurePositive(c.getDelta()) > ensurePositive(settings.minDelta)))
                .map((c) -> ShortCall.builder().shortPosition(c).costBasis(costBasis).build())
                .filter(ShortCall::valid)
                .filter((s) -> settings.minRoi == 0f || s.roi() > settings.minRoi)
                .filter((s) -> settings.minAnnualizedRoi == 0f || s.annualizedRoi() > settings.minAnnualizedRoi);


    }
}
