package dev.drekamor.warp;

import dev.drekamor.warp.command.WarpCommand;
import dev.drekamor.warp.command.WarpsCommand;
import dev.drekamor.warp.database.DatabaseManager;
import dev.drekamor.warp.handler.WarpHandler;
import dev.drekamor.warp.handler.WarpsHandler;
import dev.drekamor.warp.listener.PlayerRespawnListener;
import dev.drekamor.warp.util.Cache;
import net.civmc.kitpvp.KitPvpPlugin;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class WarpMain {
    private final KitPvpPlugin plugin;

    private DatabaseManager databaseManager;
    private Cache cache;
    private WarpsHandler warpsHandler;
    private WarpHandler warpHandler;

    public WarpMain(KitPvpPlugin plugin, ManagedDatasource datasource) {
        this.plugin = plugin;

        databaseManager = new DatabaseManager(plugin, datasource);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                cache = new Cache(databaseManager.getWarpNames(), databaseManager.getWarps());
            }
        });

        warpsHandler = new WarpsHandler(this, plugin);
        warpHandler = new WarpHandler(this, plugin);

        plugin.getCommand("warps").setExecutor(new WarpsCommand(this.warpsHandler));
        plugin.getCommand("dev/drekamor/warp").setExecutor(new WarpCommand(this.warpHandler));

        plugin.getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), plugin);
    }

    public Cache getCache() {
        return cache;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
