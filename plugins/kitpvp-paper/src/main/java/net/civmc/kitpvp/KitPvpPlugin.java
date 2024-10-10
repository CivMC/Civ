package net.civmc.kitpvp;

import net.civmc.kitpvp.command.ClearCommand;
import net.civmc.kitpvp.command.KitCommand;
import net.civmc.kitpvp.snapshot.DeathListener;
import net.civmc.kitpvp.snapshot.InventorySnapshotManager;
import net.civmc.kitpvp.snapshot.ViewInventorySnapshotCommand;
import net.civmc.kitpvp.sql.SqlKitPvpDao;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class KitPvpPlugin extends ACivMod {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ManagedDatasource source = ManagedDatasource.construct(this, (DatabaseCredentials) getConfig().get("database"));
        getCommand("kit").setExecutor(new KitCommand(new SqlKitPvpDao(source)));
        getCommand("clear").setExecutor(new ClearCommand());

        InventorySnapshotManager inventorySnapshotManager = new InventorySnapshotManager();
        getServer().getPluginManager().registerEvents(new DeathListener(inventorySnapshotManager), this);
        getCommand("viewinventorysnapshot").setExecutor(new ViewInventorySnapshotCommand(inventorySnapshotManager));



        if (Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            getServer().getPluginManager().registerEvents(new DrunkDeathListener(), this);
        }
    }
}
