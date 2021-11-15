package ninja.options.opscan.cli.scan;

import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import picocli.CommandLine;

import java.util.concurrent.Callable;

public abstract class AbstractScannerCommand<S extends ScannerSettings> implements Callable<Integer> {

    @CommandLine.ParentCommand
    ScanCommand scanCommand;

    abstract S settings();

    abstract Scanner<S> scanner();

    @Override
    public Integer call() throws Exception {
        return scanCommand.runScan(scanner(), settings());
    }

}
