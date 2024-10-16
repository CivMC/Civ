package net.minelink.ctplus.util;

import org.bukkit.Bukkit;

public final class ReflectionUtils {

    public static final String API_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Class<?> getCompatClass(String className) {
        return getClass("net.minelink.ctplus.compat." + API_VERSION + "." + className);
    }

    public static Class<?> getNmsClass(String className) throws Exception {
        return getClass("net.minecraft.server." + API_VERSION + "." + className);
    }

    public static Class<?> getObcClass(String className) throws Exception {
        return getClass("org.bukkit.craftbukkit." + API_VERSION + "." + className);
    }

}