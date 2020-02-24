package fyi.sorenneedscoffee.xputil.commands;

import com.jagrosh.jdautilities.command.Command;

/**
 * -=XPUtil=-
 *
 * @author Soren Dangaard (joseph.md.sorensen@gmail.com)
 */
public abstract class XpCommand extends Command {

    protected XpCommand() {

        this.category = new Category("User");
        this.guildOnly = true;

    }

}
