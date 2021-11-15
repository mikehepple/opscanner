package ninja.options.opscan.cli.scan;

import ninja.options.opscan.cli.OpscanCLI;
import ninja.options.opscan.results.ScanResultTable;
import ninja.options.opscan.results.TableSettings;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.tdameritrade.TDAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import tech.tablesaw.api.Table;

import java.io.PrintStream;

@Component
@CommandLine.Command(name = "scan",
        subcommands = {
                ShortVerticalSpreadCommand.class,
                LongVerticalSpreadCommand.class,
                ShortPutCommand.class,
                ShortCallCommand.class
        }
)
public class ScanCommand {

    @CommandLine.ParentCommand
    OpscanCLI parentCommand;

    enum OutputOptions {
        CONSOLE, CSV;

        void output(Table table, PrintStream printStream) {
            switch (this) {
                case CONSOLE -> printStream.println(table.printAll());
                case CSV -> table.write().csv(printStream);
            }
        }

        void noResults(PrintStream printStream) {
            switch (this) {
                case CONSOLE -> printStream.println("no results");
                case CSV -> {}
            }
        }
    }

    private TDAService tdaService;

    @CommandLine.Parameters(paramLabel = "SYMBOL")
    private String symbol;

    @CommandLine.Option(names = {"--output-format"}, defaultValue = "CONSOLE")
    private OutputOptions outputOption;

    @CommandLine.Option(names = {"-s", "--sort-by"})
    private String sortBy;

    @CommandLine.Option(names = {"-d", "--sort-descending"})
    private boolean sortDescending;


    @Autowired
    public ScanCommand(TDAService tdaService) {
        this.tdaService = tdaService;
    }

    <S extends ScannerSettings> Integer runScan(Scanner<S> scanner, S settings) {
        var optionChain = this.tdaService.getOptionChain(symbol);
        var results = scanner.scan(optionChain, settings);

        if (results == null) {
            throw new NullPointerException("scanner returned results as null");
        }

        if (results.isEmpty()) {
            this.outputOption.noResults(parentCommand.getPrintStream());
            return 99;
        }

        ScanResultTable table = new ScanResultTable(results, tableSettings());

        this.outputOption.output(table.getTable(), parentCommand.getPrintStream());

        return 0;
    }

    private TableSettings tableSettings() {
        return new TableSettings(
                sortBy,
                sortDescending,
                symbol
        );
    }

}
