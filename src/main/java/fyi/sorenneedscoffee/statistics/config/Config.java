package fyi.sorenneedscoffee.statistics.config;

public final class Config {
    private String token;
    private String ownerId;

    private StatsDb statsDb;
    private UsersDb usersDb;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public StatsDb getStatsDb() {
        return statsDb;
    }

    public void setStatsDb(StatsDb db) {
        this.statsDb = db;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public UsersDb getUsersDb() {
        return usersDb;
    }

    public void setUsersDb(UsersDb usersDb) {
        this.usersDb = usersDb;
    }
}
