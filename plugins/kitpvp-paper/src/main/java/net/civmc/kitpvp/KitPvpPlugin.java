package net.civmc.kitpvp;

import net.civmc.kitpvp.command.ClearCommand;
import net.civmc.kitpvp.command.KitCommand;
import net.civmc.kitpvp.sql.SqlKitPvpDao;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return new ChunkGenerator() {

        };
    }
}
