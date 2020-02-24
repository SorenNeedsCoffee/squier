package fyi.sorenneedscoffee.xputil.commands.xp;

import com.jagrosh.jdautilities.command.CommandEvent;
import fyi.sorenneedscoffee.xputil.commands.XpCommand;
import fyi.sorenneedscoffee.xputil.util.User;
import fyi.sorenneedscoffee.xputil.util.UserManager;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * -=XPUtil=-
 *
 * @author Soren Dangaard (joseph.md.sorensen@gmail.com)
 */
public class TopCmd extends XpCommand {

    public TopCmd() {
        this.name = "top";
        this.help = "display users with top xp values";
    }

    @SuppressWarnings("ConstantConditions")
    private static String list(List<User> users, CommandEvent event) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            User user = users.get(i);
            result.append("\n\n");
            if (event.getGuild().getMemberById(user.getId()).getNickname() != null) {
                result.append(i + 1).
                        append(". ").
                        append(event.getGuild().getMemberById(user.getId()).getNickname());
            } else {
                result.append(i + 1).
                        append(". ").
                        append(event.getJDA().getUserById(user.getId()).getName());
            }
            result.append(" (XP: ").append(new DecimalFormat("#.##").format(user.getXp())).append(")");
            result.append("\n");
            result.append("    Level: ").append(user.getLvl());
        }
        return result.toString();
    }

    @Override
    protected void execute(CommandEvent event) {
        List<User> users = UserManager.getUsers();
        Objects.requireNonNull(users).sort(Collections.reverseOrder());
        EmbedBuilder embed = new EmbedBuilder();
        float[] rgb;
        embed.setTitle("Top Users");
        //embed.setAuthor("User Level", null, event.getAuthor().getAvatarUrl());
        embed.addField("",
                "```java\n" +
                        "----------------------\n" +
                        list(users, event) +
                        "\n\n----------------------\n" +
                        "```",

                true
        );

        rgb = Color.RGBtoHSB(204, 255, 94, null);
        embed.setColor(Color.getHSBColor(rgb[0], rgb[1], rgb[2]));

        event.reply(embed.build());
    }
}
