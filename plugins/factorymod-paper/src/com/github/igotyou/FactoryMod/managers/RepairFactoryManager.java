package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.RepairFactory;
import com.github.igotyou.FactoryMod.Factorys.RepairFactory.RepairFactoryMode;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.properties.RepairFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.github.igotyou.FactoryMod.utility.StringUtils;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public class RepairFactoryManager implements Manager{
	
	private FactoryModPlugin plugin;
	private ReinforcementManager rm = Citadel.getReinforcementManager();
	private List<RepairFactory> repairFactories;
	private boolean isLogging = true;
	private long repairTime;
	
	public RepairFactoryManager(FactoryModPlugin plugin){
		this.plugin = plugin;
		repairFactories = new ArrayList<RepairFactory>();
		updateFactorys();
	}

	@Override
	public void save(File file) throws IOException {
		repairTime=System.currentTimeMillis();
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
		int version = 1;
		oos.writeInt(version);
		oos.writeInt(repairFactories.size());
		for (RepairFactory factory: repairFactories){
			Location centerLocation = factory.getCenterLocation();
			Location inventoryLocation = factory.getInventoryLocation();
			Location powerLocation = factory.getPowerSourceLocation();
			
			oos.writeUTF(centerLocation.getWorld().getName());
			
			oos.writeInt(centerLocation.getBlockX());
			oos.writeInt(centerLocation.getBlockY());
			oos.writeInt(centerLocation.getBlockZ());
			
			oos.writeInt(inventoryLocation.getBlockX());
			oos.writeInt(inventoryLocation.getBlockY());
			oos.writeInt(inventoryLocation.getBlockZ());

			oos.writeInt(powerLocation.getBlockX());
			oos.writeInt(powerLocation.getBlockY());
			oos.writeInt(powerLocation.getBlockZ());
			
			oos.writeInt(factory.getMode().getId());
			oos.writeDouble(factory.getCurrentRepair());
			oos.writeLong(factory.getTimeDisrepair());
		}
		oos.flush();
		fileOutputStream.close();
	}

	@Override
	public void load(File file) throws IOException {
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
				World world = Bukkit.getWorld(worldName);
				
				Location centerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				Location inventoryLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				Location powerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				
				RepairFactoryMode mode = RepairFactoryMode.byId(ois.readInt());
				double currentRepair = ois.readDouble();
				long timeDisrepair  = ois.readLong();
				
				RepairFactory factory = new RepairFactory(centerLocation, inventoryLocation, powerLocation, false, plugin.getRepairFactoryProperties(), 
						this, mode, currentRepair, timeDisrepair);
				addFactory(factory);
			}
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateFactorys() {
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				for (RepairFactory factory: repairFactories)
				{
					factory.update();
				}
			}
		}, 0L, FactoryModPlugin.PRODUCER_UPDATE_CYCLE);
	}

	@Override
	public InteractionResponse createFactory(Location factoryLocation,
			Location inventoryLocation, Location powerLocation) {
		RepairFactoryProperties repairFactoryProperties = plugin.getRepairFactoryProperties();
		Block inventoryBlock = inventoryLocation.getBlock();
		Chest chest = (Chest) inventoryBlock.getState();
		Inventory chestInventory = chest.getInventory();
		ItemList<NamedItemStack> constructionMaterials = repairFactoryProperties.getConstructionMaterials();
		if(!factoryExistsAt(factoryLocation)) {
			if (constructionMaterials.oneIn(chestInventory)){
				RepairFactory factory = new RepairFactory(factoryLocation, inventoryLocation, powerLocation, false, repairFactoryProperties,
						this);
				repairFactoryProperties.getConstructionMaterials().removeFrom(chestInventory);
				return addFactory(factory);
			}
			else
				return new InteractionResponse(InteractionResult.FAILURE, "Incorrect materials in chest! Stacks must match perfectly.");
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a factory there!");
	}

	@Override
	public InteractionResponse addFactory(Factory factory) {
		RepairFactory repairFactory = (RepairFactory) factory;
		if (repairFactory.getCenterLocation().getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL) && 
				(!factoryExistsAt(repairFactory.getCenterLocation())
				|| !factoryExistsAt(repairFactory.getInventoryLocation()) 
				|| !factoryExistsAt(repairFactory.getPowerSourceLocation())))
		{
			repairFactories.add(repairFactory);
			if (isLogging){
				FactoryModPlugin.sendConsoleMessage("Repair Factory created: " + repairFactory.getProperties().getName());
			}
			return new InteractionResponse(InteractionResult.SUCCESS, "You have successfully created a Repair Factory.");
		}
		else {
			FactoryModPlugin.sendConsoleMessage("Repair Factory failed to create: " + repairFactory.getProperties().getName());
			return new InteractionResponse(InteractionResult.FAILURE, "");
		}
	}

	@Override
	public Factory getFactory(Location factoryLocation) {
		for (RepairFactory factory: repairFactories){
			if (factory.getCenterLocation().equals(factoryLocation)
					|| factory.getInventoryLocation().equals(factoryLocation)
					|| factory.getPowerSourceLocation().equals(factoryLocation))
				return factory;
		}
		return null;
	}

	@Override
	public boolean factoryExistsAt(Location factoryLocation) {
		return getFactory(factoryLocation) != null;
	}

	@Override
	public boolean factoryWholeAt(Location factoryLocation) {
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = true;
		}
		return returnValue;
	}

	@Override
	public void removeFactory(Factory factory) {
		if (!(factory instanceof RepairFactory)){
			FactoryModPlugin.sendConsoleMessage("Could not remove unexpected factory type: " + factory.getClass().getName());
			return;
		}
		
		RepairFactory repairFactory = (RepairFactory) factory;
		
		FactoryModPlugin.sendConsoleMessage(new StringBuilder("Repair factory removed: ")
		.append(repairFactory.getProperties().getName())
		.append(" at ")
		.append(StringUtils.formatCoords(repairFactory.getCenterLocation()))
		.toString());
		
		repairFactories.remove(repairFactory);
	}

	@Override
	public String getSavesFileName() {
		return FactoryModPlugin.REPAIR_FACTORY_SAVE_FILE;
	}

}
