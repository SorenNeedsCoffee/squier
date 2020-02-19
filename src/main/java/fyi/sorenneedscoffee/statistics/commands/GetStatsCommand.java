package fyi.sorenneedscoffee.statistics.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import fyi.sorenneedscoffee.statistics.DbManager;
import fyi.sorenneedscoffee.statistics.data.DataSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

public class GetStatsCommand extends Command {
    private DbManager db;

    public GetStatsCommand(DbManager db) {
        this.db = db;

        this.name = "stats";
        this.arguments = "date (formatted as year-month-day)";
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();

        String[] splitDate = args.split("-");

        int year = Integer.parseInt(splitDate[0]);
        int month = Integer.parseInt(splitDate[1]);
        int day = Integer.parseInt(splitDate[2]);

        DataSet set = db.getStatistics(year, month, day);

        if(set == null) {
            event.replyError("Error parsing data. Is the date formatted correctly?");
        } else {
            TreeMap<Timestamp, Integer> map = set.getMap();
            List<Timestamp> times = new ArrayList<>();
            List<Integer> oUsers = new ArrayList<>();
            for(Map.Entry<Timestamp, Integer> e : map.entrySet()) {
                times.add(e.getKey());
                oUsers.add(e.getValue());
            }
            XYChart chart = new XYChartBuilder().width(600).height(400).title("Area Chart").xAxisTitle("X").yAxisTitle("Y").build();
            chart.addSeries("test", times, oUsers);

            try {
                File f = new File("chart.png");
                ImageIO.write(BitmapEncoder.getBufferedImage(chart), "png", f);

                new EmbedBuilder()
                        .setTitle("Statistics for " + Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH) + day + ". " + year)
                        .setColor(new Color(8311585))
                        .addField("Average Online Users", Double.toString(set.getAverage()), false)
                        .build();
                event.getChannel().sendFile(f).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
