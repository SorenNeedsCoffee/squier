package fyi.sorenneedscoffee.xputil.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import fyi.sorenneedscoffee.xputil.lib.LvlRoleIDs;
import fyi.sorenneedscoffee.xputil.lib.XpInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.*;

/**
 * -=XPUtil=-
 *
 * @author Soren Dangaard (joseph.md.sorensen@gmail.com)
 */
public class XpListener extends ListenerAdapter {
    private static JDA jda;
    private static Guild guild;
    private Logger log;
    private List<String> cooldown = new ArrayList<>();
    private Timer timer = new Timer();

    public XpListener() {
        this.log = LoggerFactory.getLogger("XpUtil");
    }

    public static void replaceRole(Guild guild, Member member, String regex, String replace) {
        Objects.requireNonNull(guild).removeRoleFromMember(member, Objects.requireNonNull(jda.getRoleById(regex))).queue();
        Objects.requireNonNull(guild).addRoleToMember(member, Objects.requireNonNull(jda.getRoleById(replace))).queue();
    }

    public static void addXP(net.dv8tion.jda.api.entities.User user, double amt) {
        User update = UserManager.getUser(user.getId());
        update.addXp(amt);
        if (update.getXp() >= XpInfo.lvlXpRequirementTotal(update.getLvl())) {
            onLvlUp(user, guild.getDefaultChannel(), update);
        }
        UserManager.updateUser(update);
    }

    static void onLvlUp(net.dv8tion.jda.api.entities.User user, TextChannel channel, User update) {
        update.setLvl(update.getLvl() + 1);

        String name;
        if (guild.getMember(user).getNickname() != null) {
            name = guild.getMember(user).getNickname();
        } else {
            name = user.getName();
        }


        try {
            Webhook hook = channel.createWebhook(name).setAvatar(Icon.from(new URL(user.getAvatarUrl()).openStream())).complete();

            if (update.getLvl() != 1) {
                WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                float[] rgb;

                embed.setTitle(new WebhookEmbed.EmbedTitle("Level up!", null));
                embed.setDescription("Congrats to " + name + " for reaching level " + update.getLvl() + "!");
                rgb = Color.RGBtoHSB(204, 255, 94, null);
                embed.setColor(Color.getHSBColor(rgb[0], rgb[1], rgb[2]).getRGB());

                WebhookClient client = WebhookClient.withUrl(hook.getUrl());
                client.send(embed.build());
                client.close();
            }

            hook.delete().complete();
        } catch (IOException ignore) {
        }

        switch (update.getLvl()) {
            case 5:
                replaceRole(guild, guild.getMember(user), LvlRoleIDs.LVL1.getId(), LvlRoleIDs.LVL5.getId());
            case 10:
                replaceRole(guild, guild.getMember(user), LvlRoleIDs.LVL5.getId(), LvlRoleIDs.LVL10.getId());
            case 15:
                replaceRole(guild, guild.getMember(user), LvlRoleIDs.LVL10.getId(), LvlRoleIDs.LVL15.getId());
            case 20:
                replaceRole(guild, guild.getMember(user), LvlRoleIDs.LVL15.getId(), LvlRoleIDs.LVL20.getId());
            case 30:
                replaceRole(guild, guild.getMember(user), LvlRoleIDs.LVL20.getId(), LvlRoleIDs.LVL30.getId());
            case 40:
                replaceRole(guild, guild.getMember(user), LvlRoleIDs.LVL30.getId(), LvlRoleIDs.LVL40.getId());
            case 50:
                replaceRole(guild, guild.getMember(user), LvlRoleIDs.LVL40.getId(), LvlRoleIDs.LVL50.getId());
            case 75:
                replaceRole(guild, guild.getMember(user), LvlRoleIDs.LVL50.getId(), LvlRoleIDs.LVL75.getId());
            case 100:
                replaceRole(guild, guild.getMember(user), LvlRoleIDs.LVL75.getId(), LvlRoleIDs.LVL100.getId());
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        log.info("Getting things ready...");
        jda = event.getJDA();
        List<Guild> guilds = jda.getGuilds();
        for (Guild guild : guilds) {
            XpListener.guild = guild;
            List<Member> members = guild.getMembers();
            for (Member member : members) {
                if (!(member.getUser().isBot() || member.getUser().isFake() ||
                        UserManager.getUser(member.getId()) != null))
                    UserManager.addUser(member.getId());

                if (!member.getRoles().contains(guild.getRoleById(LvlRoleIDs.LVL1.getId())) &&
                        !(member.getUser().isBot() || member.getUser().isFake()) &&
                        (Objects.requireNonNull(UserManager.getUser(member.getId())).getLvl() < 5))
                    guild.addRoleToMember(member, jda.getRoleById(LvlRoleIDs.LVL1.getId())).queue();
            }
        }
        log.info("XPUtil version 0.2 ready");
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!(event.getUser().isBot() || event.getUser().isFake())) {
            event.getGuild().addRoleToMember(event.getMember(), Objects.requireNonNull(event.getGuild().getRoleById(LvlRoleIDs.LVL1.getId()))).queue();
            UserManager.addUser(event.getMember().getId());
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        if (!(event.getUser().isBot() || event.getUser().isFake()))
            UserManager.removeUser(event.getMember().getId());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake())
            return;

        if (cooldown.indexOf(event.getAuthor().getId()) == -1 &&
                !event.getMessage().getContentDisplay().startsWith("!>")
        ) {
            User update = UserManager.getUser(event.getAuthor().getId());
            update.addXp(XpInfo.earnedXP(event.getMessage().getContentDisplay().replaceAll(" ", "")));
            if (update.getXp() >= XpInfo.lvlXpRequirementTotal(update.getLvl())) {
                onLvlUp(event.getAuthor(), event.getChannel(), update);
            }
            UserManager.updateUser(update);
            cooldown.add(event.getAuthor().getId());
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    cooldown.remove(event.getAuthor().getId());
                }
            }, 5000);
        }
    }
}
