package com.github.igotyou.FactoryMod.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.classicTriblockFactory.NetherFactory;

public class NetherCsvWriter implements IFactoryWriter<NetherFactory> {
	
	static final int VERSION = 1;
	
	File mFile;
	
	public NetherCsvWriter(File file) {
		mFile = file;
	}
	
	@Override
	public void write(List<NetherFactory> factories) {
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
			out.writeInt(factories.size());
		} catch(IOException ex) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not write to file ")
				.append(mFile.getName()).append(": ").append(ex.getMessage()).toString());
			try {
				if(fileStream!= null) fileStream.close();
			} catch (IOException e) {}
			return;
		}
		
		for (NetherFactory factory : factories)
		{
			Location centerlocation = factory.getCenterLocation();
			Location inventoryLocation = factory.getInventoryLocation();
			Location powerLocation = factory.getPowerSourceLocation();
			Location netherTeleportPlatformLocation = factory.getNetherTeleportPlatform();
			Location overworldTeleportPlatformLocation = factory.getOverworldTeleportPlatform();
			
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
			
			out.writeInt(overworldTeleportPlatformLocation.getBlockX());
			out.writeInt(overworldTeleportPlatformLocation.getBlockY());
			out.writeInt(overworldTeleportPlatformLocation.getBlockZ());
			
			out.writeUTF(netherTeleportPlatformLocation.getWorld().getName());
			out.writeInt(netherTeleportPlatformLocation.getBlockX());
			out.writeInt(netherTeleportPlatformLocation.getBlockY());
			out.writeInt(netherTeleportPlatformLocation.getBlockZ());
			
			out.writeBoolean(factory.getActive());
			out.writeInt(factory.getMode().getId());
			out.writeDouble(factory.getCurrentRepair());
			out.writeLong(factory.getTimeDisrepair());
			} catch (IOException e) {
				FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not write to ")
					.append(mFile.getName()).append(" for factory ").append(factories.indexOf(factory)).append(": ").append(e.getMessage()).toString());
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
