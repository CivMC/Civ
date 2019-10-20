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
		double xd = player.getX() - bastionLocation.getX();
		double zd = player.getZ() - bastionLocation.getZ();
		double dist = Math.sqrt(xd*xd + zd*zd);		
		Vector vector = null;

		if (bastion.getType().isSquare()) {
			// get linear distance
			if (xd == 0.0 & zd == 0.0) {
				// pick a direction and _shove_
				xd = radius;
			} else {
				// make a unit triangle of same shape by dividing by longest side. Then multiply by radius to "shoveout".
				double ax = Math.abs(xd);
				double az = Math.abs(zd);
				if (ax > az) {
					xd = (xd / ax);
					zd = (zd / ax);
				} else {
					xd = (xd / az);
					zd = (zd / az);
				}
				double push = (radius * Math.sqrt(xd*xd + zd*zd)) - dist;
				// adjust pushout based on "unit" triangle to accomodate corners.
				// it will always be some factor between 1 and sqrt(2).
				xd *= push;
				zd *= push;
			}
			vector = new Vector(xd, 0.0, zd);	
		} else {
			// ez. straightline vector from radial distance.
			Location nP = player.clone().subtract(bastionLocation);
			vector = new Vector(nP.getX(), 0.0, nP.getZ());
			vector.normalize(); // points from Bastion to player.

			vector.multiply((radius - dist)); // pushout!
			System.out.println(vector.toString());
		}
		return vector;
	}
}
