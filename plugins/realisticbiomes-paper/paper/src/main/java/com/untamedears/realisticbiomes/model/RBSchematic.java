package com.untamedears.realisticbiomes.model;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class RBSchematic {

	private BlockData[][][] blockData;
	private int xOffset;
	private int zOffset;
	private String name;

	public RBSchematic(String name, Clipboard weClipBoard) {
		this.blockData = convertWEClipBoard(weClipBoard);
		this.name = name;
		this.xOffset = blockData.length / 2;
		this.zOffset = blockData[0][0].length / 2;
	}
	
	public String getName() {
		return name;
	}

	public void spawnAt(Location loc) {
		int xCap = blockData.length;
		int yCap = blockData[0].length;
		int zCap = blockData[0][0].length;
		int localxOffset = loc.getBlockX() - xOffset;
		int localyOffset = loc.getBlockY();
		int localzOffset = loc.getBlockZ() - zOffset;
		for (int x = 0; x < xCap; x++) {
			for (int y = 0; y < yCap; y++) {
				for (int z = 0; z < zCap; z++) {
					loc.getWorld().getBlockAt(x + localxOffset, y + localyOffset, z + localzOffset)
							.setBlockData(blockData[x][y][z]);
				}
			}
		}
	}

	private static BlockData[][][] convertWEClipBoard(Clipboard weClipBoard) {
		BlockVector3 size = weClipBoard.getDimensions();
		BlockData[][][] result = new BlockData[size.getX()][size.getY()][size.getZ()];
		for (int x = 0; x < size.getX(); x++) {
			for (int y = 0; y < size.getY(); y++) {
				for (int z = 0; z < size.getZ(); z++) {
					result[x][y][z] = BukkitAdapter.adapt(weClipBoard.getBlock(BlockVector3.at(x, y, z)));
				}
			}
		}
		return result;
	}

}
