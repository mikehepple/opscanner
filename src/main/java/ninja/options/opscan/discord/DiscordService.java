package ninja.options.opscan.discord;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import ninja.options.opscan.cli.OpscanCLI;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import picocli.CommandLine;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Profile("discord")
@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class DiscordService extends ListenerAdapter {

    private CommandLine.IFactory iFactory;

    @PostConstruct
    void setup() throws LoginException {
        JDA jda = JDABuilder.createLight("OTA5NTgzNTY2MjYwODE3OTMw.YZGZtQ.749YABi6EAodaQ4G2ZgYs771ymg")
                .addEventListeners(this)
                .setActivity(Activity.playing("Type /opscan"))
                .build();

        CommandData commandData = new CommandData("opscan", "Run OpScan");

        SubcommandData scanData = new SubcommandData("scan", "Run a scan");
        scanData.addOption(OptionType.STRING, "args", "args", true);

        commandData.addSubcommands(scanData);

        jda.upsertCommand(commandData).queue();

        log.info("Discord connected");

    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {

        log.info("Received command {} from {} on {}/{}",
                event.getName(),
                event.getUser().getAsTag(),
                event.getGuild() == null ? "" : event.getGuild().getName(),
                event.getChannel().getName()
        );

        var args = Optional.ofNullable(event.getOption("args"))
                .map(OptionMapping::getAsString).orElse("");

        runCommand(event.getSubcommandName() + " " + args, event).queue();

    }

    protected ReplyAction runCommand(String args, Interaction interaction) {

        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        try (PrintStream stdoutStream = new PrintStream(stdout, true, StandardCharsets.UTF_8);
             PrintWriter stderrWriter = new PrintWriter(stderr, true, StandardCharsets.UTF_8)
        ) {
            var cli = new CommandLine(new OpscanCLI(stdoutStream), iFactory);

            cli.setErr(stderrWriter);

            cli.execute(args.split(" "));

            String err = stderr.toString(StandardCharsets.UTF_8);
            if (err.length() > 0) {
                return interaction.reply(String.format("```%s```", err)).setEphemeral(true);
            }

            String data = stdout.toString(StandardCharsets.UTF_8);
            if ("".equals(data)) {
                data = "error";
            }
            if (data.length() > 2000) {
                data = data.substring(0, 1990);
            }

            data = String.format("```%s```", data);

            return interaction.reply(data);
        }
    }
}
