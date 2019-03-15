package com.untamedears.realisticbiomes.persist;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Chunk;

public class ChunkCoords {
	public final int w;
	public final int x;
	public final int z;
	
	public ChunkCoords(int w, int x, int z) {
		this.w = w;
		this.x = x;
		this.z = z;
	}
	
	public ChunkCoords(Chunk chunk) {
		this.w = WorldID.getPID(chunk.getWorld().getUID());
		this.x = chunk.getX();
		this.z = chunk.getZ();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(157, 13).append(w).append(x).append(z).toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		
		ChunkCoords coords = (ChunkCoords)obj;
		if (w == coords.w && x == coords.x && coords.z ==z)
			return true;
		
		return false;
	}
	
	public String toString() {
		return String.format("<ChunkCoords [World: %d,  X: %d, Z: %d ]", w, x, z);
	}
}
