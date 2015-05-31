package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.BaseFactory;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.google.common.collect.Lists;
//original file:
/**
 * MachinesManager.java
 * Purpose: Manages the initialization and updating of all managers.
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
//edited file:
/**
 *  FactorysManager.java
 *  Purpose: Manages the initialization and updating of all managers.
 * @author igotyou
 *
 */
public class FactoryModManager 
{
	List<Manager<? extends BaseFactory>> managers;
	
	/**
	 * The plugin instance
	 */
	FactoryModPlugin plugin;
	
	/**
	 * Constructor
	 */
	public FactoryModManager(FactoryModPlugin plugin)
	{
		FactoryModPlugin.sendConsoleMessage("Initiaiting FactoryMod Managers.");
		
		this.plugin = plugin;
		managers = Lists.newArrayList();
		managers.add(new ProductionManager(plugin));
		managers.add(new PrintingPressManager(plugin));
		managers.add(new NetherFactoryManager(plugin));
		loadManagers();
		periodicSaving();
		
		FactoryModPlugin.sendConsoleMessage("Finished initializing FactoryMod Managers.");
	}
	
	/**
	 * When plugin disabled, this is called.
	 */
	public void onDisable()
	{
		saveManagers();
	}
	
	/**
	 * Saves all managers
	 */
	private void saveManagers()
	{
		for (Manager<?> manager : managers)
		{
			manager.save();
		}
	}
	
	/**
	 * Loads all managers
	 */
	private void loadManagers()
	{
		for (Manager<?> manager : managers)
		{
			manager.load();
		}
	}
	
	/**
	 * Returns the appropriate manager depending on the given Manager Type
	 * @param <T>
	 */
	public <T> Manager<?> getManager(Class<T> factoryClass)
	{
		for (Manager<?> manager : managers)
		{
			if (manager.getFactoryType() == factoryClass)
			{
				return manager;
			}
		}
		
		return null;
	}
	
	/**
	 * Save Factories to file every SAVE_CYCLE minutes.
	 */
	private void periodicSaving()
	{
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override  
			public void run()
			{
				FactoryModPlugin.sendConsoleMessage("Saving Factory data...");
				saveManagers();
			}
		}, (FactoryModPlugin.SAVE_CYCLE), 
		FactoryModPlugin.SAVE_CYCLE);
	}
	
	/**
	 * Returns the Factory Saves file
	 */
	public File getSavesFile(String fileName)
	{
		return new File(plugin.getDataFolder(), fileName + ".txt");
	}

	/**
	 * Returns whether a factory exists at given location in any manager
	 */
	public boolean factoryExistsAt(Location location)
	{
		for (Manager<?> manager : managers)
		{
			if (manager.factoryExistsAt(location))
			{
				return true;
			}
		}	
		return false;
	}

	/**
	 * Returns whether a factory is whole at given location in any manager
	 */
	public boolean factoryWholeAt(Location location)
	{
		for (Manager<?> manager : managers)
		{
			if (manager.factoryWholeAt(location))
			{
				return true;
			}
		}	
		return false;
	}

	public Factory getFactory(Location location) {
		for (Manager<?> manager : managers)
		{
			if (manager.factoryExistsAt(location))
			{
				return manager.getFactory(location);
			}
		}	
		return null;
	}

	public Manager<?> getManager(Location location) {
		for (Manager<?> manager : managers)
		{
			if (manager.factoryExistsAt(location))
			{
				return manager;
			}
		}	
		return null;
	}

	public InteractionResponse createFactory(Location centralLocation,
			Location inventoryLocation, Location powerLocation) {
		InteractionResponse response = null;
		for (Manager<?> manager : managers)
		{
			response = manager.createFactory(centralLocation, inventoryLocation, powerLocation);
			if (response.getInteractionResult() == InteractionResult.SUCCESS)
			{
				return response;
			}
		}
		
		FactoryModPlugin.sendConsoleMessage("The factory could not be created: " + response.getInteractionMessage());
		return response;
	}
}
