package com.github.maxopoly.finale.misc.velocity;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public class NormalVelocityConfig extends VelocityConfig {

	public NormalVelocityConfig(double horizontal, double vertical, double power, double pitchOffset) {
		super(horizontal, vertical, power, pitchOffset);
	}

	@Override
	public Vector modifyLaunchVelocity(Projectile proj, Player shooter) {
		return proj.getVelocity().multiply(getPower());
	}

	
}
