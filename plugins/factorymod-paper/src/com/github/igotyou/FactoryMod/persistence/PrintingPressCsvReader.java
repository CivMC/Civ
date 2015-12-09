package com.github.igotyou.FactoryMod.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.classicTriblockFactory.PrintingPress;
import com.github.igotyou.FactoryMod.classicTriblockFactory.PrintingPress.OperationMode;
import com.google.common.collect.Lists;

public class PrintingPressCsvReader implements IFactoryReader<PrintingPress> {

	/**
	 * The plugin instance
	 */
	FactoryModPlugin mPlugin;
	
	/**
	 * The CSV file being read
	 */
	File mFile;
	
	static final int VERSION = 1;
	
	public PrintingPressCsvReader(FactoryModPlugin plugin, File file) {
		mPlugin = plugin;
		mFile = file;
	}

	@Override
	public synchronized List<PrintingPress> read() {
		
		List<PrintingPress> presses = Lists.newArrayList();


		if(!mFile.exists() || mFile.isDirectory()) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: ")
				.append(mFile.getName()).append(" is not a valid file!").toString());
			return presses;
		}
		
		ObjectInputStream ois;
		
		try {
			FileInputStream fileInputStream = new FileInputStream(mFile);
			ois = new ObjectInputStream(fileInputStream);
		} catch (Exception e) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not open file ")
				.append(mFile.getName()).append("for reading: ").append(e.getMessage()).toString());
			return presses;
		}

		int lineNum = 1;
		
		try {
			int version = ois.readInt();
			if(version != VERSION) {
				FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Factory file version did not match expected value");
				ois.close();
				return presses;
			}
			
			int count = ois.readInt();
			for (int i = 0; i < count; i++)
			{
				try {
					presses.add(read(ois));
				} catch (Exception ex) {
					FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Factory parse error: " + ex.getMessage());
					break;
				}
			}
		} catch (IOException ex) {
			FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Could not read file, aborting");
		}
		//TODO: ensure everything is closed
		return presses;
	}
	
	public PrintingPress read(ObjectInputStream input) throws Exception {
		
		try {
			World world = mPlugin.getServer().getWorld(input.readUTF());
			Location centerLocation = new Location(world, input.readInt(), input.readInt(), input.readInt());
			Location inventoryLocation = new Location(world, input.readInt(), input.readInt(), input.readInt());
			Location powerLocation = new Location(world, input.readInt(), input.readInt(), input.readInt());
			boolean active = input.readBoolean();
			OperationMode mode = PrintingPress.OperationMode.byId(input.readInt());
			int productionTimer = input.readInt();
			int energyTimer = input.readInt();
			double currentRepair = input.readDouble();
			long timeDisrepair  = input.readLong();
			int containedPaper = input.readInt();
			int containedBindings = input.readInt();
			int containedSecurityMaterials = input.readInt();
			int lockedResultCode = input.readInt();
			
			int queueLength = input.readInt();
			int[] processQueue = new int[queueLength];

			for (int q = 0; q < queueLength; q++) {
				processQueue[q] = input.readInt();
			}

			return new PrintingPress(
					centerLocation, inventoryLocation, powerLocation,
					active, productionTimer, energyTimer, currentRepair, timeDisrepair,
					mode, mPlugin.getPrintingPressProperties(),
					containedPaper, containedBindings, containedSecurityMaterials,
					processQueue, lockedResultCode);
		} catch (NumberFormatException e) {
			throw new Exception("Expected token was not an integer");
		}
		
	}

}
