package net.civmc.heliodor.heliodor.chunkmeta;

import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.heliodor.InfusionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableStorageEngine;
import java.util.ArrayList;
import java.util.List;

public class CauldronInfuseData extends TableBasedBlockChunkMeta<CauldronInfusion> {

    private final List<CauldronInfusion> infusions = new ArrayList<>();
    private final InfusionManager infusionManager;

    public CauldronInfuseData(boolean isNew, TableStorageEngine<CauldronInfusion> storage, InfusionManager infusionManager) {
        super(isNew, storage);
        this.infusionManager = infusionManager;
    }

    @Override
    public void handleChunkCacheReuse() {
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(HeliodorPlugin.class),
            () -> iterateAll(o -> {
                infusions.add((CauldronInfusion) o);
                if (!infusionManager.addInfusion((CauldronInfusion) o)) {
                    remove(o.getLocation());
                }
            }));
    }

    @Override
    public void handleChunkUnload() {
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(HeliodorPlugin.class),
            () -> {
                infusionManager.removeInfusions(infusions);
                infusions.clear();
            });
    }
}
