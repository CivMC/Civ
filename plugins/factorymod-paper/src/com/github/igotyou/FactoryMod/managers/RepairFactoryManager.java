package com.github.igotyou.FactoryMod.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.RepairFactory;
import com.github.igotyou.FactoryMod.properties.RepairFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class RepairFactoryManager extends AManager<RepairFactory>{

	public RepairFactoryManager(FactoryModPlugin plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class<RepairFactory> getFactoryType() {
		return RepairFactory.class;
	}

	//SEE RepairCsvReader.java
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
	
	//SEE RepairCsvWriter.java
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
		
		if(!factoryExistsAt(factoryLocation)) {
			Block inventoryBlock = inventoryLocation.getBlock();
			Chest chest = (Chest) inventoryBlock.getState();
			Inventory chestInventory = chest.getInventory();
			ItemList<NamedItemStack> constructionMaterials = repairFactoryProperties.getConstructionMaterials();
			
			if (constructionMaterials.exactlyIn(chestInventory)){
				RepairFactory factory = new RepairFactory(factoryLocation, inventoryLocation, powerLocation, false, repairFactoryProperties);
				constructionMaterials.removeFrom(factory.getInventory());

				if (addFactory(factory).getInteractionResult() == InteractionResult.FAILURE) {
					return new InteractionResponse(InteractionResult.FAILURE, "Unable to construct a " + repairFactoryProperties.getName());
				} else {
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created a " + repairFactoryProperties.getName());
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "Incorrect materials in chest! Stacks must match perfectly to create a " + repairFactoryProperties.getName());
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a " + repairFactoryProperties.getName() + " there!");
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

}
