package com.devotedmc.testbukkit;

import static org.mockito.Mockito.mock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.mockito.Mockito;

@SuppressWarnings("deprecation")
public abstract class TestBlock implements Block {

	public World world;
	public int X;
	public int Y;
	public int Z;
	public Material type;
	public TestBlockState state;
	
	public static TestBlock create(World world, int x, int y, int z, Material type) {
		TestBlock b = mock(TestBlock.class, Mockito.CALLS_REAL_METHODS);
		b.world = world;
		b.X = x;
		b.Y = y;
		b.Z = z;
		b.type = type;
		b.state = TestBlockState.create(b);
		return b;
	}
	
	public static TestBlock create(Location l, Material type) {
		return create(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), type);
	}
	
	private TestBlock() { }
	
	@Override
	public Material getType() {
		return type;
	}
	
	@Override
	public void setType(Material type) {
		this.type = type;
	}
	
	@Override
	public int getTypeId() {
		return type.getId();
	}
	
	@Override
	public int getX() {
		return X;
	}
	
	@Override
	public int getY() {
		return Y;
	}
	
	@Override
	public int getZ() {
		return Z;
	}
	
	@Override
	public Location getLocation() {
		return new Location(world, X, Y, Z);
	}
	
	@Override
	public boolean isEmpty() {
		return type == Material.AIR;
	}
	
	@Override
	public BlockState getState() {
		return this.state;
	}
}
