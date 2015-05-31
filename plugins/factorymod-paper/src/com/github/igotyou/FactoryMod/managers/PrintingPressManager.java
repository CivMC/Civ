package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.PrintingPress;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.persistence.FactoryDao;
import com.github.igotyou.FactoryMod.persistence.FileBackup;
import com.github.igotyou.FactoryMod.persistence.PersistenceFactory;
import com.github.igotyou.FactoryMod.properties.PrintingPressProperties;
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

public class PrintingPressManager implements Manager<PrintingPress>
{
	private FactoryModPlugin plugin;
	private List<PrintingPress> presses;
	private FactoryDao<PrintingPress> mDao;
	private File mSaveFile;
	private long repairTime;
	private boolean isLogging = true;
	
	public PrintingPressManager(FactoryModPlugin plugin)
	{
		this.plugin = plugin;
		presses = Lists.newArrayList();
		mSaveFile = new File(plugin.getDataFolder(), "pressSaves.txt");
		//Set maintenance clock to 0
		updateFactorys();
		mDao = PersistenceFactory.getFactoryDao(this, mSaveFile, "txt");
	}
	
	public void save() 
	{
		//Takes difference between last repair update and current one and scales repair accordingly
		updateRepair(System.currentTimeMillis() - repairTime);
		repairTime = System.currentTimeMillis();
		
		FileBackup.backup(mSaveFile);
		mDao.writeFactories(presses);		
	}

	public void load()
	{
		isLogging = false;
		repairTime = System.currentTimeMillis();
		for(PrintingPress press : mDao.readFactories()) {
			addFactory(press);
		}
		isLogging = true;
	}

	@Override
	public void updateFactorys() 
	{
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				for (PrintingPress production: presses)
				{
					production.update();
				}
			}
		}, 0L, FactoryModPlugin.PRODUCER_UPDATE_CYCLE);
	}

	@Override
	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerSourceLocation) 
	{
	PrintingPressProperties printingPressProperties = plugin.getPrintingPressProperties();
		
		if (!factoryExistsAt(factoryLocation))
		{
			Block inventoryBlock = inventoryLocation.getBlock();
			Chest chest = (Chest) inventoryBlock.getState();
			Inventory chestInventory = chest.getInventory();
			ItemList<NamedItemStack> inputs = printingPressProperties.getConstructionMaterials();
			boolean hasMaterials = inputs.allIn(chestInventory);
			if (hasMaterials)
			{
				PrintingPress production = new PrintingPress(factoryLocation, inventoryLocation, powerSourceLocation, false, plugin.getPrintingPressProperties());
				if (printingPressProperties.getConstructionMaterials().removeFrom(production.getInventory()))
				{
					addFactory(production);
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created " + printingPressProperties.getName());
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "Not enough materials in chest!");
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a " + printingPressProperties.getName() + " there!");
	}

	@Override
	public InteractionResponse addFactory(PrintingPress press) 
	{
		if (press.getCenterLocation().getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL) && (!factoryExistsAt(press.getCenterLocation()))
				|| !factoryExistsAt(press.getInventoryLocation()) || !factoryExistsAt(press.getPowerSourceLocation()))
		{
			presses.add(press);
			if (isLogging) { FactoryModPlugin.sendConsoleMessage("Printing press created: " + press.getProperties().getName()); }
			return new InteractionResponse(InteractionResult.SUCCESS, "");
		}
		else
		{
			FactoryModPlugin.sendConsoleMessage("Printing press failed to create: " + press.getProperties().getName());
			return new InteractionResponse(InteractionResult.FAILURE, "");
		}
	}

	@Override
	public PrintingPress getFactory(Location factoryLocation) 
	{
		for (PrintingPress production : presses)
		{
			if (production.getCenterLocation().equals(factoryLocation) || production.getInventoryLocation().equals(factoryLocation)
					|| production.getPowerSourceLocation().equals(factoryLocation))
				return production;
		}
		return null;
	}

	@Override
	public boolean factoryExistsAt(Location factoryLocation) 
	{
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = true;
		}
		return returnValue;
	}

	@Override
	public boolean factoryWholeAt(Location factoryLocation) 
	{
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = getFactory(factoryLocation).isWhole(false);
		}
		return returnValue;
	}

	@Override
	public void removeFactory(PrintingPress press) 
	{
		FactoryModPlugin.sendConsoleMessage(new StringBuilder("Printing press removed: ")
		.append(press.getProperties().getName())
		.append(" at ")
		.append(StringUtils.formatCoords(press.getCenterLocation()))
		.toString());
		
		presses.remove(press);
	}

	public void updateRepair(long time)
	{
		for (PrintingPress press : presses)
		{
			press.updateRepair(time / ((double)FactoryModPlugin.REPAIR_PERIOD));
		}
		long currentTime = System.currentTimeMillis();
		Iterator<PrintingPress> itr = presses.iterator();
		while(itr.hasNext())
		{
			PrintingPress press = itr.next();
			if(currentTime > (press.getTimeDisrepair() + FactoryModPlugin.DISREPAIR_PERIOD))
			{
				FactoryModPlugin.sendConsoleMessage(new StringBuilder("Printing press removed due to disrepair: ")
				.append(press.getProperties().getName())
				.append(" at ")
				.append(StringUtils.formatCoords(press.getCenterLocation()))
				.toString());
				
				itr.remove();
			}
		}
	}

	@Override
	public String getSavesFileName() 
	{
		return FactoryModPlugin.PRINTING_PRESSES_SAVE_FILE;
	}

	@Override
	public FactoryModPlugin getPlugin() {
		return plugin;
	}

	@Override
	public Class<PrintingPress> getFactoryType() {
		return PrintingPress.class;
	}

}
