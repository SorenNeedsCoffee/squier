package fyi.sorenneedscoffee.squier.commands.staff;

import com.jagrosh.jdautilities.command.CommandEvent;
import fyi.sorenneedscoffee.squier.commands.StaffCommand;
import fyi.sorenneedscoffee.squier.util.DbManager;
import fyi.sorenneedscoffee.squier.util.TimeUtil;
import fyi.sorenneedscoffee.squier.util.data.DataEntry;
import fyi.sorenneedscoffee.squier.util.data.DataSet;
import net.dv8tion.jda.api.EmbedBuilder;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

public class GetStatsCommand extends StaffCommand {
    private DbManager db;

    public GetStatsCommand(DbManager db) {
        this.db = db;

        this.name = "stats";
        this.help = "displays stats for a given day";
        this.arguments = "<date (formatted as year-month-day, cannot be blank!)> <timezone (must be in continent/city, for example America/Denver)>";
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();

        if(args == null || args.isEmpty()) {
            event.replyError("Args cannot be blank!");
            return;
        }

        String[] splitArgs = args.split(" ");

        String date = splitArgs[0];
        String timezone = splitArgs[1];

        DataSet set = db.getStatistics(date, timezone);

        if(set == null || set.isEmpty()) {
            event.replyError("Error parsing data. Is the date formatted correctly?");
            return;
        }

        TreeMap<Timestamp, Integer> map = set.getMap();
        List<Timestamp> times = new ArrayList<>();
        List<Integer> oUsers = new ArrayList<>();

        for(Map.Entry<Timestamp, Integer> e : map.entrySet()) {
            LocalDateTime t = e.getKey().toLocalDateTime();
            times.add(Timestamp.valueOf(
                    TimeUtil.formatter.format(TimeUtil.fromUTC(t, ZoneId.of(timezone)))
                )
            );
            oUsers.add(e.getValue());
        }

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
        File f = null;

        try {
            f = new File("chart.png");
            ImageIO.write(BitmapEncoder.getBufferedImage(chart), "png", f);

            DataEntry max = set.getMax();
            DataEntry min = set.getMin();

            event.getChannel().sendMessage(
                    new EmbedBuilder()
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
                            )
                            .build()
            ).queue();
            event.getChannel().sendFile(f).queue();
        } catch (IOException e) {
            event.replyError("Something went wrong");
            Logger log = LoggerFactory.getLogger("GetStatistics");
            log.error(e.getMessage());
            log.error(ExceptionUtils.getStackTrace(e));
        }
        f.delete();
    }
}
