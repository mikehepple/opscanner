package ninja.options.opscan.cli.scan;

import lombok.RequiredArgsConstructor;
import ninja.options.opscan.scanners.Directionality;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.scanners.impl.LongVerticalScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@CommandLine.Command(
        name = "long-vertical",
        aliases = {"lv", "debit-spread", "ds"},
        showDefaultValues = true,
        description = "Search for a long vertical / debit spread"
)
public class LongVerticalSpreadCommand extends AbstractScannerCommand {

    private final LongVerticalScanner longVerticalScanner;

    @CommandLine.Option(names = {"-m", "--min-dte"}, defaultValue = "30")
    private int minDte;
    @CommandLine.Option(names = {"-M", "--max-dte"}, defaultValue = "60")
    private int maxDte;
    @CommandLine.Option(names = {"-w", "--max-width"}, defaultValue = "15")
    private float maxWidth;
    @CommandLine.Option(names = {"-r", "--max-ratio"}, defaultValue = "0")
    private float maxPremiumWidthRatio;
    @CommandLine.Option(names = {"-sd", "--max-short-delta"})
    private float maxShortDelta;
    @CommandLine.Option(names = {"-ld", "--max-long-delta"})
    private float maxLongDelta;
    @CommandLine.Option(names = {"-s", "--max-strikes-from-atm"}, defaultValue = "1")
    private int maxStrikesFromAtm;
    @CommandLine.Option(names = {"-b", "--directionality", "--bias"}, defaultValue = "NONE")
    private Directionality directionality;

    private LongVerticalSpreadCommand() {
        this(null);
    }

    @Override
    ScannerSettings settings() {
        return new LongVerticalScanner.Settings(
                minDte,
                maxDte,
                maxWidth,
                maxPremiumWidthRatio,
                maxShortDelta,
                maxLongDelta,
                maxStrikesFromAtm,
                directionality
        );
    }

    @Override
    Scanner scanner() {
        return longVerticalScanner;
    }
}
