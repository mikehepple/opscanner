package ninja.options.opscan.scanners.impl;

import lombok.extern.slf4j.Slf4j;
import ninja.options.opscan.scanners.ScanResult;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.strategy.ShortPut;
import ninja.options.opscan.tdameritrade.model.TDAOption;
import ninja.options.opscan.tdameritrade.model.TDAOptionChain;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ninja.options.opscan.scanners.impl.ScannerUtils.*;

@Component
@Slf4j
public class ShortPutScanner implements Scanner<ShortPutScanner.Settings> {

    public static record Settings(int minDte,
                                  int maxDte,
                                  float minRoi,
                                  float minAnnualizedRoi,
                                  float maxDelta,
                                  float minDelta,
                                  boolean allowITM,
                                  float maxStrike) implements ScannerSettings {

    }

    @Override
    public List<ScanResult> scan(TDAOptionChain optionChain, Settings settings) {
        log.info("Scanning for short puts with settings: {}", settings);

        List<ScanResult> results = new ArrayList<>();

        var putMap = filterByDate(optionChain.getPutExpDateMap(),
                settings.minDte, settings.maxDte);

        filterChain((float) optionChain.getUnderlyingPrice(), settings, results, putMap);

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

    private Stream<ShortPut> findMatchingSpreads(float stockprice, List<TDAOption> contracts, Settings settings) {

        return fixITMStatus(stockprice, contracts.stream())
                .filter((c) -> settings.allowITM || !c.isInTheMoney())
                .filter((c) -> settings.maxStrike <= 0f || c.getStrikePrice() < settings.maxStrike)
                .filter((c) -> settings.maxDelta == 0f || (ensurePositive(c.getDelta()) < ensurePositive(settings.maxDelta)))
                .filter((c) -> settings.minDelta == 0f || (ensurePositive(c.getDelta()) > ensurePositive(settings.minDelta)))
                .map((c) -> ShortPut.builder().shortPosition(c).build())
                .filter(ShortPut::valid)
                .filter((s) -> settings.minRoi == 0f || s.roi() > settings.minRoi)
                .filter((s) -> settings.minAnnualizedRoi == 0f || s.annualizedRoi() > settings.minAnnualizedRoi);


    }
}
