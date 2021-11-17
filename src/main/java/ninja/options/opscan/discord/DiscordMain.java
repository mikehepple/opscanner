package ninja.options.opscan.discord;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages = "ninja.options.opscan")
@AllArgsConstructor(onConstructor = @__(@Autowired))
@PropertySource(value = {"classpath:secrets.properties",
        "file:/etc/opscan-secrets.properties",
        "file:/run/secrets/opscan-secrets.properties",
        "file:/secrets/secrets.properties"}, ignoreResourceNotFound = true)
public class DiscordMain implements CommandLineRunner {


    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "discord");
        SpringApplication.run(DiscordMain.class, args);
    }

    @Override
    public void run(String... args) {

    }
}
