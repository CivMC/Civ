package vg.civcraft.mc.citadel.reinforcement;

import org.bukkit.Location;
import org.bukkit.Material;

public class Reinforcement {

	private int creation;
	private int acid;
	private Location loc;
	private Material mat;
	private int dur;
	protected boolean isDirty;
	
	public Reinforcement(Location loc, Material mat, int dur, int creation, int acid){
		this.loc = loc;
		this.mat = mat;
		this.dur = dur;
		this.creation = creation;
		this.acid = acid;
		isDirty = false;
	}
	/**
	 * Sets the durability of a reinforcement.
	 * @param The int of the durability.
	 */
	public void setDurability(int dur){
		this.dur = dur;
		isDirty = true;
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
	 * @param The time in minutes it was created.
	 */
	public void setMaturationTime(int time){
		creation = time;
		isDirty = true;
	}
	/**
	 * @return Returns the time that this acid process began or 0 if not acid/done.
	 */
	public int getAcidTime(){
		return acid;
	}
	/**
	 * Sets the acid process time of this reinforcement (0 to indicate done/not acid).
	 * @param The time in minutes acid process began.
	 */
	public void setAcidTime(int acid) {
		this.acid = acid;
		isDirty = true;
	}
	/**
	 * @return Returns if this reinforcement needs to be saved.
	 */
	public boolean isDirty(){
		return isDirty;
	}
	/**
	 * Sets if this reinforcement needs to be saved or not.
	 * @param dirty
	 */
	public void setDirty(boolean dirty){
		isDirty = dirty;
	}
	
	public Material getType() {
		return this.mat;
	}
}
