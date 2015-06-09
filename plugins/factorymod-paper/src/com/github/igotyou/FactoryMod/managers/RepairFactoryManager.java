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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.NetherFactory;
import com.github.igotyou.FactoryMod.Factorys.ProductionFactory;
import com.github.igotyou.FactoryMod.Factorys.RepairFactory;
import com.github.igotyou.FactoryMod.Factorys.RepairFactory.RepairFactoryMode;
import com.github.igotyou.FactoryMod.Factorys.IFactory;
import com.github.igotyou.FactoryMod.managers.IManager;
import com.github.igotyou.FactoryMod.properties.RepairFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.github.igotyou.FactoryMod.utility.StringUtils;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public class RepairFactoryManager extends AManager<RepairFactory>{

	public RepairFactoryManager(FactoryModPlugin plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class<RepairFactory> getFactoryType() {
		return RepairFactory.class;
	}
	private ReinforcementManager rm = Citadel.getReinforcementManager();

//	@Override
//	public void save(File file) throws IOException {
//		repairTime=System.currentTimeMillis();
//		FileOutputStream fileOutputStream = new FileOutputStream(file);
//		ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
//		int version = 1;
//		oos.writeInt(version);
//		oos.writeInt(factories.size());
//		for (RepairFactory factory: factories){
//			Location centerLocation = factory.getCenterLocation();
//			Location inventoryLocation = factory.getInventoryLocation();
//			Location powerLocation = factory.getPowerSourceLocation();
//			
//			oos.writeUTF(centerLocation.getWorld().getName());
//			
//			oos.writeInt(centerLocation.getBlockX());
//			oos.writeInt(centerLocation.getBlockY());
//			oos.writeInt(centerLocation.getBlockZ());
//			
//			oos.writeInt(inventoryLocation.getBlockX());
//			oos.writeInt(inventoryLocation.getBlockY());
//			oos.writeInt(inventoryLocation.getBlockZ());
//
//			oos.writeInt(powerLocation.getBlockX());
//			oos.writeInt(powerLocation.getBlockY());
//			oos.writeInt(powerLocation.getBlockZ());
//			
//			oos.writeInt(factory.getMode().getId());
//			oos.writeDouble(factory.getCurrentRepair());
//			oos.writeLong(factory.getTimeDisrepair());
//		}
//		oos.flush();
//		fileOutputStream.close();
//	}

//	@Override
//	public void load(File file) throws IOException {
//		isLogging = false;
//		try {
//			repairTime=System.currentTimeMillis();
//			FileInputStream fileInputStream = new FileInputStream(file);
//			ObjectInputStream ois = new ObjectInputStream(fileInputStream);
//			int version = ois.readInt();
//			assert(version == 1);
//			int count = ois.readInt();
//			int i = 0;
//			for (i = 0; i < count; i++)
//			{
//				String worldName = ois.readUTF();
//				World world = Bukkit.getWorld(worldName);
//				
//				Location centerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
//				Location inventoryLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
//				Location powerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
//				
//				RepairFactoryMode mode = RepairFactoryMode.byId(ois.readInt());
//				double currentRepair = ois.readDouble();
//				long timeDisrepair  = ois.readLong();
//				
//				RepairFactory factory = new RepairFactory(centerLocation, inventoryLocation, powerLocation, false, plugin.getRepairFactoryProperties(), 
//						this, mode, currentRepair, timeDisrepair);
//				addFactory(factory);
//			}
//			fileInputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

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
				RepairFactory factory = new RepairFactory(factoryLocation, inventoryLocation, powerLocation, false, repairFactoryProperties/*,
						this*/);
				repairFactoryProperties.getConstructionMaterials().removeFrom(chestInventory);
				return addFactory(factory);
			}
			else
				return new InteractionResponse(InteractionResult.FAILURE, "Incorrect materials in chest! Stacks must match perfectly.");
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a factory there!");
	}

	public boolean isClear(RepairFactory factory){
		return factory.getCenterLocation().getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL) && 
				(!factoryExistsAt(factory.getCenterLocation())
				|| !factoryExistsAt(factory.getInventoryLocation()) 
				|| !factoryExistsAt(factory.getPowerSourceLocation()));
	}

	@Override
	public RepairFactory getFactory(Location factoryLocation) {
		for (RepairFactory factory: factories){
			if (factory.getCenterLocation().equals(factoryLocation)
					|| factory.getInventoryLocation().equals(factoryLocation)
					|| factory.getPowerSourceLocation().equals(factoryLocation))
				return factory;
		}
		return null;
	}

	@Override
	public String getSavesFileName() {
		return FactoryModPlugin.REPAIR_FACTORY_SAVE_FILE;
	}

}
