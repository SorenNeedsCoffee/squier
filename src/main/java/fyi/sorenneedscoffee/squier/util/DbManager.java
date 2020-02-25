package fyi.sorenneedscoffee.squier.util;

import fyi.sorenneedscoffee.squier.util.data.DataSet;
import fyi.sorenneedscoffee.squier.config.StatsDb;
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
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static fyi.sorenneedscoffee.squier.db.tables.Statistics.STATISTICS;

public class DbManager {
    private final Logger log = LoggerFactory.getLogger("DbManager");
    private final DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ZoneId system = ZoneId.systemDefault();
    private final String url;

    public DbManager(StatsDb db) {
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
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime utc = TimeUtil.toUTC(now, system);
        Timestamp stamp = Timestamp.valueOf(TimeUtil.formatter.format(utc));

        try (Connection connect = DriverManager.getConnection(url)) {
            DSLContext context = DSL.using(connect, SQLDialect.MARIADB);

            context.insertInto(STATISTICS, STATISTICS.DATE, STATISTICS.ONLINEUSERS)
                    .values(stamp, tally)
                    .execute();

        } catch (SQLException e) {
            log.error("JDBC experienced the following error:" + ExceptionUtils.getMessage(e) + " Please see below for details");
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public DataSet getStatistics(String date, String timezone) {
        LocalDateTime localDate = LocalDateTime.parse(date + " 00:00:00", parser);

        Timestamp min = Timestamp.valueOf(TimeUtil.formatter.format(TimeUtil.toUTC(localDate, ZoneId.of(timezone))));
        Timestamp max = Timestamp.valueOf(TimeUtil.formatter.format(TimeUtil.toUTC(localDate.plusDays(1), ZoneId.of(timezone))));
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
        } catch (DateTimeParseException e) {
            log.error(ExceptionUtils.getMessage(e));
            log.error(ExceptionUtils.getStackTrace(e));
        }

        return null;
    }
}
