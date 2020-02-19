package fyi.sorenneedscoffee.statistics;

import fyi.sorenneedscoffee.statistics.config.Db;
import fyi.sorenneedscoffee.statistics.data.DataSet;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;

import static fyi.sorenneedscoffee.statistics.db.tables.Statistics.STATISTICS;

public class DbManager {
    private final Logger log = LoggerFactory.getLogger("DbManager");
    private final String url;

    public DbManager(Db db) {
        url = "jdbc:mariadb://" + db.getIp() + "/" + db.getDb() + "?"
                + "user=" + db.getUser() + "&password=" + db.getPass();
        log.info("Validating connection to " + db.getDb() + " at " + db.getIp() + "...");

        try (Connection connect = DriverManager.getConnection(url)) {
            if (connect.isValid(5))
                log.info("Success.");
            else
                log.error("Failed. Please check your configuration");
        } catch (SQLException e) {
            log.error("JDBC experienced the following error:" + ExceptionUtils.getMessage(e) + " Please see below for details");
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void saveTally(int tally) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try (Connection connect = DriverManager.getConnection(url)) {
            DSLContext context = DSL.using(connect, SQLDialect.MARIADB);

            context.insertInto(STATISTICS, STATISTICS.DATE, STATISTICS.ONLINEUSERS)
                    .values(now, tally)
                    .execute();

        } catch (SQLException e) {
            log.error("JDBC experienced the following error:" + ExceptionUtils.getMessage(e) + " Please see below for details");
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public DataSet getStatistics(int year, int month, int day) {
        Timestamp min = Timestamp.valueOf(year + month + day + "00:00:00");
        Timestamp max = Timestamp.valueOf(year + month + (day+1) + "00:00:00");
        try (Connection connect = DriverManager.getConnection(url)) {
            DSLContext context = DSL.using(connect, SQLDialect.MARIADB);

            Result<Record> result = context.select()
                    .from(STATISTICS)
                    .where(STATISTICS.DATE.greaterOrEqual(min))
                    .and(STATISTICS.DATE.lessThan(max))
                .fetch();

            return new DataSet(result.intoResultSet());
        } catch (SQLException e) {
            log.error("JDBC experienced the following error:" + ExceptionUtils.getMessage(e) + " Please see below for details");
            log.error(ExceptionUtils.getStackTrace(e));
        }

        return null;
    }
}
