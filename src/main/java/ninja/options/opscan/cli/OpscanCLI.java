package ninja.options.opscan.cli;

import lombok.Getter;
import lombok.NonNull;
import ninja.options.opscan.cli.scan.ScanCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.PrintStream;

@Component
@CommandLine.Command(
        name="opscan",
        mixinStandardHelpOptions = true,
        subcommands = {ScanCommand.class}
)
public class OpscanCLI {

    @Getter
    private PrintStream printStream;

    public OpscanCLI() {
        this(System.out);
    }

    public OpscanCLI(@NonNull PrintStream printStream) {
        this.printStream = printStream;
    }
}
