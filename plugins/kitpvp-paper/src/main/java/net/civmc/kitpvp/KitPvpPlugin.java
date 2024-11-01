package net.civmc.kitpvp;

import java.sql.SQLException;
import net.civmc.kitpvp.anvil.AnvilGui;
import net.civmc.kitpvp.arena.ArenaCleaner;
import net.civmc.kitpvp.arena.ArenaCommand;
import net.civmc.kitpvp.arena.ArenaManager;
import net.civmc.kitpvp.arena.MysqlLoader;
import net.civmc.kitpvp.arena.RespawnListener;
import net.civmc.kitpvp.arena.data.SqlArenaDao;
import net.civmc.kitpvp.command.ClearCommand;
import net.civmc.kitpvp.command.KitCommand;
import net.civmc.kitpvp.snapshot.DeathListener;
import net.civmc.kitpvp.snapshot.InventorySnapshotManager;
import net.civmc.kitpvp.snapshot.ViewInventorySnapshotCommand;
import net.civmc.kitpvp.spawn.SetSpawnCommand;
import net.civmc.kitpvp.spawn.SpawnCommand;
import net.civmc.kitpvp.spawn.SpawnListener;
import net.civmc.kitpvp.spawn.SpawnProvider;
import net.civmc.kitpvp.spawn.SqlSpawnProvider;
import net.civmc.kitpvp.sql.SqlKitPvpDao;
import net.civmc.kitpvp.warp.WarpMain;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class KitPvpPlugin extends ACivMod {

    private ManagedDatasource source;

    @Override
    public void onEnable() {
        AnvilGui anvilGui = new AnvilGui();
        getServer().getPluginManager().registerEvents(anvilGui, this);

        saveDefaultConfig();
        DatabaseCredentials credentials = (DatabaseCredentials) getConfig().get("database");
        source = ManagedDatasource.construct(this, credentials);
        getCommand("kit").setExecutor(new KitCommand(new SqlKitPvpDao(source), anvilGui));
        WarpMain warpMain = new WarpMain(this, source);
        getCommand("clear").setExecutor(new ClearCommand());

        InventorySnapshotManager inventorySnapshotManager = new InventorySnapshotManager();
        getServer().getPluginManager().registerEvents(new DeathListener(inventorySnapshotManager), this);
        getCommand("viewinventorysnapshot").setExecutor(new ViewInventorySnapshotCommand(inventorySnapshotManager));

        if (Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            getServer().getPluginManager().registerEvents(new DrunkDeathListener(), this);
        }

        SpawnProvider spawnProvider = new SqlSpawnProvider(source);
        getCommand("spawn").setExecutor(new SpawnCommand(spawnProvider));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(spawnProvider));
        getServer().getPluginManager().registerEvents(new SpawnListener(spawnProvider), this);

        try {
            ArenaManager manager = new ArenaManager(this, spawnProvider, new MysqlLoader(source));
            getCommand("arena").setExecutor(new ArenaCommand(this, new SqlArenaDao(source), manager));
            getServer().getPluginManager().registerEvents(new RespawnListener(manager), this);
            Bukkit.getScheduler().runTaskTimer(this, new ArenaCleaner(manager), 20 * 60, 20 * 60);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        source.updateDatabase();
    }

    @Override
    public void onDisable() {
        this.source.close();
    }
}
