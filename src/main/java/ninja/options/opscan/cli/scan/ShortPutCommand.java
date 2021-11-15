package ninja.options.opscan.cli.scan;

import lombok.RequiredArgsConstructor;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.scanners.vertical.ShortPutScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@CommandLine.Command(name = "short-put", aliases = {"sp", "csp", "naked-put", "np"})
public class ShortPutCommand extends AbstractScannerCommand {

    private final ShortPutScanner shortPutScanner;

    @CommandLine.Option(names = {"-m", "--min-dte"}, defaultValue = "45")
    private int minDte;
    @CommandLine.Option(names = {"-M", "--max-dte"}, defaultValue = "60")
    private int maxDte;
    @CommandLine.Option(names = {"-r", "--min-roi"}, defaultValue = "0")
    private int minRoi;
    @CommandLine.Option(names = {"-a", "--min-annualized-roi"}, defaultValue = "0")
    private int minAnnualizedRoi;
    @CommandLine.Option(names = {"-D", "--max-delta"}, defaultValue = "0")
    private float maxDelta;
    @CommandLine.Option(names = {"-d", "--min-delta"}, defaultValue = "0")
    private float minDelta;
    @CommandLine.Option(names = {"-i", "--allow-in-the-money"}, defaultValue = "false")
    private boolean allowITM;

    @Override
    ScannerSettings settings() {
        return new ShortPutScanner.Settings(
                minDte,
                maxDte,
                minRoi / 100f,
                minAnnualizedRoi / 100f,
                maxDelta,
                minDelta,
                allowITM
        );
    }

    @Override
    Scanner scanner() {
        return shortPutScanner;
    }
}
