package com.github.igotyou.interfaces;

import java.util.HashMap;

import org.bukkit.Material;

//original file:
/**
 * Properties.java
 * Purpose: Interface for Properties objects for basic properties functionality
 *
 * @author MrTwiggy
 * @version 0.1 1/17/13
 */
//edited version:
/**
* Properties.java	 
 * Purpose: Interface for Properties objects for basic properties functionality
* @author igotyou
*
*/
public interface Properties 
{
	/**
	 * Returns the amount of upgrade materials required to reach this tier
	 */
	public HashMap<Integer,Integer> getBuildAmount();
	
	/**
	 * Returns the material used to upgrade to this tier
	 */
	public HashMap<Integer,Material> getBuildMaterial();
}