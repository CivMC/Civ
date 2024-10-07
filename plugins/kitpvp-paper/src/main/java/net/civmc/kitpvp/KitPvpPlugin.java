package net.civmc.kitpvp;

import net.civmc.kitpvp.sql.SqlKitPvpDao;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class KitPvpPlugin extends ACivMod {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ManagedDatasource source = ManagedDatasource.construct(this, (DatabaseCredentials) getConfig().get("database"));
        getCommand("kit").setExecutor(new KitCommand(new SqlKitPvpDao(source)));
    }
}
