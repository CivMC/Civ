package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
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
public class FactoryModManager 
{
	List<Listener> listeners;
	List<Manager> managers;
	
	FactoryModPlugin plugin; //The plugin object
	
	public static FactoryModManager factoryMan;
	
	
	/**
	 * Constructor
	 */
	public FactoryModManager(FactoryModPlugin plugin)
	{
		FactoryModPlugin.sendConsoleMessage("Initiating FactoryMod Managers.");
		this.plugin = plugin;
		FactoryModManager.factoryMan = this;
		
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
		managers = new ArrayList<Manager>();
		listeners = new ArrayList<Listener>();
		
		//if (FactoryModPlugin.PRODUCTION_ENEABLED)
		//{
			initializeProductionManager();
			initializePrintingPressManager();
			initializeRepairFactoryManager();
			initializeNetherFactoryManager();
		//}
	}
	

	/**
	 * Initializes the Ore Gin Manager
	 */
	private void initializeProductionManager()
	{
		ProductionManager productionnMan = new ProductionManager(plugin);
		
		managers.add(productionnMan);
	}
	/**
	 * Initializes the Printing Press Manager
	 */
	private void initializePrintingPressManager()
	{
		PrintingPressManager printingMan = new PrintingPressManager(plugin);
		
		managers.add(printingMan);
	}
	
	/**
	 * Initialized the NetherFactory manager
	 */
	private void initializeNetherFactoryManager()
	{
		NetherFactoryManager netherMan = new NetherFactoryManager(plugin);
		
		managers.add(netherMan);
	}
	
	/**
	 * Initializes the RepairFactory manager
	 */
	private void initializeRepairFactoryManager(){
		RepairFactoryManager repairMan = new RepairFactoryManager(plugin);
		
		managers.add(repairMan);
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
		for (Manager manager : managers)
		{
			save(manager, getSavesFile(manager.getSavesFileName()));
		}
	}
	
	/**
	 * Loads all managers
	 */
	private void loadManagers()
	{
		for (Manager manager : managers)
		{
			load(manager, getSavesFile(manager.getSavesFileName()));
		}
	}
	
	/**
	 * Returns the appropriate manager depending on the given Manager Type
	 */
	@SuppressWarnings("rawtypes")
	public Manager getManager(Class managerType)
	{
		for (Manager manager : managers)
		{
			if (managerType.isInstance(manager))
			{
				return manager;
			}
		}
		
		return null;
	}
		
	/**
	 * Load file
	 */
	private static void load(Manager managerInterface, File file) 
	{
		try
		{
			managerInterface.load(file);
		}
		catch (FileNotFoundException exception)
		{
			FactoryModPlugin.sendConsoleMessage(file.getName() + " does not exist! Creating file!");
		}
		catch (IOException exception)
		{
			throw new RuntimeException("Failed to load " + file.getPath(), exception);
		}
		
		try
		{
			managerInterface.save(file);
		}
		catch (IOException exception)
		{
			throw new RuntimeException("Failed to create " + file.getPath(), exception);
		}
	}

	/**
	 * Save file
	 */
	private static void save(Manager manager, File file) 
	{	
		
		try
		{
			File newFile = new File(file.getAbsolutePath() + ".new");
			File bakFile = new File(file.getAbsolutePath() + ".bak");
			
			manager.save(newFile);
			
			if (bakFile.exists())
			{
				bakFile.delete();
			}
			
			if (file.exists() && !file.renameTo(bakFile))
			{
				throw new IOException("Failed to rename " + file.getAbsolutePath() + " to " + bakFile.getAbsolutePath());
			}
			
			if (!newFile.renameTo(file))
			{
				throw new IOException("Failed to rename " + newFile.getAbsolutePath() + " to " + file.getAbsolutePath());
			}
		}
		catch (IOException exception)
		{
			throw new RuntimeException("Failed to save to " + file.getAbsolutePath(), exception);
		}
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
		for (Manager manager : managers)
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
		for (Manager manager : managers)
		{
			if (manager.factoryWholeAt(location))
			{
				return true;
			}
		}	
		return false;
	}	
	

	public ProductionManager getProductionManager() 
	{
		for (Manager manager : managers)
		{
			if (manager.getClass() == ProductionManager.class)
			{
				return (ProductionManager) manager;
			}
		}
		
		return null;
	}
	
	public PrintingPressManager getPrintingPressManager() 
	{
		for (Manager manager : managers)
		{
			if (manager.getClass() == PrintingPressManager.class)
			{
				return (PrintingPressManager) manager;
			}
		}
		
		return null;
	}

	public Factory getFactory(Location location) {
		for (Manager manager : managers)
		{
			if (manager.factoryExistsAt(location))
			{
				return manager.getFactory(location);
			}
		}	
		return null;
	}

	public Manager getManager(Location location) {
		for (Manager manager : managers)
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
		for (Manager manager : managers)
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
