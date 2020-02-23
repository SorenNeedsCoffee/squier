package fyi.sorenneedscoffee.statistics;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import fyi.sorenneedscoffee.statistics.commands.staff.GetStatsCommand;
import fyi.sorenneedscoffee.statistics.config.Config;
import fyi.sorenneedscoffee.statistics.config.ConfigManager;
import fyi.sorenneedscoffee.statistics.config.UsersDb;
import fyi.sorenneedscoffee.statistics.listeners.Listener;
import fyi.sorenneedscoffee.statistics.util.DbManager;
import fyi.sorenneedscoffee.xputil.XPUtil;
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

    public static void main(String[] args) throws Exception {
        Logger log = LoggerFactory.getLogger("Startup");

        if (version != null)
            log.info("M.E Statistics | v" + version);
        else
            log.info("M.E Statistics | DEVELOPMENT MODE");

        log.info("Loading Config...");
        Config config = ConfigManager.load();

        String token = config.getToken();
        String ownerId = config.getOwnerId();
        DbManager db = new DbManager(config.getStatsDb());

        CommandClientBuilder cb = new CommandClientBuilder()
                .setOwnerId(ownerId)
                .setPrefix("!>")
                .setEmojis("\u2705", "\u26A0", "\u26D4")
                .addCommands(
                        new GetStatsCommand(db)
                )
                .setActivity(Activity.watching("over you fools"));

        XPUtil xpUtil = new XPUtil(cb);
        UsersDb usersDb = config.getUsersDb();
        xpUtil.db(usersDb.getIp(), usersDb.getDb(), usersDb.getUser(), usersDb.getPass());
        cb = xpUtil.builder();

        CommandClient client = cb.build();
        Listener listener = new Listener(db);

        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.playing("loading..."))
                    .addEventListeners(client, listener, xpUtil.listener())
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
                    shutdown();
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
