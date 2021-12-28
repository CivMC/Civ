package vg.civcraft.mc.civmodcore.world.model;

import org.bukkit.Chunk;
import org.bukkit.World;

public class PseudoChunk {

	private World world;

	private int x;

	private int z;

	public PseudoChunk(World w, int x, int z) {
		this.world = w;
		this.x = x;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public World getWorld() {
		return world;
	}

	public int getActualX() {
		return x * 16;
	}

	public int getActualZ() {
		return z * 16;
	}

	public Chunk getActualChunk() {
		return world.getChunkAt(x, z);
	}

}
