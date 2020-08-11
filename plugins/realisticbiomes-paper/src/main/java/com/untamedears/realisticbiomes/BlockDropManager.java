package com.untamedears.realisticbiomes;

import org.bukkit.Location;

import com.untamedears.realisticbiomes.model.gauss.drop.BlockDrop;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.auto.YamlStorageEngine;

public class BlockDropManager {

	private final ManagedDatasource db;
	private BlockBasedChunkMetaView<BlockBasedChunkMeta<BlockDrop, YamlStorageEngine<BlockDrop>>, BlockDrop, YamlStorageEngine<BlockDrop>> api;

	public BlockDropManager(ManagedDatasource db) {
		this.db = db;
	}

	public boolean setup() {
		RealisticBiomes plugin = RealisticBiomes.getInstance();
		YamlStorageEngine<BlockDrop> dropDao = new YamlStorageEngine<>(db, plugin.getLogger(), BlockDrop::deserialize);
		api = ChunkMetaAPI
				.registerAutoBlockBasedPlugin(plugin, "rb_drops", dropDao, false);
		return api != null;
	}
	
	public void shutdown() {
		api.disable();
	}

	public void setDrops(BlockDrop drop) {
		api.put(drop);
	}
	
	public BlockDrop getDrops(Location location) {
		return api.get(location);
	}
	
	public void removeDrops(Location location) {
		api.remove(location);
	}
	
	public BlockDrop getAndRemove(Location location) {
		return api.remove(location);
	}

}
