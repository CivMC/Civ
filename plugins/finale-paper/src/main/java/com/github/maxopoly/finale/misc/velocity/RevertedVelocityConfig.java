package com.github.maxopoly.finale.misc.velocity;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_13_R2.MathHelper;

public class RevertedVelocityConfig extends VelocityConfig {

	public RevertedVelocityConfig(double horizontal, double vertical, double power, double pitchOffset) {
		super(horizontal, vertical, power, pitchOffset);
	}

	private Random random = new Random();
	
	@Override
	public Vector modifyLaunchVelocity(Projectile proj, Player shooter) {
		Location eyeLoc = shooter.getEyeLocation();
		float fyaw = eyeLoc.getYaw();
		float fpitch = eyeLoc.getPitch();
		double yaw = (double) fyaw;
		double pitch = (double) fpitch;
		
		double yawRad = Math.toRadians(yaw);
		double pitchRad = Math.toRadians(pitch);
		double yPitchRad = Math.toRadians(pitch + getPitchOffset());
		
		double velX = -Math.sin(yawRad) * Math.cos(pitchRad) * getHorizontal();
		double velZ = Math.cos(yawRad) * Math.cos(pitchRad) * getHorizontal();
		double velY = -Math.sin(yPitchRad) * getVertical();
		
		Vector vel = new Vector(velX, velY, velZ);
		Vector norm = vel.normalize();
		
		double a = this.random.nextGaussian() * 0.007499999832361937D;
		Vector nextVec = norm.add(new Vector(a, a, a));
		Vector mulVec = nextVec.multiply(getPower());
		return mulVec;
	}
	
}
