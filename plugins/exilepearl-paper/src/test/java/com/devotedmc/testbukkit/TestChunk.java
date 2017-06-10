package com.devotedmc.testbukkit;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;

public class TestChunk implements Chunk {
	
	public TestWorld world;
	public int X;
	public int Z;
	public boolean isLoaded;
	
	public static TestChunk create(TestWorld world, int X, int Z) {
		TestChunk c = new TestChunk();
		c.world = world;
		c.X = X;
		c.Z = Z;
		return c;
	}
	
	private TestChunk() { }
	
	@Override
	public int getX() {
    	return X;
    }

	@Override
    public int getZ() {
		return Z;
	}

	@Override
	public World getWorld() {
		return world;
	}
	
	public Block getBlock(int x, int y, int z) {
		return world.getBlockAt(x, y, z);
	}

	@Override
	public ChunkSnapshot getChunkSnapshot() {
		return null;
	}

	@Override
	public ChunkSnapshot getChunkSnapshot(boolean arg0, boolean arg1, boolean arg2) {
		return null;
	}

	@Override
	public Entity[] getEntities() {
		return new Entity[0];
	}

	@Override
	public BlockState[] getTileEntities() {
		return new BlockState[0];
	}

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public boolean load() {
		world.loadedChunks.add(this);
		isLoaded = true;
		return true;
	}

	@Override
	public boolean load(boolean arg0) {
		world.loadedChunks.add(this);
		isLoaded = true;
		return true;
	}

	@Override
	public boolean unload() {
		world.loadedChunks.remove(this);
		isLoaded = false;
		return true;
	}

	@Override
	public boolean unload(boolean arg0) {
		world.loadedChunks.remove(this);
		isLoaded = false;
		return true;
	}

	@Override
	public boolean unload(boolean arg0, boolean arg1) {
		world.loadedChunks.remove(this);
		isLoaded = false;
		return true;
	}

	@Override
	public boolean isSlimeChunk() {
		return false;
	}

}
