package com.github.igotyou.FactoryMod.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.classicTriblockFactory.NetherFactory;
import com.github.igotyou.FactoryMod.classicTriblockFactory.NetherFactory.NetherOperationMode;
import com.github.igotyou.FactoryMod.managers.NetherFactoryManager;
import com.google.common.collect.Lists;

public class NetherCsvReader implements IFactoryReader<NetherFactory> {

	/**
	 * The plugin instance
	 */
	FactoryModPlugin mPlugin;
	
	/**
	 * The CSV file being read
	 */
	File mFile;
	
	static final int VERSION = 1;
	
	public NetherCsvReader(FactoryModPlugin plugin, File file) {
		mPlugin = plugin;
		mFile = file;
	}
	
	@Override
	public synchronized List<NetherFactory> read() {
		
		List<NetherFactory> factories = Lists.newArrayList();

		if(!mFile.exists() || mFile.isDirectory()) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: ")
				.append(mFile.getName()).append(" is not a valid file!").toString());
			return factories;
		}
		
		ObjectInputStream ois;
		
		try {
			FileInputStream fileInputStream = new FileInputStream(mFile);
			ois = new ObjectInputStream(fileInputStream);
		} catch (Exception e) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not open file ")
				.append(mFile.getName()).append("for reading: ").append(e.getMessage()).toString());
			return factories;
		}

		int lineNum = 1;
		
		try {
			int version = ois.readInt();
			if(version != VERSION) {
				FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Factory file version did not match expected value");
				ois.close();
				return factories;
			}
			
			int count = ois.readInt();
			for (int i = 0; i < count; i++)
			{
				try {
					factories.add(read(ois));
				} catch (Exception ex) {
					FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Factory parse error: " + ex.getMessage());
					break;
				}
			}
		} catch (IOException ex) {
			FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Could not read file, aborting");
		}
		
		return factories;
	}
	
	public NetherFactory read(ObjectInputStream input) throws Exception {
		String worldName = input.readUTF();
		World world = mPlugin.getServer().getWorld(worldName);

		Location centerLocation = new Location(world, input.readInt(), input.readInt(), input.readInt());
		Location inventoryLocation = new Location(world, input.readInt(), input.readInt(), input.readInt());
		Location powerLocation = new Location(world, input.readInt(), input.readInt(), input.readInt());
		Location overworldTeleportPlatformLocation = new Location(world, input.readInt(), input.readInt(), input.readInt());
		
		String worldName2 = input.readUTF();
		World world2 = mPlugin.getServer().getWorld(worldName2);
		
		Location netherTeleportPlatformLocation = new Location(world2, input.readInt(), input.readInt(), input.readInt());
		
		boolean active = input.readBoolean();
		NetherOperationMode mode = NetherFactory.NetherOperationMode.byId(input.readInt());
		double currentRepair = input.readDouble();
		long timeDisrepair  = input.readLong();
		
		return new NetherFactory(centerLocation, inventoryLocation, powerLocation, netherTeleportPlatformLocation, overworldTeleportPlatformLocation,
				active, currentRepair, timeDisrepair, mode, mPlugin.getNetherFactoryProperties());
	}

}
