package ninja.options.opscan.scanners;

import ninja.options.opscan.strategy.Strategy;
import ninja.options.opscan.tdameritrade.model.TDAOption;

import java.util.List;

public record ScanResult(
        Strategy strategy
) {
}
