package fyi.sorenneedscoffee.statistics.util.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class DataSet {
    private final List<DataEntry> entries = new ArrayList<>();

    public DataSet(ResultSet r) throws SQLException {
        while(r.next()) {
            entries.add(new DataEntry(r.getTimestamp("Date"), r.getInt("OnlineUsers")));
        }
    }

    public double getAverage() {
        int userCount = 0;
        for(DataEntry e : entries)
            userCount += e.getOnlineUsers();

        return userCount/entries.size();
    }

    public TreeMap<Timestamp, Integer> getMap() {
        Map<Timestamp, Integer> map = new HashMap<>();

        for(DataEntry e : entries) {
            map.put(e.getDate(), e.getOnlineUsers());
        }

        return new TreeMap<>(map);
    }
}
