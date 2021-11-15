package ninja.options.opscan.cli;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import picocli.CommandLine;

@SpringBootApplication(scanBasePackages = "ninja.options.opscan")
@AllArgsConstructor(onConstructor = @__(@Autowired))
@PropertySource(value = { "classpath:secrets.properties",
        "file:/run/secrets/optrack-secrets.properties" }, ignoreResourceNotFound = true)
@Profile("cli")
public class Main implements CommandLineRunner {

    private CommandLine.IFactory iFactory;

    @Autowired
    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "cli");
        SpringApplication.run(Main.class, args).close();
    }

    @Override
    public void run(String... args) throws Exception {

        int exitCode = new CommandLine(new OpscanCLI(), iFactory).execute(args);

        System.exit(Math.max(SpringApplication.exit(context), exitCode));


    }
}
