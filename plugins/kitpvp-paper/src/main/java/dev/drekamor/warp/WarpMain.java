package dev.drekamor.warp;

import dev.drekamor.warp.command.WarpCommand;
import dev.drekamor.warp.command.WarpsCommand;
import dev.drekamor.warp.database.DatabaseManager;
import dev.drekamor.warp.handler.WarpHandler;
import dev.drekamor.warp.handler.WarpsHandler;
import dev.drekamor.warp.listener.PlayerRespawnListener;
import dev.drekamor.warp.util.Cache;
import net.civmc.kitpvp.KitPvpPlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class WarpMain {

    private final DatabaseManager databaseManager;

    public WarpMain(KitPvpPlugin plugin, ManagedDatasource datasource) {

        databaseManager = new DatabaseManager(plugin, datasource);
        Cache.initialiseCache(databaseManager.getWarpNames(), databaseManager.getWarps());

        WarpsHandler warpsHandler = new WarpsHandler(this, plugin);
        WarpHandler warpHandler = new WarpHandler(this, plugin);

        plugin.getCommand("warps").setExecutor(new WarpsCommand(warpsHandler));
        plugin.getCommand("warp").setExecutor(new WarpCommand(warpHandler));

        plugin.getServer().getPluginManager().registerEvents(new PlayerRespawnListener(), plugin);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
