package com.github.igotyou.FactoryMod.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.eggs.PipeEgg;
import com.github.igotyou.FactoryMod.factories.Factory;

public class FactoryFileHandler {
	private FactoryMod plugin;
	private FactoryModManager manager;
	File saveFile;
	File backup;

	public FactoryFileHandler(FactoryModManager manager) {
		plugin = FactoryMod.getPlugin();
		this.manager = manager;
		saveFile = new File(plugin.getDataFolder().getAbsolutePath()
				+ File.separator + "factoryData.txt");
		backup = new File(plugin.getDataFolder().getAbsolutePath()
				+ File.separator + "factoryDataPreviousSave.txt");
	}

	public void save(Collection<Factory> factories) {
		if (saveFile.exists()) {
			// old save file exists, so it is our new backup now
			if (backup.exists()) {
				backup.delete();
			}
			saveFile.renameTo(backup);
		}
		try {
			saveFile.createNewFile();
			FileWriter fw = new FileWriter(saveFile);
			BufferedWriter buff = new BufferedWriter(fw);
			for (Factory f : factories) {
				buff.write(f.serialize());
				buff.newLine();
			}
			buff.flush();
			buff.close();
		} catch (Exception e) {
			// In case anything goes wrong while saving we always keep the
			// latest valid backup
			plugin.severe("Fatal error while trying to save factory data");
			e.printStackTrace();
			saveFile.delete();
		}
	}

	public void load(Map<String, IFactoryEgg> eggs) {
		if (saveFile.exists()) {
			loadFromFile(saveFile, eggs);
		} else {
			plugin.warning("No default save file found");
			if (backup.exists()) {
				plugin.info("Backup file found, loading backup");
				loadFromFile(backup, eggs);
			} else {
				plugin.warning("No backup save file found. If you are not starting this plugin for the first time you should be worried now");
			}
		}
	}

	private void loadFromFile(File f, Map<String, IFactoryEgg> eggs) {
		int counter = 0;
		try {
			FileReader fr = new FileReader(f);
			BufferedReader reader = new BufferedReader(fr);
			String line = reader.readLine();
			while (line != null) {
				String[] content = line.split("#");
				switch (content[0]) {
				case "FCC":
					FurnCraftChestEgg egg = (FurnCraftChestEgg) eggs
							.get(content[1]);
					if (egg == null) {
						plugin.warning("Save file contained factory named "
								+ content[1]
								+ " , but no factory with this name was found in the config");
						line = reader.readLine();
						continue;
					}
					int health = Integer.parseInt(content[2]);
					int productionTimer = Integer.parseInt(content[3]);
					String selectedRecipe = content[4];
					List<Block> blocks = new LinkedList<Block>();
					for (int i = 5; i < 17; i += 4) {
						World w = plugin.getServer().getWorld(content[i]);
						int x = Integer.parseInt(content[i + 1]);
						int y = Integer.parseInt(content[i + 2]);
						int z = Integer.parseInt(content[i + 3]);
						blocks.add(new Location(w, x, y, z).getBlock());
					}
					Factory fac = egg.revive(blocks, health, selectedRecipe,
							productionTimer);
					manager.addFactory(fac);
					counter++;
					break;
				case "PIPE":
					PipeEgg pipeEgg = (PipeEgg) eggs.get(content[1]);
					int runTime = Integer.parseInt(content[2]);
					List<Material> mats = new LinkedList<Material>();
					int i = 3;
					for (; i < content.length; i++) {
						if (content[i].equals("NONE")) {
							mats = null;
							i+=2;
							break;
						}
						if (content[i].equals("BLOCKS")) {
							i++;
							break;
						}
						Material m = Material.valueOf(content[i]);
						mats.add(m);
					}
					List<Block> pipeBlocks = new LinkedList<Block>();
					for (; i < content.length; i += 4) {
						World w = plugin.getServer().getWorld(content[i]);
						int x = Integer.parseInt(content[i + 1]);
						int y = Integer.parseInt(content[i + 2]);
						int z = Integer.parseInt(content[i + 3]);
						pipeBlocks.add(new Location(w, x, y, z).getBlock());
					}
					Factory p = pipeEgg.revive(pipeBlocks, mats, runTime);
					manager.addFactory(p);
					counter++;
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			plugin.severe("Fatal error while loading factory data");
			e.printStackTrace();
		}
		plugin.info("Loaded " + counter + " factory from save file");
	}
}
