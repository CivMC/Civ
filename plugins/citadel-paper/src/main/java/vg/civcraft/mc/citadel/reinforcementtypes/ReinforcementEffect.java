package vg.civcraft.mc.citadel.reinforcementtypes;

import org.bukkit.Location;
import org.bukkit.Particle;
import vg.civcraft.mc.citadel.model.Reinforcement;

public class ReinforcementEffect {
	private Particle particle;
	private float offsetX;
	private float offsetY;
	private float offsetZ;
	private float speed;
	private int particleCount;

	public ReinforcementEffect(Particle particle, float offsetX, float offsetY, float offsetZ, float speed,
			int particleCount) {
		this.particle = particle;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.particleCount = particleCount;
	}

	/**
	 * 
	 * @return the amount to be randomly offset by in the X axis
	 */
	public float getOffsetX() {
		return offsetX;
	}

	/**
	 * 
	 * @return the amount to be randomly offset by in the Y axis
	 */
	public float getOffsetY() {
		return offsetY;
	}

	/**
	 * 
	 * @return the amount to be randomly offset by in the Z axis
	 */
	public float getOffsetZ() {
		return offsetZ;
	}

	/**
	 * 
	 * @return the type of particle used in this effect
	 */
	public Particle getParticle() {
		return particle;
	}

	/**
	 * 
	 * @return the amount of particle to display.
	 */
	public int getParticleCount() {
		return particleCount;
	}

	/**
	 * 
	 * @return the speed of the particles
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * Display an effect defined in the config around a reinforcement.
	 * 
	 * @param location the location of the reinforcement.
	 */
	public void playEffect(Location location) {
		location.getWorld().spawnParticle(particle, location, particleCount, offsetX, offsetY, offsetZ, speed, null);
	}
	
	/**
	 * Display an effect defined in the config around a reinforcement.
	 * 
	 * @param reinforcement Reinforcement to play effect around
	 */
	public void playEffect(Reinforcement reinforcement) {
		playEffect(reinforcement.getBlockCenter());
	}

	@Override
	public String toString() {
		return String.format(
				"  type: %s \n   offsetX: %f \n   offsetY: %f \n   offsetZ: %f \n   speed: %f \n   particleCount: %d",
				particle, offsetX, offsetY, offsetZ, speed, particleCount);
	}
}
