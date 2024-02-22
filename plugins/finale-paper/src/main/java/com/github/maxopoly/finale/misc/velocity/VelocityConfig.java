package com.github.maxopoly.finale.misc.velocity;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public abstract class VelocityConfig {
	
	public enum Type {
		REVERTED, NORMAL;
	}

	private double horizontal;
	private double vertical;
	private double power;
	private double pitchOffset;
	
	public VelocityConfig(double horizontal, double vertical, double power, double pitchOffset) {
		this.horizontal = horizontal;
		this.vertical = vertical;
		this.power = power;
		this.pitchOffset = pitchOffset;
	}
	
	public abstract Vector modifyLaunchVelocity(Projectile proj, Player shooter);

	public double getHorizontal() {
		return horizontal;
	}

	public double getVertical() {
		return vertical;
	}

	public double getPower() {
		return power;
	}

	public double getPitchOffset() {
		return pitchOffset;
	}
	
	public static VelocityConfig createVelocityConfig(Type type, double horizontal, double vertical,double power, double pitchOffset) {
		return (type == Type.REVERTED) ? new RevertedVelocityConfig(horizontal, vertical, power, pitchOffset) 
				: new NormalVelocityConfig(horizontal, vertical, power, pitchOffset);
	}
	
}
