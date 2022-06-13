package com.untamedears.realisticbiomes.model;

import com.untamedears.realisticbiomes.GrowthConfigManager;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import org.bukkit.Location;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.files.FileCacheSerializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlantSerializer extends FileCacheSerializer<Plant> {
	private final GrowthConfigManager growthConfigManager;

	public PlantSerializer(GrowthConfigManager growthConfigManager) {
		this.growthConfigManager = growthConfigManager;
	}

	@Override
	public void serialize(DataOutputStream stream, Plant object) throws IOException {
		Location location = object.getLocation();
		byte xOffset = (byte)BlockBasedChunkMeta.modulo(location.getBlockX());
		short y = (short)location.getBlockY();
		byte zOffset = (byte)BlockBasedChunkMeta.modulo(location.getBlockZ());
		short configId = object.getGrowthConfig() != null ? object.getGrowthConfig().getID() : 0;

		stream.writeByte(xOffset);
		stream.writeShort(y);
		stream.writeByte(zOffset);
		stream.writeShort(configId);
		stream.writeLong(object.getCreationTime());
	}

	@Override
	public Plant deserialize(DataInputStream stream, World world, int chunkXOffset, int chunkZOffset) throws IOException {
		int xOffset = stream.readByte();
		int y = stream.readShort();
		int zOffset = stream.readByte();
		short configId = stream.readShort();
		long creationTime = stream.readLong();

		Location location = new Location(world, chunkXOffset + xOffset, y, chunkZOffset + zOffset);
		PlantGrowthConfig growthConfig = configId != 0 ? this.growthConfigManager.getConfigById(configId) : null;

		return new Plant(creationTime, location, false, growthConfig);
	}
}
