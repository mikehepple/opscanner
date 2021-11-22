package ninja.options.opscan.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import ninja.options.opscan.cli.OpscanCLI;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class DiscordService extends ListenerAdapter {


    private String discordToken;
    private CommandLine.IFactory iFactory;

    public DiscordService(@Value("${discord.token}") String discordToken,
                          @Autowired CommandLine.IFactory iFactory) {
        this.discordToken = discordToken;
        this.iFactory = iFactory;
    }

    @PostConstruct
    void setup() throws LoginException {
        JDA jda = JDABuilder.createLight(discordToken)
                .addEventListeners(this)
                .setActivity(Activity.playing("Type /opscan"))
                .build();

        jda.retrieveCommands().complete().stream().map(Command::toString).peek(log::info);

        CommandData commandData = new CommandData("opscan", "Run OpScan");

        SubcommandData scanData = new SubcommandData("scan", "Run a scan");
        scanData.addOption(OptionType.STRING, "symbol", "stock symbol", true);

        OptionData scanTypeOptionData = new OptionData(OptionType.STRING, "type", "Type of scan to run");

        scanTypeOptionData.addChoice("short-put", "short-put");
        scanTypeOptionData.addChoice("short-call", "short-call");
        scanTypeOptionData.addChoice("long-vertical", "long-vertical");
        scanTypeOptionData.addChoice("short-vertical", "short-vertical");
        scanTypeOptionData.setRequired(true);

        scanData.addOptions(scanTypeOptionData);

        scanData.addOption(OptionType.STRING, "args", "args", false);

        commandData.addSubcommands(scanData);

        jda.updateCommands()
                .addCommands(commandData)
                .complete();

        log.info("Discord connected");

        log.info("Logged in as {}", jda.getSelfUser().getAsTag());

    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {

        log.info("Received command {} from {} on {}/{}: {} {} {}",
                event.getName(),
                event.getUser().getAsTag(),
                event.getGuild() == null ? "" : event.getGuild().getName(),
                event.getChannel().getName(),
                event.getOption("symbol"),
                event.getOption("type"),
                event.getOption("args")
        );

        var symbol = Optional.ofNullable(event.getOption("symbol")).orElseThrow().getAsString();

        var scanType = Optional.ofNullable(event.getOption("type")).orElseThrow().getAsString();

        var args = Optional.ofNullable(event.getOption("args"))
                .map(OptionMapping::getAsString).orElse("");


        runCommand(String.format("%s %s %s %s", event.getSubcommandName(), symbol, scanType, args), event).queue();

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
