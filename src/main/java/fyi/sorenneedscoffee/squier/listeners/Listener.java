package fyi.sorenneedscoffee.squier.listeners;

import fyi.sorenneedscoffee.squier.util.DbManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Listener extends ListenerAdapter {
    private final Timer timer = new Timer();
    private JDA jda;
    private DbManager db;

    public Listener(DbManager db) {
        this.db = db;
    }

    @Override
    public void onReady(ReadyEvent event) {
        this.jda = event.getJDA();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int tally = 0;

                List<Member> members = jda.getGuildById("624262853997625384").getMembers();

                for (Member member : members) {
                    User user = member.getUser();

                    if (!(user.isBot() || user.isFake())) {
                        if (member.getOnlineStatus().equals(OnlineStatus.ONLINE) || member.getOnlineStatus().equals(OnlineStatus.IDLE))
                            tally++;
                    }
                }

                db.saveTally(tally);
            }
        }, 0, 1800000);
    }
}
