package vg.civcraft.mc.citadel.reinforcement;

import org.bukkit.Location;
import org.bukkit.Material;

public class Reinforcement {

	private int creation;
	private Location loc;
	private Material mat;
	private int dur;
	
	public Reinforcement(Location loc, Material mat, int dur, int creation){
		this.loc = loc;
		this.mat = mat;
		this.dur = dur;
		this.creation = creation;
	}
	/**
	 * Sets the durability of a reinforcement.
	 * @param The int of the durability.
	 */
	public void setDurability(int dur){
		this.dur = dur;
	}
	/**
	 * @return Returns what the current durability is.
	 */
	public int getDurability(){
		return dur;
	}
	/**
	 * @return Returns the material of the ReinforcementMaterial.
	 */
	public Material getMaterial(){
		return mat;
	}
	/**
	 * @return Return the location of the Reinforcement.
	 */
	public Location getLocation(){
		return loc;
	}
	/**
	 * @return Returns the time that this reinforcement was created or 0 if it is mature.
	 */
	public int getMaturationTime(){
		return creation;
	}
	/**
	 * Sets the maturation time of this reinforcement.
	 * @param The time in seconds it was created.
	 */
	public void setMaturationTime(int time){
		creation = time;
	}
}
