package ninja.options.opscan.scanners.impl;

import lombok.extern.slf4j.Slf4j;
import ninja.options.opscan.scanners.Directionality;
import ninja.options.opscan.scanners.ScanResult;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.strategy.ShortVerticalSpread;
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
public class ShortVerticalScanner implements Scanner<ShortVerticalScanner.Settings> {

    public static record Settings(int minDte,
                                  int maxDte,
                                  float maxWidth,
                                  float minPremiumWidthRatio,
                                  float maxShortDelta,
                                  Directionality directionality) implements ScannerSettings {

    }

    @Override
    public List<ScanResult> scan(TDAOptionChain optionChain, ShortVerticalScanner.Settings settings) {

        log.info("Scanning for short vertical spreads with settings: {}", settings);

        List<ScanResult> results = new ArrayList<>();

        var callMap = filterByDate(optionChain.getCallExpDateMap(), settings.minDte, settings.maxDte);
        var putMap = filterByDate(optionChain.getPutExpDateMap(), settings.minDte, settings.maxDte);

        if (settings.directionality == null || settings.directionality == Directionality.BEARISH) {
            log.info("checking for call spreads..");
            filterChain(settings, results, callMap);
        }

        if (settings.directionality == null || settings.directionality == Directionality.BULLISH) {
            log.info("checking for put spreads..");
            filterChain(settings, results, putMap);
        }

        return results;
    }

    private void filterChain(Settings settings, List<ScanResult> results, Map<String, Map<String, List<TDAOption>>> optionMap) {
        optionMap.forEach((d, strikes) -> {
            findMatchingSpreads(
                    strikes.values().stream()
                            .flatMap(List::stream)
                            .filter(o -> ! o.isInTheMoney())
                            .collect(Collectors.toList()),
                    settings
            )
                    .map(ScanResult::new)
                    .forEach(results::add);
        });
    }

    private Stream<ShortVerticalSpread> findMatchingSpreads(List<TDAOption> contracts, Settings settings) {

        log.info("looking for vertical spreads across {} OTM contracts", contracts.size());

        return contracts.stream()
                .flatMap((c1) -> contracts.stream()
                                .map((c2) -> ShortVerticalSpread.builder()
                                        .longPosition(c1)
                                        .shortPosition(c2)
                                        .build()
                                )
                                .filter(ShortVerticalSpread::valid)
                                .filter(s -> settings.minPremiumWidthRatio == 0f || s.premiumWidthRatio() > settings.minPremiumWidthRatio)
                                .filter(s -> settings.maxWidth == 0f || s.width() <= settings.maxWidth)
                                .filter(s -> settings.maxShortDelta == 0f || s.getShortPosition().getDelta() <= settings.maxShortDelta)
                                .filter(s -> settings.maxShortDelta == 0f || s.getShortPosition().getDelta() >= -1*settings.maxShortDelta)
                );

    }


}

