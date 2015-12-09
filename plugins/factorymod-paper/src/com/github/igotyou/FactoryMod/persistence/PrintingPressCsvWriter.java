package com.github.igotyou.FactoryMod.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.classicTriblockFactory.PrintingPress;

public class PrintingPressCsvWriter implements IFactoryWriter<PrintingPress> {
	
	static final int VERSION = 1;
	
	File mFile;
	
	public PrintingPressCsvWriter(File file) {
		mFile = file;
	}

	@Override
	public synchronized void write(List<PrintingPress> presses) {

		FileBackup.backup(mFile);
		
		if(!mFile.exists()) {
			try {
				mFile.createNewFile();
			} catch (IOException e) {
				FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not create file ")
					.append(mFile.getName()).append(": ").append(e.getMessage()).toString());
				return;				
			}
		}
		
		FileOutputStream fileStream = null;
		
		try {
			fileStream = new FileOutputStream(mFile);
		} catch (FileNotFoundException ex) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not open file ")
				.append(mFile.getName()).append("for writing: ").append(ex.getMessage()).toString());
			return;
		}
		
		ObjectOutputStream out = null;

		try {
			out = new ObjectOutputStream(fileStream);
			out.writeInt(VERSION);
			out.writeInt(presses.size());
		} catch(IOException ex) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not write to file ")
				.append(mFile.getName()).append(": ").append(ex.getMessage()).toString());
			try {
				if(fileStream!= null) fileStream.close();
			} catch (IOException e) {}
			return;
		}
			for (PrintingPress press : presses)
			{
				// order: subFactoryType world recipe1,recipe2
				// central_x central_y central_z
				// inventory_x inventory_y inventory_z
				// power_x power_y power_z
				// active productionTimer energyTimer current_Recipe_number 
				
				Location centerlocation = press.getCenterLocation();
				Location inventoryLocation = press.getInventoryLocation();
				Location powerLocation = press.getPowerSourceLocation();
				
				try {
					out.writeUTF(centerlocation.getWorld().getName());
					
					out.writeInt(centerlocation.getBlockX());
					out.writeInt(centerlocation.getBlockY());
					out.writeInt(centerlocation.getBlockZ());
		
					out.writeInt(inventoryLocation.getBlockX());
					out.writeInt(inventoryLocation.getBlockY());
					out.writeInt(inventoryLocation.getBlockZ());
		
					out.writeInt(powerLocation.getBlockX());
					out.writeInt(powerLocation.getBlockY());
					out.writeInt(powerLocation.getBlockZ());
					
					out.writeBoolean(press.getActive());
					out.writeInt(press.getMode().getId());
					out.writeInt(press.getProductionTimer());
					out.writeInt(press.getEnergyTimer());
					out.writeDouble(press.getCurrentRepair());
					out.writeLong(press.getTimeDisrepair());
		
					out.writeInt(press.getContainedPaper());
					out.writeInt(press.getContainedBindings());
					out.writeInt(press.getContainedSecurityMaterials());
					out.writeInt(press.getLockedResultCode());
					
					int[] processQueue = press.getProcessQueue();
					out.writeInt(processQueue.length);
					for (int entry : processQueue) {
						out.writeInt(entry);
					}
				} catch (IOException e) {
					FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not write to ")
						.append(mFile.getName()).append(" for factory ").append(presses.indexOf(press)).append(": ").append(e.getMessage()).toString());
				}
			}
			
		try {
			out.flush();
			fileStream.close();
		} catch(IOException ex) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not complete write to ")
				.append(mFile.getName()).append(": ").append(ex.getMessage()).toString());
		}
	}
}
