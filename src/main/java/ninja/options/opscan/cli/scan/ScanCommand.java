package ninja.options.opscan.cli.scan;

import ninja.options.opscan.cli.OpscanCLI;
import ninja.options.opscan.results.ScanResultTable;
import ninja.options.opscan.results.TableSettings;
import ninja.options.opscan.scanners.ScanResult;
import ninja.options.opscan.scanners.Scanner;
import ninja.options.opscan.scanners.ScannerSettings;
import ninja.options.opscan.tdameritrade.TDAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.PrintStream;

@Component
@CommandLine.Command(name = "scan",
        subcommands = {
                ShortVerticalSpreadCommand.class,
                LongVerticalSpreadCommand.class,
                ShortPutCommand.class,
                ShortCallCommand.class
        },
        showDefaultValues = true,
        description = "Scan option chains for specific strategies"
)
public class ScanCommand {

    @CommandLine.ParentCommand
    OpscanCLI parentCommand;

    enum OutputOptions {
        LIST, TABLE, CSV;

        void output(ScanResultTable table, PrintStream printStream) {
            switch (this) {
                case LIST -> {
                    printStream.println(" ".repeat(9) + table.getTable().name());
                    printStream.println("");
                    table.getTable().column("description")
                            .first(15)
                            .asList().forEach(printStream::println);
                }
                case TABLE ->
                    printStream.println(table.getTable().removeColumns("description").printAll());

                case CSV -> table.getTable().write().csv(printStream);
            }
        }

        void noResults(String symbol, String strategy, ScannerSettings settings, PrintStream printStream) {
            switch (this) {
                case LIST, TABLE -> {
                    printStream.println(header(symbol, strategy, settings.description()));
                    printStream.println("no results");
                }
                case CSV -> {}
            }
        }
    }

    private TDAService tdaService;

    @CommandLine.Parameters(
            paramLabel = "SYMBOL",
            description = "The stock symbol to scan, e.g. SPY"
    )
    private String symbol;

    @CommandLine.Option(
            names = {"--output-format"},
            defaultValue = "LIST",
            description = "How to format the results. Other options TABLE and CSV."
    )
    private OutputOptions outputOption;

    @CommandLine.Option(
            names = {"-s", "--sort-by"},
            description = "In TABLE or CSV output mode, what column to sort by. Ignored in other modes.",
            defaultValue = ""

    )
    private String sortBy;

    @CommandLine.Option(
            names = {"-d", "--sort-descending"},
            description = "In TABLE or CSV output mode, reverse the sort order. Ignored in other modes.",
            defaultValue = "false"
    )
    private boolean sortDescending;

    @Autowired
    public ScanCommand(TDAService tdaService) {
        this.tdaService = tdaService;
    }

    // Needed for doc generation
    private ScanCommand() {
    }

    <S extends ScannerSettings> Integer runScan(Scanner<S> scanner, S settings) {
        var optionChain = this.tdaService.getOptionChain(symbol);
        var results = scanner.scan(optionChain, settings);

        if (results == null) {
            throw new NullPointerException("scanner returned results as null");
        }

        if (results.isEmpty()) {
            this.outputOption.noResults(symbol, scanner.name(), settings, parentCommand.getPrintStream());
            return 99;
        }

        ScanResultTable table = new ScanResultTable(results, tableSettings(results.get(0), scanner, settings));

        this.outputOption.output(table, parentCommand.getPrintStream());

        return 0;
    }

    private <S extends ScannerSettings> TableSettings tableSettings(ScanResult result, Scanner<S> scanner, S settings) {
        return new TableSettings(
                sortBy,
                sortDescending,
                header(symbol, scanner.name(), settings.description())
        );
    }

    static String header(String symbol, String name, String description) {
        return String.format("$%s (%s) - %s", symbol, name, description);
    }

}
