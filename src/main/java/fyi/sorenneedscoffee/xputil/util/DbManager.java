package fyi.sorenneedscoffee.xputil.util;

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
import java.util.ArrayList;
import java.util.List;

import static fyi.sorenneedscoffee.xputil.db.Tables.USERS;

/**
 * -=XPUtil=-
 *
 * @author Soren Dangaard (joseph.md.sorensen@gmail.com)
 */
class DbManager {
    private final Logger log = LoggerFactory.getLogger("DbManager");
    private final String url;

    DbManager(String ip, String db, String user, String pass) throws SQLException {
        url = "jdbc:mariadb://" + ip + "/" + db + "?"
                + "user=" + user + "&password=" + pass;
        log.info("Validating connection to " + db + " at " + ip + "...");

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

    void addUser(String id, int lvl, double xp) {
        try (Connection connect = DriverManager.getConnection(url)) {
            DSLContext context = DSL.using(connect, SQLDialect.MARIADB);
            context.insertInto(USERS, USERS.ID, USERS.XP, USERS.LVL)
                    .values(id, xp, lvl)
                    .execute();
        } catch (SQLException e) {
            log.error("JDBC experienced the following error:" + ExceptionUtils.getMessage(e) + " Please see below for details");
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    void delUser(String id) {
        try (Connection connect = DriverManager.getConnection(url)) {
            DSLContext context = DSL.using(connect, SQLDialect.MARIADB);
            context.delete(USERS)
                    .where(USERS.ID.eq(id))
                    .execute();
        } catch (SQLException e) {
            log.error("JDBC experienced the following error:" + ExceptionUtils.getMessage(e) + " Please see below for details");
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    void updateUser(User user) {
        try (Connection connect = DriverManager.getConnection(url)) {
            DSLContext context = DSL.using(connect, SQLDialect.MARIADB);
            context.update(USERS)
                    .set(USERS.XP, user.getXp())
                    .set(USERS.LVL, user.getLvl())
                    .where(USERS.ID.eq(user.getId()))
                    .execute();
        } catch (SQLException e) {
            log.error("JDBC experienced the following error:" + ExceptionUtils.getMessage(e) + " Please see below for details");
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    User getUser(String id) {
        try (Connection connect = DriverManager.getConnection(url)) {
            DSLContext context = DSL.using(connect, SQLDialect.MARIADB);
            Result<Record> result = context.select()
                    .from(USERS)
                    .where(USERS.ID.eq(id))
                    .fetch();

            if (!result.isEmpty()) {
                Record r = result.get(0);
                return new User(r.getValue(USERS.ID), r.getValue(USERS.XP), r.getValue(USERS.LVL));
            }
        } catch (SQLException e) {
            log.error("JDBC experienced the following error:" + ExceptionUtils.getMessage(e) + " Please see below for details");
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    List<User> getUsers() {
        try (Connection connect = DriverManager.getConnection(url)) {
            DSLContext context = DSL.using(connect, SQLDialect.MARIADB);
            Result<Record> result = context.select()
                    .from(USERS)
                    .fetch();

            List<User> users = new ArrayList<>();
            for (Record r : result)
                users.add(new User(r.getValue(USERS.ID), r.getValue(USERS.XP), r.getValue(USERS.LVL)));

            return users;
        } catch (SQLException e) {
            log.error("JDBC experienced the following error:" + ExceptionUtils.getMessage(e) + " Please see below for details");
            log.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}
