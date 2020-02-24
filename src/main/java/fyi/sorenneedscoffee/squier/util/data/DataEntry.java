package fyi.sorenneedscoffee.squier.util.data;

import java.sql.Timestamp;

public class DataEntry {
    private final Timestamp date;
    private final int onlineUsers;

    public DataEntry(Timestamp date, int onlineUsers) {
        this.date = date;
        this.onlineUsers = onlineUsers;
    }

    public Timestamp getDate() {
        return date;
    }

    public int getOnlineUsers() {
        return onlineUsers;
    }
}
