package com.github.igotyou.FactoryMod.interfaces;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
	public HashMap<Integer, ItemStack> getBuildMaterials() ;
}