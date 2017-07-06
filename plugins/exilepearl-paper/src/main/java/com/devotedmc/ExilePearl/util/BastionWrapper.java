package com.devotedmc.ExilePearl.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import isaac.bastion.BastionBlock;

public class BastionWrapper {
	private BastionBlock bastion;
	
	public BastionWrapper(BastionBlock bastion) {
		this.bastion = bastion;
	}
	
	public BastionBlock getBastion() {
		return bastion;
	}
	
	public Vector getPushout(Location player, int KNOCKBACK) {
		Location bastionLocation = bastion.getLocation();
		if (!player.getWorld().getUID().equals(bastionLocation.getWorld().getUID())) return null; // what?
		double radius = bastion.getType().getEffectRadius() + (double) KNOCKBACK;
		
		Vector vector = null;
		
		if (bastion.getType().isSquare()) {
			// get linear distance
			double xd = bastionLocation.getX() - player.getX();
			double zd = bastionLocation.getZ() - player.getZ();
			if (xd == 0.0 & zd == 0.0) {
				// pick a direction and _shove_
				xd = radius;
			} else {
				// make a unit triangle of same shape by dividing by longest side. Then multiply by radius to "shoveout".
				double ax = Math.abs(xd);
				double az = Math.abs(zd);
				if (ax > az) {
					xd = (xd / ax) * radius;
					zd = (zd / ax) * radius;
				} else {
					xd = (xd / az) * radius;
					zd = (zd / az) * radius;
				}
			}
			vector = new Vector(xd, 0.0, zd);	
		} else {
			// ez. straightline vector from radial distance.
			vector = bastionLocation.subtract(player).getDirection(); // points from Bastion to player.
			vector.setY(0.0);

			double dist = bastionLocation.distance(player);
			vector.multiply((radius - dist)); // pushout!
		}
		return vector;
	}
}
