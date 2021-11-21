package ninja.options.opscan.scanners.impl;

import lombok.extern.slf4j.Slf4j;
import ninja.options.opscan.scanners.ScanResult;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.strategy.ShortPut;
import ninja.options.opscan.tdameritrade.model.TDAOption;
import ninja.options.opscan.tdameritrade.model.TDAOptionChain;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
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
        @Override
        public String description() {
            List<String> tokens = new ArrayList<>();

            if (maxStrike != 0) {
                tokens.add(String.format("Over $%.2f", maxStrike));
            }

            if (minDte != 0 && maxDte != 0) {
                tokens.add(String.format("DTE: over %d, under %d", minDte, maxDte));
            } else if (minDte != 0) {
                tokens.add(String.format("DTE: over %d", minDte));
            } else if (maxDte != 0) {
                tokens.add(String.format("DTE: under %d", maxDte));
            }

            if (minRoi != 0) {
                tokens.add(String.format("ROI: over %s%%", NumberFormat.getPercentInstance().format(minRoi)));
            }

            if (minAnnualizedRoi != 0) {
                tokens.add(String.format("Annualized ROI: over %f%%",
                        NumberFormat.getPercentInstance().format(minAnnualizedRoi)));
            }

            if (minDelta != 0 && maxDelta != 0) {
                tokens.add(String.format("Delta: over %.2f, under %.2f", minDelta, maxDelta));
            } else if (minDelta != 0) {
                tokens.add(String.format("Delta: over %.2f", minDelta));
            } else if (maxDelta != 0) {
                tokens.add(String.format("Delta: under %.2f", maxDelta));
            }

            if (allowITM) {
                tokens.add("Include ITM options");
            }

            return String.join(", ", tokens);
        }
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

    @Override
    public String name() {
        return "Short Put";
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
