package ninja.options.opscan.scanners.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import ninja.options.opscan.scanners.Directionality;
import ninja.options.opscan.scanners.ScanResult;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.strategy.LongVerticalSpread;
import ninja.options.opscan.tdameritrade.model.TDAOption;
import ninja.options.opscan.tdameritrade.model.TDAOptionChain;
import ninja.options.opscan.tdameritrade.model.TDAPutCall;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ninja.options.opscan.scanners.impl.ScannerUtils.filterByDate;
import static ninja.options.opscan.scanners.impl.ScannerUtils.fixITMStatus;

@Component
@Slf4j
public class LongVerticalScanner implements Scanner<LongVerticalScanner.Settings> {

    public static record Settings(int minDte,
                                  int maxDte,
                                  float maxWidth,
                                  float maxPremiumWidthRatio,
                                  float maxShortDelta,
                                  float maxLongDelta,
                                  int maxStrikesFromATM,
                                  Directionality directionality) implements ScannerSettings {

        @Override
        public String description() {
            List<String> tokens = new ArrayList<>();

            if (minDte != 0 && maxDte != 0) {
                tokens.add(String.format("DTE: over %d, under %d", minDte, maxDte));
            } else if (minDte != 0) {
                tokens.add(String.format("DTE: over %d", minDte));
            } else if (maxDte != 0){
                tokens.add(String.format("DTE: under %d", maxDte));
            }

            if (maxWidth != 0) {
                tokens.add(String.format("Max Width: $%.2f", maxWidth));
            }

            if (maxPremiumWidthRatio != 0) {
                tokens.add(String.format("Max Ratio: %.2f", maxPremiumWidthRatio));
            }

            if (maxShortDelta != 0) {
                tokens.add(String.format("Short leg delta: under %.2f", maxShortDelta));
            }

            if (maxLongDelta != 0) {
                tokens.add(String.format("Long leg delta: under %.2f", maxLongDelta));
            }

            if (maxStrikesFromATM != 0) {
                tokens.add(String.format("Max strikes from ATM: %d", maxStrikesFromATM));
            }

            switch (directionality) {
                case BEARISH -> tokens.add("Puts only");
                case BULLISH -> tokens.add("Calls only");
            }

            return String.join(", ", tokens);

        }
    }

    @Override
    public String name() {
        return "Debit Spread";
    }

    @Override
    public List<ScanResult> scan(TDAOptionChain optionChain, Settings settings) {

        log.info("Scanning for long vertical spreads with settings: {}", settings);

        List<ScanResult> results = new ArrayList<>();

        var callMap = filterByDate(optionChain.getCallExpDateMap(),
                settings.minDte, settings.maxDte);
        var putMap = filterByDate(optionChain.getPutExpDateMap(),
                settings.minDte, settings.maxDte);

        if (settings.directionality == Directionality.NONE || settings.directionality == Directionality.BULLISH) {
            log.info("checking for call spreads..");
            filterChain((float) optionChain.getUnderlyingPrice(), settings, results, callMap);
        }

        if (settings.directionality == Directionality.NONE || settings.directionality == Directionality.BEARISH) {
            log.info("checking for put spreads..");
            filterChain((float) optionChain.getUnderlyingPrice(),settings, results, putMap);
        }

        return results;
    }

    private void filterChain(float stockPrice, Settings settings, List<ScanResult> results,
                             Map<String, Map<String, List<TDAOption>>> optionMap) {

        optionMap.forEach((d, strikes) -> {
            findMatchingSpreads(
                    stockPrice,
                    strikes.values().stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList()),
                    settings
            )
                    .map(ScanResult::new)
                    .forEach(results::add);
        });

    }

    private Stream<LongVerticalSpread> findMatchingSpreads(float stockprice, List<TDAOption> contracts, Settings settings) {

        // Fix ITM booleans
        contracts = fixITMStatus(stockprice, contracts.stream()).toList();

        final List<TDAOption> originalContracts = Collections.unmodifiableList(contracts);
        if (settings.maxStrikesFromATM > 0) {
            contracts = contracts.stream()
                    .filter(c -> strikesFromATM(c, originalContracts) <= settings.maxStrikesFromATM)
                    .toList();
        }

        final List<TDAOption> filteredContracts = contracts;

        return filteredContracts.stream()
                .flatMap((c1) -> filteredContracts.stream()
                                .map((c2) -> LongVerticalSpread.builder()
                                        .longPosition(c1)
                                        .shortPosition(c2)
                                        .build()
                                )
                                .filter(LongVerticalSpread::valid)
                        .filter(s -> settings.maxWidth == 0 || s.width() <= settings.maxWidth)
                        .filter(s -> settings.maxPremiumWidthRatio == 0 || s.premiumWidthRatio() <= settings.maxPremiumWidthRatio)
                        .filter(s -> settings.maxLongDelta == 0 || s.getLongPosition().getDelta() <= settings.maxLongDelta)
                        .filter(s -> settings.maxShortDelta == 0 || s.getShortPosition().getDelta() <= settings.maxShortDelta)
                );


    }

    /*
    Calculate how many strikes (inclusive) this contract is from the ATM price, e.g. the closest
    to the money will have a value of 1.
     */
    @VisibleForTesting
    static int strikesFromATM(TDAOption contract, List<TDAOption> contracts) {

        if (!contracts.contains(contract)) {
            throw new IllegalArgumentException("contract not found in list of contracts");
        }

        var ascending = (contract.getPutCall() == TDAPutCall.CALL && contract.isInTheMoney()) ||
                (contract.getPutCall() == TDAPutCall.PUT && !contract.isInTheMoney());

        var sortedContracts = contracts.stream()
                .filter(c -> c.isInTheMoney() == contract.isInTheMoney())
                .map(TDAOption::getStrikePrice)
                .collect(Collectors.toSet()) // remove dupes
                .stream().sorted().toList();

        if (ascending) {
            sortedContracts = Lists.reverse(sortedContracts);
        }

        return sortedContracts.indexOf(contract.getStrikePrice()) + 1;
    }
}
