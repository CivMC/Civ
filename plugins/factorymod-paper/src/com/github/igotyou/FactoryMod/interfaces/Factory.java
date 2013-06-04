package com.github.igotyou.FactoryMod.interfaces;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import java.util.List;
//original file:
/**
 * Machine.java
 * Purpose: An interface for machines to implement with basic functionality
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
//edited version:
/**
* Factory.java	 
* Purpose: An interface for factorys to implement with basic functionality
* @author igotyou
*/
public interface Factory
{
	/**
	 * Updates the machine
	 */
	public void update();
	
	public void destroy(Location destroyLocation);
	
	/**
	 * Powers on the machine
	 */
	public void powerOn();
	
	/**
	 * Powers off the machine
	 */
	public void powerOff();
	
	/**
	 * Toggles the current power state and returns interaction response
	 */
	public List<InteractionResponse> togglePower();
	public List<InteractionResponse> getChestResponse();
	public List<InteractionResponse> getCentralBlockResponse();
	
	/**
	 * Returns the location of the machine
	 */
	public Location getCenterLocation();
	
	public Location getInventoryLocation();
	
	public Location getPowerSourceLocation();
}