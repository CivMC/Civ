package net.minelink.ctplus.util;

import me.confuser.barapi.BarAPI;

import java.util.HashMap;
import java.util.Map;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.google.common.base.Preconditions;

import net.minelink.ctplus.CombatTagPlus;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.bossbar.BossBarAPI;

public final class BarUtils {

    private static final CombatTagPlus plugin = CombatTagPlus.getPlugin(CombatTagPlus.class);
    private static Handler handler;

    public static void init() {
        if (ReflectionUtils.getClass("org.bukkit.boss.BossBar") != null) {
            handler = Handler.NEW_BUKKIT_API;
        } else if (Bukkit.getPluginManager().isPluginEnabled("ActionBarAPI")) {
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
        NEW_BUKKIT_API {
            private final Map<Player, BossBar> bars = new HashMap<>();

            @Override
            public boolean hasBar(Player player) {
                return bars.get(player) != null;
            }

            @Override
            public void setMessage(final Player player, String message, int timeout) {
                setMessage(player, message, 100F);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        removeBar(player);
                    }
                }.runTaskLater(plugin, 20 * timeout);
            }

            @Override
            public void setMessage(Player player, String message, float percent) {
                Preconditions.checkArgument(percent <= 100, "Invalid percentage: %s", percent);
                BossBar bar = bars.get(player);
                if (bar == null) {
                    bar = Bukkit.createBossBar(message, BarColor.RED, BarStyle.SOLID);
                    bars.put(player, bar);
                    bar.addPlayer(player);
                } else {
                    bar.setTitle(message);
                }
                bar.setVisible(false);
                bar.setProgress(percent / 100.0);
                bar.setVisible(true);
            }

            @Override
            public void removeBar(Player player) {
                BossBar bar = bars.remove(player);
                if (bar != null) {
                    bar.removePlayer(player);
                }
            }
        },
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
