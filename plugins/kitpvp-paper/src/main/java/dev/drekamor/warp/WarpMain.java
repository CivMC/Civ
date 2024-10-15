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

    public WarpMain(KitPvpPlugin plugin, ManagedDatasource datasource) {
        DatabaseManager databaseManager = new DatabaseManager(plugin, datasource);
        Cache cache = new Cache(databaseManager.getWarpNames(), databaseManager.getWarps());

        WarpsHandler warpsHandler = new WarpsHandler(plugin, databaseManager, cache);
        WarpHandler warpHandler = new WarpHandler(plugin, cache);

        plugin.getCommand("warps").setExecutor(new WarpsCommand(warpsHandler));
        plugin.getCommand("warp").setExecutor(new WarpCommand(warpHandler));

        plugin.getServer().getPluginManager().registerEvents(new PlayerRespawnListener(cache), plugin);
    }
}
