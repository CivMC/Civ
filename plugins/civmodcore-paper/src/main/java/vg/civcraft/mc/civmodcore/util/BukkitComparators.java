package vg.civcraft.mc.civmodcore.util;

import java.util.Comparator;

import org.bukkit.Location;

public class BukkitComparators {
	
	private BukkitComparators() {
		
	}
	
	public static Comparator<Location> getLocation() {
		return (l1, l2) -> {
			int worldComp = l1.getWorld().getUID().compareTo(l2.getWorld().getUID());
			if (worldComp != 0) {
				return worldComp;
			}
			int xComp = Double.compare(l1.getX(), l2.getX());
			if (xComp != 0) {
				return xComp;
			}
			int zComp = Double.compare(l1.getZ(), l2.getZ());
			if (zComp != 0) {
				return zComp;
			}
			return Double.compare(l1.getY(), l2.getY());
		}; 
	}

}
