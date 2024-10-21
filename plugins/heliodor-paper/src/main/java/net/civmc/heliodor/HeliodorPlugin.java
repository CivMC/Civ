package net.civmc.heliodor;

import net.civmc.heliodor.heliodor.InfusionListener;
import net.civmc.heliodor.heliodor.InfusionManager;
import net.civmc.heliodor.heliodor.chunkmeta.CauldronDao;
import net.civmc.heliodor.heliodor.chunkmeta.CauldronInfuseData;
import net.civmc.heliodor.heliodor.chunkmeta.CauldronInfusion;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableStorageEngine;
import java.util.function.Supplier;

public class HeliodorPlugin extends ACivMod {

    private BlockBasedChunkMetaView<CauldronInfuseData, TableBasedDataObject, TableStorageEngine<CauldronInfusion>> chunkMetaView;

    @Override
    public void onEnable() {
        super.onEnable();

        saveDefaultConfig();

        ManagedDatasource database = ManagedDatasource.construct(this, (DatabaseCredentials) getConfig().get("database"));

        InfusionManager infusionManager = new InfusionManager();
        CauldronDao dao = new CauldronDao(this.getLogger(), database, infusionManager);
        if (!dao.updateDatabase()) {
            Bukkit.shutdown();
            return;
        }
        Supplier<CauldronInfuseData> newData = () -> new CauldronInfuseData(false, dao, infusionManager);
        this.chunkMetaView = ChunkMetaAPI.registerBlockBasedPlugin(this, newData, dao, true);

        getServer().getPluginManager().registerEvents(new InfusionListener(infusionManager, chunkMetaView), this);
    }

    public BlockBasedChunkMetaView<CauldronInfuseData, TableBasedDataObject, TableStorageEngine<CauldronInfusion>> getChunkMetaView() {
        return chunkMetaView;
    }
}
