package fyi.sorenneedscoffee.squier.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import fyi.sorenneedscoffee.squier.Squier;
import fyi.sorenneedscoffee.squier.commands.OwnerCommand;

public class ShutdownCmd extends OwnerCommand {
    public ShutdownCmd() {
        this.name = "shutdown";
        this.help = "safely shuts down";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.replyWarning("Shutting down...");
        Squier.shutdown();
    }
}
