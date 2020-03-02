package fyi.sorenneedscoffee.squier.commands.staff;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import fyi.sorenneedscoffee.squier.Squier;
import fyi.sorenneedscoffee.squier.commands.StaffCommand;
import fyi.sorenneedscoffee.squier.util.DbManager;
import fyi.sorenneedscoffee.squier.util.TimeUtil;
import fyi.sorenneedscoffee.squier.util.data.DataEntry;
import fyi.sorenneedscoffee.squier.util.data.DataSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class GetStatsCommand extends StaffCommand {
    private DbManager db;
    private Paginator.Builder test;

    public GetStatsCommand(DbManager db) {
        this.db = db;

        this.name = "stats";
        this.help = "displays stats for a given day";
        this.arguments = "<date (formatted as year-month-day, cannot be blank!)> <timezone (must be in continent/city, for example America/Denver)>";

        test = new Paginator.Builder();
        test.setItemsPerPage(10);
        test.setEventWaiter(Squier.waiter);
        test.setColumns(2);
        for (String zone : ZoneId.getAvailableZoneIds()) {
            if (zone.startsWith("Etc/GMT"))
                test.addItems(zone.replace("Etc/", ""));
        }
        test.setText("Available Zones");
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();

        if (args == null || args.isEmpty()) {
            event.replyError("Args cannot be blank!");
            return;
        }

        String[] splitArgs = args.split(" ");

        if (splitArgs[0].equals("-availablezones")) {
            event.getAuthor().openPrivateChannel().queue((channel) ->
                    test.build().display(channel));
            return;
        }

        String date = splitArgs[0];
        String timezone = null;

        if (splitArgs[1].contains("+"))
            timezone = "Etc/" + splitArgs[1].replace("+", "-");
        if (splitArgs[1].contains("-"))
            timezone = "Etc/" + splitArgs[1].replace("-", "+");

        ZoneId zone;

        try {
            zone = ZoneId.of(timezone);
        } catch (ZoneRulesException e) {
            event.replyError("Invalid timezone. Use !>stats -availablezones to see available timezone IDs.");
            return;
        }

        DataSet set = db.getStatistics(date, zone);

        if (set == null) {
            event.replyError("Error parsing data. Is the date formatted correctly?");
            return;
        }

        if (set.isEmpty()) {
            event.replyError("No records found.");
            return;
        }

        TreeMap<Timestamp, Integer> map = set.getMap();
        List<Timestamp> times = new ArrayList<>();
        List<Integer> oUsers = new ArrayList<>();

        for (Map.Entry<Timestamp, Integer> e : map.entrySet()) {
            LocalDateTime t = e.getKey().toLocalDateTime();
            times.add(Timestamp.valueOf(
                    TimeUtil.formatter.format(TimeUtil.fromUTC(t, ZoneId.of(timezone)))
                    )
            );
            oUsers.add(e.getValue());
        }

        sendMsg(event, date, timezone, set, times, oUsers);
    }

    private CompletableFuture<Boolean> sendMsg(CommandEvent event, String date, String timezone, DataSet set, List<Timestamp> times, List<Integer> oUsers) {
        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(400)
                .build();
        chart.getStyler()
                .setLegendVisible(false)
                .setPlotBackgroundColor(Color.black)
                .setSeriesColors(new Color[]{Color.white})
                .setChartBackgroundColor(Color.darkGray);
        chart.addSeries("test", times, oUsers);
        File f = new File("chart.png");
        if (!await(write(chart, f))) {
            event.replyError("Something went wrong");
            return completedFuture(false);
        }

        DataEntry max = set.getMax();
        DataEntry min = set.getMin();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Statistics for " + date)
                .setColor(new Color(8311585))
                .addField("Average Online Users", Double.toString(set.getAverage()), false)
                .addField("Most Online Users",
                        TimeUtil.formatter.format(TimeUtil.fromUTC(max.getDate().toLocalDateTime(), ZoneId.of(timezone)))
                                + " | " + max.getOnlineUsers(),
                        false
                )
                .addField("Least Online Users",
                        TimeUtil.formatter.format(TimeUtil.fromUTC(min.getDate().toLocalDateTime(), ZoneId.of(timezone)))
                                + " | " + min.getOnlineUsers(),
                        false
                );

        event.getChannel().sendMessage(new MessageBuilder().setEmbed(embed.build()).build()).addFile(f).queue();
        f.delete();
        return completedFuture(true);
    }

    private CompletableFuture<Boolean> write(XYChart chart, File f) {
        try {
            ImageIO.write(BitmapEncoder.getBufferedImage(chart), "png", f);
            return completedFuture(true);
        } catch (IOException e) {
            Logger log = LoggerFactory.getLogger("GetStatistics");
            log.error(e.getMessage());
            log.error(ExceptionUtils.getStackTrace(e));
            return completedFuture(false);
        }
    }
}
