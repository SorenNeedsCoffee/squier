package fyi.sorenneedscoffee.xputil;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import fyi.sorenneedscoffee.xputil.commands.xp.LvlCmd;
import fyi.sorenneedscoffee.xputil.commands.xp.TopCmd;
import fyi.sorenneedscoffee.xputil.util.UserManager;
import fyi.sorenneedscoffee.xputil.util.XpListener;

/**
 * -=XPUtil=-
 * A flexible User XP library in active development.
 *
 * @author Soren Dangaard (joseph.md.sorensen@gmail.com)
 */

public class XPUtil {
    private final CommandClientBuilder builder;
    private final XpListener listener;

    /**
     * On startup, you can initialize the XPUtil class to handle command client and listener setup. Be sure to add the XPListener to JDA!
     *
     * @param builder Pass your CommandClientBuilder here. This will only add the current level and top commands and will NOT build the client for you.
     */
    public XPUtil(CommandClientBuilder builder) {
        builder.addCommands(
                new LvlCmd(),
                new TopCmd()
        );
        this.builder = builder;
        this.listener = new XpListener();
    }

    public void db(String ip, String db, String user, String pass) throws Exception {
        UserManager.initDb(ip, db, user, pass);
    }

    public CommandClientBuilder builder() {
        return this.builder;
    }

    public XpListener listener() {
        return this.listener;
    }
}
