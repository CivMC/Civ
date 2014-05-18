package com.untamedears.realisticbiomes.persist;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.block.Block;

public class Coords {
	public int w;
	public int x;
	public int y;
	public int z;
	
	public Coords(int w, int x, int y, int z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Coords(Block block) {
		this.w = WorldID.getPID(block.getWorld().getUID());
		this.x = block.getX();
		this.y = block.getY();
		this.z = block.getZ();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(157, 13).append(w).append(x).append(y).append(z).toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		
		Coords coords = (Coords)obj;
		if (w == coords.w && x == coords.x && coords.z ==z && y == coords.y)
			return true;
		
		return false;
	}
	
	public String toString() {
		return "<Coords [w: "+w+", x:"+x+", y: "+y+", z: "+z+"]>";
	}
}
