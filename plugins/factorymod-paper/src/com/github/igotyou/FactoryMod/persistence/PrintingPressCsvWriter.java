package com.github.igotyou.FactoryMod.persistence;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.Factorys.PrintingPress;

public class PrintingPressCsvWriter {

	
	public void save() {

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
}
