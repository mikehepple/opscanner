package ninja.options.opscan.cli.scan;

import lombok.RequiredArgsConstructor;
import ninja.options.opscan.scanners.Directionality;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.scanners.impl.ShortVerticalScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@CommandLine.Command(
        name = "short-vertical",
        aliases = {"sv", "credit-spread", "cs"},
        showDefaultValues = true
)
public class ShortVerticalSpreadCommand extends AbstractScannerCommand {

    private final ShortVerticalScanner shortVerticalScanner;

    @CommandLine.Option(names = {"-m", "--min-dte"}, defaultValue = "45")
    private int minDte;
    @CommandLine.Option(names = {"-M", "--max-dte"}, defaultValue = "70")
    private int maxDte;
    @CommandLine.Option(names = {"-w", "--max-width"}, defaultValue = "15")
    private float maxWidth;
    @CommandLine.Option(names = {"-r", "--min-ratio"}, defaultValue = "0")
    private float minPremiumWidthRatio;
    @CommandLine.Option(names = {"-d", "--max-short-delta"}, defaultValue = "0.4")
    private float maxShortDelta;
    @CommandLine.Option(names = {"-b", "--directionality", "--bias"}, defaultValue = "NONE")
    private Directionality directionality;

    @Override
    ScannerSettings settings() {
        return new ShortVerticalScanner.Settings(
                minDte,
                maxDte,
                maxWidth,
                minPremiumWidthRatio,
                maxShortDelta,
                directionality);
    }

    @Override
    Scanner scanner() {
        return shortVerticalScanner;
    }
}
