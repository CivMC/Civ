package net.civmc.kitpvp.warp;

import net.civmc.kitpvp.warp.command.WarpCommand;
import net.civmc.kitpvp.warp.command.WarpsCommand;
import net.civmc.kitpvp.warp.database.DatabaseManager;
import net.civmc.kitpvp.warp.handler.WarpHandler;
import net.civmc.kitpvp.warp.handler.WarpsHandler;
import net.civmc.kitpvp.warp.listener.PlayerRespawnListener;
import net.civmc.kitpvp.warp.util.Cache;
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
