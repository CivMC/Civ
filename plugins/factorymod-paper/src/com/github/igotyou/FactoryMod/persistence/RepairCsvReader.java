package com.github.igotyou.FactoryMod.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.classicTriblockFactory.RepairFactory;
import com.github.igotyou.FactoryMod.classicTriblockFactory.RepairFactory.RepairFactoryMode;
import com.google.common.collect.Lists;

public class RepairCsvReader implements IFactoryReader<RepairFactory>{
	
	/**
	 * The plugin instance
	 */
	FactoryModPlugin mPlugin;
	
	/**
	 * The CSV file being read
	 */
	File mFile;
	
	static final int VERSION = 1;
	
	public RepairCsvReader(FactoryModPlugin plugin, File file) {
		mPlugin = plugin;
		mFile = file;
	}

	@Override
	public synchronized List<RepairFactory> read() {
		
		List<RepairFactory> factory = Lists.newArrayList();


		if(!mFile.exists() || mFile.isDirectory()) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: ")
				.append(mFile.getName()).append(" is not a valid file!").toString());
			return factory;
		}
		
		ObjectInputStream ois;
		
		try {
			FileInputStream fileInputStream = new FileInputStream(mFile);
			ois = new ObjectInputStream(fileInputStream);
		} catch (Exception e) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not open file ")
				.append(mFile.getName()).append("for reading: ").append(e.getMessage()).toString());
			return factory;
		}

		int lineNum = 1;
		
		try {
			int version = ois.readInt();
			if(version != VERSION) {
				FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Factory file version did not match expected value");
				ois.close();
				return factory;
			}
			
			int count = ois.readInt();
			for (int i = 0; i < count; i++)
			{
				try {
					factory.add(read(ois));
				} catch (Exception ex) {
					FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Factory parse error: " + ex.getMessage());
					break;
				}
			}
		} catch (IOException ex) {
			FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Could not read file, aborting");
		}
		//TODO: ensure everything is closed
		return factory;
	}
	
	//TODO: split into common csv file handler and factory type parser
	public RepairFactory read(ObjectInputStream ois) throws Exception {
		String worldName = ois.readUTF();
		World world = Bukkit.getWorld(worldName);
		
		Location centerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
		Location inventoryLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
		Location powerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
		
		RepairFactoryMode mode = RepairFactoryMode.byId(ois.readInt());
		double currentRepair = ois.readDouble();
		long timeDisrepair  = ois.readLong();
		
		return new RepairFactory(centerLocation, inventoryLocation, powerLocation, false, mPlugin.getRepairFactoryProperties(), 
				mode, currentRepair, timeDisrepair);
	}

}
