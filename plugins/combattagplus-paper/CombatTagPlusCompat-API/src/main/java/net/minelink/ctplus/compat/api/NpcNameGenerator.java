package net.minelink.ctplus.compat.api;

import org.bukkit.Bukkit;

import java.util.Random;

public final class NpcNameGenerator {

    private static Random random = new Random();

    public static String generate() {
        String name = null;

        while (name == null || Bukkit.getPlayerExact(name) != null) {
            name = "PvPLogger" + random.nextInt(100000);
        }

        return name;
    }

}
