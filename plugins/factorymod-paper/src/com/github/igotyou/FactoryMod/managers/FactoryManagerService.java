package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.ABaseFactory;
import com.github.igotyou.FactoryMod.Factorys.IFactory;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

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
public class FactoryManagerService 
{
	List<Listener> listeners;
	List<FactoryModManager<? extends ABaseFactory>> managers;
	
	/**
	 * The plugin instance
	 */
	FactoryModPlugin plugin;

	public static FactoryManagerService factoryMan;
	
	/**
	 * Constructor
	 */
	public FactoryManagerService(FactoryModPlugin plugin)
	{
		FactoryModPlugin.sendConsoleMessage("Initiaiting FactoryMod Managers.");
		
		this.plugin = plugin;
		FactoryManagerService.factoryMan = this;
		
		initializeManagers();
		loadManagers();
		periodicSaving();
		FactoryModPlugin.sendConsoleMessage("Finished initializing FactoryMod Managers.");
	}

	/**
	 * Initializes the necassary managers for enabled factorys
	 */
	private void initializeManagers()
	{
		managers = new ArrayList<FactoryModManager<? extends ABaseFactory>>();
		listeners = new ArrayList<Listener>();
		
		managers.add(new ProductionFactoryManager(plugin));
		managers.add(new PrintingPressManager(plugin));
		managers.add(new RepairFactoryManager(plugin));
		managers.add(new CompactorManager(plugin));
		managers.add(new NetherFactoryManager(plugin));
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
		for (IManager<?> manager : managers)
		{
			manager.save();
		}
	}
	
	/**
	 * Loads all managers
	 */
	private void loadManagers()
	{
		for (IManager<?> manager : managers)
		{
			manager.load();
		}
	}
	
	/**
	 * Returns the appropriate manager depending on the given Manager Type
	 * @param <T>
	 */
	public <T> IManager<?> getManager(Class<T> factoryClass)
	{
		for (IManager<?> manager : managers)
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
		for (IManager<?> manager : managers)
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
		for (IManager<?> manager : managers)
		{
			if (manager.factoryWholeAt(location))
			{
				return true;
			}
		}	
		return false;
	}

	public IFactory getFactory(Location location) {
		for (IManager<?> manager : managers)
		{
			if (manager.factoryExistsAt(location))
			{
				return manager.getFactory(location);
			}
		}	
		return null;
	}

	public IManager<?> getManager(Location location) {
		for (IManager<?> manager : managers)
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
		for (IManager<?> manager : managers)
		{
			response = manager.createFactory(centralLocation, inventoryLocation, powerLocation);
			if (response.getInteractionResult() == InteractionResult.SUCCESS)
			{
				return response;
			}
		}
		
		if (response == null) {
			response = new InteractionResponse(InteractionResult.FAILURE, "Couldn't create a factory, invalid ingredients");
		}
		
		FactoryModPlugin.sendConsoleMessage("The factory could not be created: " + response.getInteractionMessage());
		return response;
	}
}
