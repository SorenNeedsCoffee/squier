package fyi.sorenneedscoffee.statistics.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataSet {
    private final List<DataEntry> entries = new ArrayList<>();

    public DataSet(ResultSet r) throws SQLException {
        while(r.next()) {
            entries.add(new DataEntry(r.getTimestamp("date"), r.getInt("OnlineUsers")));
        }
    }

    public double getAverage() {
        int userCount = 0;
        for(DataEntry e : entries)
            userCount += e.getOnlineUsers();

        return userCount/entries.size();
    }
}
