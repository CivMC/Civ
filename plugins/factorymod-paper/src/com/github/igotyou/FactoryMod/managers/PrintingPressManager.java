package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.PrintingPress;
import com.github.igotyou.FactoryMod.Factorys.PrintingPress.OperationMode;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.properties.PrintingPressProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.github.igotyou.FactoryMod.utility.StringUtils;

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

public class PrintingPressManager implements Manager
{
	private FactoryModPlugin plugin;
	private List<PrintingPress> presses;
	private long repairTime;
	private boolean isLogging = true;
	
	public PrintingPressManager(FactoryModPlugin plugin)
	{
		this.plugin = plugin;
		presses = new ArrayList<PrintingPress>();
		//Set maintenance clock to 0
		updateFactorys();
	}
	
	public void save(File file) throws IOException 
	{
		//Takes difference between last repair update and current one and scales repair accordingly
		updateRepair(System.currentTimeMillis()-repairTime);
		repairTime=System.currentTimeMillis();
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
		int version = 1;
		oos.writeInt(version);
		oos.writeInt(presses.size());
		for (PrintingPress production : presses)
		{
			//order: subFactoryType world recipe1,recipe2 central_x central_y central_z inventory_x inventory_y inventory_z power_x power_y power_z active productionTimer energyTimer current_Recipe_number 
			
			Location centerlocation = production.getCenterLocation();
			Location inventoryLocation = production.getInventoryLocation();
			Location powerLocation = production.getPowerSourceLocation();
			
			oos.writeUTF(centerlocation.getWorld().getName());
			
			oos.writeInt(centerlocation.getBlockX());
			oos.writeInt(centerlocation.getBlockY());
			oos.writeInt(centerlocation.getBlockZ());

			oos.writeInt(inventoryLocation.getBlockX());
			oos.writeInt(inventoryLocation.getBlockY());
			oos.writeInt(inventoryLocation.getBlockZ());

			oos.writeInt(powerLocation.getBlockX());
			oos.writeInt(powerLocation.getBlockY());
			oos.writeInt(powerLocation.getBlockZ());
			
			oos.writeBoolean(production.getActive());
			oos.writeInt(production.getMode().getId());
			oos.writeInt(production.getProductionTimer());
			oos.writeInt(production.getEnergyTimer());
			oos.writeDouble(production.getCurrentRepair());
			oos.writeLong(production.getTimeDisrepair());

			oos.writeInt(production.getContainedPaper());
			oos.writeInt(production.getContainedBindings());
			oos.writeInt(production.getContainedSecurityMaterials());
			oos.writeInt(production.getLockedResultCode());
			
			int[] processQueue = production.getProcessQueue();
			oos.writeInt(processQueue.length);
			for (int entry : processQueue) {
				oos.writeInt(entry);
			}
		}
		oos.flush();
		fileOutputStream.close();
	}

	public void load(File file) throws IOException 
	{
		isLogging = false;
		try {
			repairTime=System.currentTimeMillis();
			FileInputStream fileInputStream = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fileInputStream);
			int version = ois.readInt();
			assert(version == 1);
			int count = ois.readInt();
			int i = 0;
			for (i = 0; i < count; i++)
			{
				String worldName = ois.readUTF();
				World world = plugin.getServer().getWorld(worldName);

				Location centerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				Location inventoryLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				Location powerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				boolean active = ois.readBoolean();
				OperationMode mode = PrintingPress.OperationMode.byId(ois.readInt());
				int productionTimer = ois.readInt();
				int energyTimer = ois.readInt();
				double currentRepair = ois.readDouble();
				long timeDisrepair  = ois.readLong();
				int containedPaper = ois.readInt();
				int containedBindings = ois.readInt();
				int containedSecurityMaterials = ois.readInt();
				int lockedResultCode = ois.readInt();
				
				int queueLength = ois.readInt();
				int[] processQueue = new int[queueLength];
				int j;
				for (j = 0; j < queueLength; j++) {
					processQueue[j] = ois.readInt();
				}

				PrintingPress production = new PrintingPress(centerLocation, inventoryLocation, powerLocation,
						active, productionTimer,
						energyTimer, currentRepair, timeDisrepair,
						mode,
						plugin.getPrintingPressProperties(),
						containedPaper, containedBindings, containedSecurityMaterials,
						processQueue, lockedResultCode);
				addFactory(production);
			}
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
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
				for (PrintingPress production: presses)
				{
					production.update();
				}
			}
		}, 0L, FactoryModPlugin.PRODUCER_UPDATE_CYCLE);
	}

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

	public InteractionResponse addFactory(Factory factory) 
	{
		PrintingPress press = (PrintingPress) factory;
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
		if(!(factory instanceof PrintingPress)) {
			FactoryModPlugin.sendConsoleMessage("Could not remove unexpected factory type: " + factory.getClass().getName());
			return;
		}
		
		PrintingPress press = (PrintingPress)factory;

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
	
	public String getSavesFileName() 
	{
		return FactoryModPlugin.PRINTING_PRESSES_SAVE_FILE;
	}

}
