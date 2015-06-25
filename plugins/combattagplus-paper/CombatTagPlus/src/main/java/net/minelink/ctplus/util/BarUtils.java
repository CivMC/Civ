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

    public static void setMessage(Player player, String message) {
        if (handler != null) {
            handler.setMessage(player, message);
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
            public void setMessage(Player player, String message) {
                ActionBarAPI.sendActionBar(player, message);
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
            public void setMessage(Player player, String message) {
                BarAPI.setMessage(player, message);
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
            public void setMessage(Player player, String message) {
                BossBarAPI.setMessage(player, message);
            }

            @Override
            public void setMessage(Player player, String message, int timeout) {
                BossBarAPI.setMessage(player, message, timeout);
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

        public abstract void setMessage(Player player, String message);
        public abstract void setMessage(Player player, String message, int timeout);
        public abstract void setMessage(Player player, String message, float percent);
        public abstract void removeBar(Player player);
    }

}
