package net.minelink.ctplus.util;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.inventivetalent.bossbar.BossBarAPI;

public final class BarUtils {

    private static Handler handler;

    public static void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("ActionBarAPI")) {
            handler = Handler.ACTIONBAR_API;
        } else if (Bukkit.getPluginManager().isPluginEnabled("BarAPI")) {
            handler = Handler.CONFUSER_BAR_API;
        } else if (Bukkit.getPluginManager().isPluginEnabled("BossBarAPI")) {
            handler = Handler.BOSSBAR_API;
        }
    }

    public static boolean hasBar(Player player) {
        return handler != null && handler.hasBar(player);
    }

    public static void setMessage(Player player, String message, int timeout) {
        if (handler != null) {
            handler.setMessage(player, message, timeout);
        }
    }


    public static void setMessage(Player player, String message, float percent) {
        if (handler != null) {
            handler.setMessage(player, message, percent);
        }
    }

    public static void removeBar(Player player) {
        if (handler != null) {
            handler.removeBar(player);
        }
    }

    private enum Handler {
        ACTIONBAR_API {
            @Override
            public boolean hasBar(Player player) {
                return false;
            }

            @Override
            public void setMessage(Player player, String message, int timeout) {
                ActionBarAPI.sendActionBar(player, message);
            }

            @Override
            public void setMessage(Player player, String message, float percent) {
                ActionBarAPI.sendActionBar(player, message);
            }

            @Override
            public void removeBar(Player player) {

            }
        },

        CONFUSER_BAR_API {
            @Override
            public boolean hasBar(Player player) {
                return BarAPI.hasBar(player);
            }

            @Override
            public void setMessage(Player player, String message, int timeout) {
                BarAPI.setMessage(player, message, timeout);
            }

            @Override
            public void setMessage(Player player, String message, float percent) {
                BarAPI.setMessage(player, message, percent);
            }

            @Override
            public void removeBar(Player player) {
                BarAPI.removeBar(player);
            }
        },

        BOSSBAR_API {
            @Override
            public boolean hasBar(Player player) {
                return BossBarAPI.hasBar(player);
            }

            @Override
            public void setMessage(Player player, String message, int timeout) {
                BossBarAPI.setMessage(player, message, 100, timeout);
            }

            @Override
            public void setMessage(Player player, String message, float percent) {
                BossBarAPI.setMessage(player, message);
                BossBarAPI.setHealth(player, percent);
            }

            @Override
            public void removeBar(Player player) {
                BossBarAPI.removeBar(player);
            }
        };

        public abstract boolean hasBar(Player player);
        public abstract void setMessage(Player player, String message, int timeout);
        public abstract void setMessage(Player player, String message, float percent);
        public abstract void removeBar(Player player);
    }

}
