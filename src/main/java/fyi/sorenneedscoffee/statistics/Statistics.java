package fyi.sorenneedscoffee.statistics;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import fyi.sorenneedscoffee.statistics.config.Config;
import fyi.sorenneedscoffee.statistics.config.ConfigManager;
import fyi.sorenneedscoffee.statistics.config.Db;
import fyi.sorenneedscoffee.statistics.listeners.Listener;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.Console;

public class Statistics {
    private static boolean shuttingDown = false;
    private static JDA jda;
    public static final String version = Statistics.class.getPackage().getImplementationVersion();

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger("Startup");

        if (version != null)
            log.info("M.E Statistics | v" + version);
        else
            log.info("M.E Statistics | DEVELOPMENT MODE");

        log.info("Loading Config...");
        Config config = ConfigManager.load();

        String token = config.getToken();
        String ownerId = config.getOwnerId();
        Db db = config.getDb();

        CommandClientBuilder cb = new CommandClientBuilder()
                .setOwnerId(ownerId)
                .setActivity(Activity.watching("over you fools."));

        CommandClient client = cb.build();
        Listener listener = new Listener(db);

        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.playing("loading..."))
                    .addEventListeners(client, listener)
                    .build();
        } catch (LoginException ex) {
            log.error("Invalid Token");
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LoggerFactory.getLogger("M.E Statistics").info("Shutting down...");
            shutdown();
        }));

        Console console = System.console();
        Thread th = new Thread(() -> {
            while (true) {
                String in = console.readLine();
                if("shutdown".equals(in))
                    System.exit(0);
            }
        });
        th.start();
    }

    public static void shutdown() {
        if (shuttingDown)
            return;
        shuttingDown = true;
        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        if (jda.getStatus() != JDA.Status.SHUTTING_DOWN)
            jda.shutdown();
        Runtime.getRuntime().exit(0);
    }
}
