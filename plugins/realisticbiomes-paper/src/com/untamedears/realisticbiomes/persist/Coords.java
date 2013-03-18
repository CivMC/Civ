package com.untamedears.realisticbiomes.persist;

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
	
	@Override
	public int hashCode() {
		return (new Integer(w^((x^z)^y)).hashCode());
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
		return "["+w+", "+x+", "+y+", "+z+"]";
	}
}
