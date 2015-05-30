package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.ProductionFactory;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.persistence.FactoryDao;
import com.github.igotyou.FactoryMod.persistence.FileBackup;
import com.github.igotyou.FactoryMod.persistence.PersistenceFactory;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.github.igotyou.FactoryMod.utility.StringUtils;
import com.google.common.collect.Lists;

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

public class ProductionManager implements Manager
{
	private FactoryModPlugin plugin;
	private List<ProductionFactory> producers;
	private FactoryDao<ProductionFactory> mDao;
	private File mSaveFile;
	private long repairTime;
	private boolean isLogging = true;
	
	@SuppressWarnings("unchecked")
	public ProductionManager(FactoryModPlugin plugin)
	{
		this.plugin = plugin;
		mSaveFile = new File(plugin.getDataFolder(), "productionSaves.txt");
		producers = Lists.newArrayList();
		//Set maintenance clock to 0
		updateFactorys();
		//TODO: use type inference to avoid cast
		mDao = (FactoryDao<ProductionFactory>) PersistenceFactory.getFactoryDao(this, mSaveFile, "txt");
	}
	
	public void save(File file) throws IOException 
	{
		//Takes difference between last repair update and current one and scales repair accordingly
		updateRepair(System.currentTimeMillis() - repairTime);
		repairTime = System.currentTimeMillis();
		
		FileBackup.backup(mSaveFile);
		mDao.writeFactories(producers);
	}

	public void load(File file) throws IOException 
	{
		isLogging = false;
		repairTime = System.currentTimeMillis();
		for(ProductionFactory factory : mDao.readFactories()) {
			addFactory(factory);
		}
		isLogging = true;
	}

	public void updateFactorys() 
	{
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				for (ProductionFactory production: producers)
				{
					production.update();
				}
			}
		}, 0L, FactoryModPlugin.PRODUCER_UPDATE_CYCLE);
	}

	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerSourceLocation) 
	{
		if (!factoryExistsAt(factoryLocation))
		{
			Map<String, ProductionProperties> properties = FactoryModPlugin.productionProperties;
			Block inventoryBlock = inventoryLocation.getBlock();
			Chest chest = (Chest) inventoryBlock.getState();
			Inventory chestInventory = chest.getInventory();
			String subFactoryType = null;
			for (Map.Entry<String, ProductionProperties> entry : properties.entrySet())
			{
				ItemList<NamedItemStack> inputs = entry.getValue().getInputs();
				if(inputs.exactlyIn(chestInventory))
				{
					subFactoryType = entry.getKey();
				}
			}
			if (subFactoryType != null)
			{
				ProductionFactory production = new ProductionFactory(factoryLocation, inventoryLocation, powerSourceLocation,subFactoryType);
				if (properties.get(subFactoryType).getInputs().allIn(production.getInventory()))
				{
					addFactory(production);
					properties.get(subFactoryType).getInputs().removeFrom(production.getInventory());
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created " + production.getProductionFactoryProperties().getName());
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "Incorrect materials in chest! Stacks must match perfectly.");
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a factory there!");
	}
	
	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerSourceLocation, int productionTimer, int energyTimer) 
	{
		if (!factoryExistsAt(factoryLocation))
		{
			Map<String, ProductionProperties> properties = FactoryModPlugin.productionProperties;
			Block inventoryBlock = inventoryLocation.getBlock();
			Chest chest = (Chest) inventoryBlock.getState();
			Inventory chestInventory = chest.getInventory();
			String subFactoryType = null;
			boolean hasMaterials = true;
			for (Map.Entry<String, ProductionProperties> entry : properties.entrySet())
			{
				ItemList<NamedItemStack> inputs = entry.getValue().getInputs();
				if(!inputs.allIn(chestInventory))
				{
					hasMaterials = false;
				}
				if (hasMaterials == true)
				{
					subFactoryType = entry.getKey();
				}
			}
			if (hasMaterials && subFactoryType != null)
			{
				ProductionFactory production = new ProductionFactory(factoryLocation, inventoryLocation, powerSourceLocation,subFactoryType);
				if (properties.get(subFactoryType).getInputs().removeFrom(production.getInventory()))
				{
					addFactory(production);
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created " + subFactoryType + " production factory");
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "Not enough materials in chest!");
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a factory there!");
	}

	public InteractionResponse addFactory(Factory factory) 
	{
		ProductionFactory production = (ProductionFactory) factory;
		if (production.getCenterLocation().getBlock().getType().equals(Material.WORKBENCH) && (!factoryExistsAt(production.getCenterLocation()))
				|| !factoryExistsAt(production.getInventoryLocation()) || !factoryExistsAt(production.getPowerSourceLocation()))
		{
			producers.add(production);
			if(isLogging) { FactoryModPlugin.sendConsoleMessage("Production factory created: " + production.getProductionFactoryProperties().getName()); }
			return new InteractionResponse(InteractionResult.SUCCESS, "");
		}
		else
		{
			FactoryModPlugin.sendConsoleMessage("Production factory failed to create: " + production.getProductionFactoryProperties().getName());
			return new InteractionResponse(InteractionResult.FAILURE, "");
		}
	}

	public ProductionFactory getFactory(Location factoryLocation) 
	{
		for (ProductionFactory production : producers)
		{
			if (production.getCenterLocation().equals(factoryLocation) || production.getInventoryLocation().equals(factoryLocation)
					|| production.getPowerSourceLocation().equals(factoryLocation))
				return production;
		}
		return null;
	}
	
	public boolean factoryExistsAt(Location factoryLocation) 
	{
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = true;
		}
		return returnValue;
	}
	
	public boolean factoryWholeAt(Location factoryLocation) 
	{
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = getFactory(factoryLocation).isWhole(false);
		}
		return returnValue;
	}

	public void removeFactory(Factory factory) 
	{
		if(!(factory instanceof ProductionFactory)) {
			FactoryModPlugin.sendConsoleMessage("Could not remove unexpected factory type: " + factory.getClass().getName());
			return;
		}
		
		ProductionFactory producer = (ProductionFactory)factory;
		
		FactoryModPlugin.sendConsoleMessage(new StringBuilder("Production factory removed: ")
				.append(producer.getProductionFactoryProperties().getName())
				.append(" at ")
				.append(StringUtils.formatCoords(producer.getCenterLocation()))
				.toString());
		
		producers.remove(producer);
	}
	
	public void updateRepair(long time)
	{
		for (ProductionFactory production : producers)
		{
			production.updateRepair(time / ((double)FactoryModPlugin.REPAIR_PERIOD));
		}
		long currentTime = System.currentTimeMillis();
		Iterator<ProductionFactory> itr = producers.iterator();
		while(itr.hasNext())
		{
			ProductionFactory producer = itr.next();
			if(currentTime > (producer.getTimeDisrepair() + FactoryModPlugin.DISREPAIR_PERIOD))
			{
				FactoryModPlugin.sendConsoleMessage(new StringBuilder("Production factory removed due to disrepair: ")
					.append(producer.getProductionFactoryProperties().getName())
					.append(" at ")
					.append(StringUtils.formatCoords(producer.getCenterLocation()))
					.toString());
				
				itr.remove();				
			}
		}
	}
	
	public String getSavesFileName() 
	{
		return mSaveFile.getName();
	}

	@Override
	public FactoryModPlugin getPlugin() {
		return plugin;
	}

}
