package fyi.sorenneedscoffee.xputil.util;

import org.jetbrains.annotations.NotNull;

/**
 * -=XPUtil=-
 *
 * @author Soren Dangaard (joseph.md.sorensen@gmail.com)
 */
public class User implements Comparable<User> {
    private final String id;
    private double xp;
    private int lvl;

    public User(String id, double xp, int lvl) {
        this.id = id;
        this.xp = xp;
        this.lvl = lvl;
    }

    public String getId() {
        return id;
    }

    public double getXp() {
        return xp;
    }

    public void setXp(double val) {
        this.xp = val;
    }

    public int getLvl() {
        return lvl;
    }

    public void setLvl(int val) {
        this.lvl = val;
    }

    public void addXp(double amt) {
        this.xp += amt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return this.id.equals(user.getId());
    }

    @Override
    public int compareTo(@NotNull User o) {
        return Double.compare(this.getXp(), o.getXp());
    }
}
