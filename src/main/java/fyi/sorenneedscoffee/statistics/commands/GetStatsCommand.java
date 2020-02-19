package fyi.sorenneedscoffee.statistics.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import fyi.sorenneedscoffee.statistics.DbManager;
import fyi.sorenneedscoffee.statistics.data.DataSet;
import org.knowm.xchart.XYChart;

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

        }
    }
}
