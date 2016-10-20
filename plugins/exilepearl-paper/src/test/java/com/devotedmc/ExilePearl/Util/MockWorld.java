package com.devotedmc.ExilePearl.Util;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.mockito.Mockito;

@SuppressWarnings("deprecation")
public abstract class MockWorld implements World {

	public String name;
	public UUID uid;
	public Environment env;
	public WorldType worldType;
	public File worldFolder;
	
	private HashMap<Location, Block> blocks;
	public HashSet<MockChunk> loadedChunks;
	
	public static MockWorld create(String name, Environment env, WorldType type) {
		MockWorld world = mock(MockWorld.class, Mockito.CALLS_REAL_METHODS);
		world.name = name;
		world.uid = UUID.randomUUID();
		world.env = env;
		world.worldType = type;
		world.blocks = new HashMap<Location, Block>();
		world.loadedChunks = new HashSet<MockChunk>();
		return world;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public UUID getUID() {
		return uid;
	}
	
	@Override
	public Environment getEnvironment() {
		return env;
	}
	
	@Override
	public WorldType getWorldType() {
		return worldType;
	}
	
	@Override
	public File getWorldFolder() {
		return worldFolder;
	}
	
	@Override
	public Block getBlockAt(Location l) {
		Block b = blocks.get(l);
		
		if (b == null) {
            Material blockType = Material.AIR;
            if (l.getBlockY() < 64) {
                blockType = Material.DIRT;
            }
            
            b  = MockBlock.create(l, blockType);
		}
		
		return b;
	}
	
	@Override
	public Block getBlockAt(int x, int y, int z) {
		return getBlockAt(new Location(this, x, y, z));
	}
	
	@Override
	public Chunk getChunkAt(Location l) {
		return MockChunk.create((MockWorld)l.getWorld(), l.getBlockX() >> 4, l.getBlockZ() >> 4);
	}
	
	@Override
	public Chunk getChunkAt(Block b) {
		return getChunkAt(b.getLocation());
	}
	
	@Override
	public Chunk getChunkAt(int x, int z) {
		return MockChunk.create(this, x, z);
	}
	
	@Override
	public Chunk[] getLoadedChunks() {
		Chunk[] chunks = new Chunk[this.loadedChunks.size()];
		
		int i = 0;
		for (Chunk c : loadedChunks) {
			chunks[i++] = c;
		}
		
		return chunks;
	}

	@Override
	public int getHighestBlockYAt(int x, int z) {
		return 63;
	}
	
	@Override
	public int getMaxHeight() {
		if (env == Environment.NETHER) {
			return 125;
		}
		return 255;
	}

	@Override
	public int getBlockTypeIdAt(int x, int y, int z) {
		return getBlockAt(x, y, z).getTypeId();
	}
	
	
	public MockBlock addBlock(MockBlock b) {
		blocks.put(b.getLocation(), b);
		return b;
	}
	
	public MockBlock removeBlock(MockBlock b) {
		blocks.remove(b.getLocation(), b);
		return b;
	}
}
