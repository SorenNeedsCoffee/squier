package fyi.sorenneedscoffee.xputil.util;

import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * -=XPUtil=-
 *
 * @author Soren Dangaard (joseph.md.sorensen@gmail.com)
 */
@SuppressWarnings("unchecked")
public class UserManager {
    private static List<User> users = new ArrayList<>();
    private static DbManager db;

    static void addUser(String id) {
        db.addUser(id, 1, 0.0);
    }

    static void removeUser(String id) {
        db.delUser(id);
    }

    public static void initDb(String url, String dbName, String user, String pass) throws Exception {
        db = new DbManager(url, dbName, user, pass);
    }

    public static void pruneUsers(Guild guild) {
        Logger log = LoggerFactory.getLogger("PruneMembers");
        List<String> toRemove = new ArrayList<>();
        for (User user : users) {
            if (guild.getMemberById(user.getId()) == null) {
                log.info("Removing user with ID " + user.getId());
                toRemove.add(user.getId());
            }
        }
        for (String id : toRemove) {
            removeUser(id);
            log.info("Removed user with ID " + id);
        }
    }

    public static User getUser(String id) {
        return db.getUser(id);
    }

    public static List<User> getUsers() {
        return db.getUsers();
    }

    public static void updateUser(User user) {
        if (user.getId().equals("") || user.getId() == null)
            throw new IllegalArgumentException("Id of user cannot be empty or null.");

        db.updateUser(user);
    }

    public static void saveFile() {
        Logger log = LoggerFactory.getLogger("SaveMembersToJSON");

        JSONObject file = getJSON();

        try {
            Files.write(Paths.get("backup.json"), file.toString().getBytes());
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public static JSONObject getJSON() {
        users = db.getUsers();
        JSONArray data = createJsonArrayFromList();
        JSONObject obj = new JSONObject();
        obj.put("data", data);

        return obj;
    }

    static void loadFile() {
        Logger log = LoggerFactory.getLogger("loadMembersFromFile");

        JSONObject raw = null;
        try {
            raw = (JSONObject) new JSONParser().parse(new FileReader("members.json"));
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException: members file not found. Please ensure that the members file exists, is in the same directory as the jar, and is called members.json");
            System.exit(1);
        } catch (IOException | ParseException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            System.exit(1);
        }

        JSONArray members = (JSONArray) raw.get("data");

        for (Object user : members) {
            JSONObject obj = (JSONObject) user;
            users.add(new User((String) obj.get("id"), (double) obj.get("xp"), ((Long) obj.get("lvl")).intValue()));

        }

        for (User user : users) {
            db.addUser(user.getId(), user.getLvl(), user.getXp());
        }
    }

    private static JSONArray createJsonArrayFromList() {
        JSONArray result = new JSONArray();
        for (User user : users) {
            JSONObject obj = new JSONObject();
            obj.put("id", user.getId());
            obj.put("xp", user.getXp());
            obj.put("lvl", user.getLvl());
            result.add(obj);
        }
        return result;
    }
}
