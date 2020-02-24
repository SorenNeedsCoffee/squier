package fyi.sorenneedscoffee.squier.util.data;

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

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public double getAverage() {
        int userCount = 0;
        for(DataEntry e : entries)
            userCount += e.getOnlineUsers();

        return userCount/entries.size();
    }

    public DataEntry getMax() {
        DataEntry result = null;
        int max = 0;

        for(DataEntry e : entries) {
            if(e.getOnlineUsers() >= max) {
                max = e.getOnlineUsers();
                result = e;
            }
        }

        return result;
    }

    public DataEntry getMin() {
        DataEntry result = null;
        int min = getMax().getOnlineUsers();

        for(DataEntry e : entries) {
            if(e.getOnlineUsers() <= min) {
                min = e.getOnlineUsers();
                result = e;
            }
        }

        return result;
    }

    public TreeMap<Timestamp, Integer> getMap() {
        Map<Timestamp, Integer> map = new HashMap<>();

        for(DataEntry e : entries) {
            map.put(e.getDate(), e.getOnlineUsers());
        }

        return new TreeMap<>(map);
    }
}
