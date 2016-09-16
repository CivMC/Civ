package vg.civcraft.mc.citadel.reinforcementtypes;

import org.bukkit.Effect;
import org.bukkit.Location;

public class ReinforcementEffect {
	private Effect effect;
	private int id;
	private int data;
	private float offsetX;
	private float offsetY;
	private float offsetZ;
	private float speed;
	private int particleCount; 
	private int viewDistance;
	
	public ReinforcementEffect(Effect effect, int id, int data, float offsetX, float offsetY, float offsetZ,
			float speed, int particleCount, int viewDistance) {
		this.effect = effect;
		this.id = id;
		this.data = data;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.particleCount = particleCount;
		this.viewDistance = viewDistance;
	}

	/**
	 * 
	 * @return the type of particle used in this effect
	 */
	public Effect getEffect() {
		return effect;
	}
	
	/**
	 * 
	 * @return the item/block/data id for the effect
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @return the data value of the block/item for the effect
	 */
	public int getData() {
		return data;
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
	 * @return the speed of the particles
	 */
	public float getSpeed() {
		return speed;
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
	 * @return the distance from which players will be able to see the effect. 
	 */
	public int getViewDistance() {
		return viewDistance;
	}
	
	/**
	 * Display an effect defined in the config around a reinforcement.
	 * @param location the location of the reinforcement.
	 */
	public void playEffect(Location location){
		location.getWorld().spigot().playEffect(location, effect, id, data, offsetX, offsetY, offsetZ, speed, particleCount, viewDistance);
	}
	
	public String toString() {
		return String.format(
				"  type: %s \n   id: %d \n   data: %d \n   offsetX: %f \n   offsetY: %f \n   offsetZ: %f \n   speed: %f \n   particleCount: %d \n   viewDistance: %d",
				effect, id, data, offsetX, offsetY, offsetZ, speed, particleCount, viewDistance);
	}
}
