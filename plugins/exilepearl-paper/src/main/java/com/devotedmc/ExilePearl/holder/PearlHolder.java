package com.devotedmc.ExilePearl.holder;

import org.bukkit.Location;

import com.devotedmc.ExilePearl.ExilePearl;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;

/**
 * Interface for an object that can hold or contain an exile pearl
 * @author Gordon
 *
 */
public interface PearlHolder {

	/**
	 * Gets the holder name
	 * @return The holder name
	 */
	public String getName();

	/**
	 * Gets the holder location
	 * @return The holder location
	 */
	public Location getLocation();

	/**
	 * Validate that the given pearl still exists in this holder instance
	 * @param pearl The exile pearl
	 * @return the validation result
	 */
	public HolderVerifyResult validate(final ExilePearl pearl);

	/**
	 * Gets whether the holder is a block
	 * @return true if the holder is a block
	 */
	public boolean isBlock();

	/**
	 * Gets whether the holder is in a loaded chunk
	 * @return true if the holder is in a loaded chunk
	 */
	public default boolean inLoadedChunk() {
		Location loc = getLocation();
		World world = loc.getWorld();

		if (world == null) {
			return false;
		}

		return world.isChunkLoaded(BlockBasedChunkMeta.toChunkCoord(loc.getBlockX()), BlockBasedChunkMeta.toChunkCoord(loc.getBlockZ()));
	}
}
