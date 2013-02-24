package com.github.igotyou.FactoryMod.interfaces;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.utility.InteractionResponse;
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
* Purpose: An interface for machines to implement with basic functionality
* @author igotyou
*
*/
public interface Factory
{
	/**
	 * Updates the machine
	 */
	public void update();
	
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
	public InteractionResponse togglePower();
	
	/**
	 * Returns the location of the machine
	 */
	public Location getCenterLocation();
	
	public Location getInventoryLocation();
	
	public Location getPowerSourceLocation();
}