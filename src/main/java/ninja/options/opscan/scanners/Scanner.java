package ninja.options.opscan.scanners;

import ninja.options.opscan.tdameritrade.model.TDAOptionChain;

import java.util.List;

public interface Scanner<S extends ScannerSettings> {

    List<ScanResult> scan(TDAOptionChain optionChain, S settings);
    String name();

}
