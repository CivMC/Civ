package com.devotedmc.testbukkit;

import static org.mockito.Mockito.mock;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.mockito.Mockito;

public abstract class TestBlockState implements BlockState {
	
	public TestBlock block;
	public MaterialData data;
	public byte lightLevel;

	public static TestBlockState create(TestBlock block) {
		TestBlockState bs = mock(TestBlockState.class, Mockito.CALLS_REAL_METHODS);
		bs.block = block;
		bs.data = new MaterialData(bs.getType());
		bs.lightLevel = 0;
		return bs;
	}
	
	private TestBlockState() { }
	
	@Override
    public Block getBlock() {
    	return block;
    }

	@Override
    public MaterialData getData() {
    	return data;
    }

	@Override
    public Material getType() {
    	return block.type;
    }
	
	@Override
    public byte getLightLevel() {
		return lightLevel;
	}
	
	@Override
    public World getWorld() {
		return block.getWorld();
	}
	
	@Override
    public int getX() {
		return block.getX();
	}
	
	@Override
    public int getY() {
		return block.getY();
	}
	
	@Override
    public int getZ() {
		return block.getZ();
	}
	
	@Override
    public Location getLocation() {
		return block.getLocation();
	}
	
	@Override
    public Location getLocation(Location loc) {
		if (loc == null) {
			return null;
		}
		
		Location l = block.getLocation();
		loc.setWorld(l.getWorld());
		loc.setX(l.getX());
		loc.setY(l.getY());
		loc.setZ(l.getZ());
		return loc;
	}
	
	@Override
    public Chunk getChunk() {
		return block.getLocation().getChunk();
	}
	
	@Override
    public void setData(MaterialData data) {
		this.data = data;
	}
	
	@Override
    public void setType(Material type) {
		block.setType(type);
	}
	
	@Override
    public boolean update() {
		return true;
	}
	
	@Override
    public boolean update(boolean force) {
		return true;
	}
	
	@Override
    public boolean update(boolean force, boolean applyPhysics) {
		return true;
	}
}
