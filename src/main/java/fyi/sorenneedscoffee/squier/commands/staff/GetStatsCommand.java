package fyi.sorenneedscoffee.squier.commands.staff;

import com.jagrosh.jdautilities.command.CommandEvent;
import fyi.sorenneedscoffee.squier.commands.StaffCommand;
import fyi.sorenneedscoffee.squier.util.DbManager;
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
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

public class GetStatsCommand extends StaffCommand {
    private DbManager db;

    public GetStatsCommand(DbManager db) {
        this.db = db;

        this.name = "stats";
        this.help = "displays stats for a given day";
        this.arguments = "<date (formatted as year-month-day, cannot be blank!)> <timezone (in gmt format, such as GMT-7)>";
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();

        if(args == null || args.isEmpty()) {
            event.replyError("Args cannot be blank!");
            return;
        }

        String[] splitArgs = args.split(" ");
        String[] splitDate = splitArgs[0].split("-");

        int year = Integer.parseInt(splitDate[0]);
        int month = Integer.parseInt(splitDate[1]);
        int day = Integer.parseInt(splitDate[2]);
        String timezone = "Etc/" + splitArgs[1];

        DataSet set = db.getStatistics(year, month, day, timezone);

        if(set == null || set.isEmpty()) {
            event.replyError("Error parsing data. Is the date formatted correctly?");
            return;
        }

        TreeMap<Timestamp, Integer> map = set.getMap();
        List<Timestamp> times = new ArrayList<>();
        List<Integer> oUsers = new ArrayList<>();

        Calendar offset = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        offset.set(year, month-1, day, 0, 0, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for(Map.Entry<Timestamp, Integer> e : map.entrySet()) {
            times.add(Timestamp.valueOf(
                sdf.format(new Date(e.getKey().getTime() +
                    TimeZone.getTimeZone(timezone).getOffset(offset.getTimeInMillis()))
                )
            ));
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
                            .setTitle("Squier for " + Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH) + " " + day + ", " + year)
                            .setColor(new Color(8311585))
                            .addField("Average Online Users", Double.toString(set.getAverage()), false)
                            .addField("Most Online Users", sdf.format(new Date(max.getDate().getTime() +
                                    TimeZone.getTimeZone(timezone).getOffset(offset.getTimeInMillis()))
                            ) + " | " + max.getOnlineUsers(), false)
                            .addField("Least Online Users", sdf.format(new Date(min.getDate().getTime() +
                                    TimeZone.getTimeZone(timezone).getOffset(offset.getTimeInMillis()))
                            ) + " | " + min.getOnlineUsers(), false)
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
