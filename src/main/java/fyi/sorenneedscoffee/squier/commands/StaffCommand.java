package fyi.sorenneedscoffee.squier.commands;

import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.api.Permission;

public abstract class StaffCommand extends Command {
    protected StaffCommand() {
        this.category = new Category("Staff", event ->
                event.getAuthor().getId().equals(event.getClient().getOwnerId())
                        || event.getGuild() == null
                        || event.getMember().hasPermission(Permission.MANAGE_SERVER)
        );
        this.guildOnly = true;
    }
}
