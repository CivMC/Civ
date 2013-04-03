package com.github.igotyou.FactoryMod.interfaces;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.utility.InteractionResponse;

//original file:
/**
* Manager.java
* Purpose: Interface for Manager objects for basic manager functionality
*
* @author MrTwiggy
* @version 0.1 1/08/13
*/
//edited version:
/**
* Manager.java	 
* Purpose: Interface for Manager objects for basic manager functionality
* @author igotyou
*
*/

public interface Manager
{

/**
* Saves the machine objects list of this manager to file
*/
	public void save(File file) throws IOException;

/**
* Loads machine objects list of this manager from file
*/
	public void load(File file) throws IOException;

/**
* Updates all the machines from this manager's machine object list
*/
	public void updateFactorys();

/**
* Attempts to create a new machine for this manager
*/
	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerLocation);

/**
* Creates a machine from an existing machine data object
*/
	public InteractionResponse addFactory(Factory factory);

/**
* Returns the machine (if any exists) at the given location from this manager
*/
	public Factory getFactory(Location factoryLocation);

/**
* Returns whether a machine exists at the given location
*/
	public boolean factoryExistsAt(Location factoryLocation);
	
/**
* Returns whether a machine is whole at the given location
*/
	public boolean factoryWholeAt(Location factoryLocation);

/**
* Removes the given machine from the object list
*/
	public void removeFactory(Factory factory);

/**
* Returns the saves file name for this manager
*/
	public String getSavesFileName();

}