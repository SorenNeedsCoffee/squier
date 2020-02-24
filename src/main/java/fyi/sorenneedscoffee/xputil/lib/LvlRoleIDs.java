package fyi.sorenneedscoffee.xputil.lib;

/**
 * -=XPUtil=-
 *
 * @author Soren Dangaard (joseph.md.sorensen@gmail.com)
 */
public enum LvlRoleIDs {
    LVL1("680816454740082718"),
    LVL5("680816489305341964"),
    LVL10("680816513661665280"),
    LVL15("680816536923144255"),
    LVL20("680816560222240789"),
    LVL30("680816686772781096"),
    LVL40("680816712601305179"),
    LVL50("680816728066097181"),
    LVL75("680816741596921876"),
    LVL100("680816757736341565");

    private final String id;

    LvlRoleIDs(String id) {
        this.id = id;
    }

    public static String getLvlRole(int lvl) {
        return isBetween(lvl, 1, 4) ? LVL1.getId() :
                isBetween(lvl, 5, 9) ? LVL5.getId() :
                isBetween(lvl, 10, 14) ? LVL10.getId() :
                isBetween(lvl, 15, 19) ? LVL15.getId() :
                isBetween(lvl, 20, 29) ? LVL20.getId() :
                isBetween(lvl, 30, 39) ? LVL30.getId() :
                isBetween(lvl, 40, 49) ? LVL40.getId() :
                isBetween(lvl, 50, 74) ? LVL50.getId() :
                isBetween(lvl, 75, 99) ? LVL75.getId() :
                LVL100.getId();
    }

    private static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    public String getId() {
        return this.id;
    }
}
