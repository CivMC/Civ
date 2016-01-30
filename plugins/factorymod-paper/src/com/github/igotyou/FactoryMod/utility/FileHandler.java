package com.github.igotyou.FactoryMod.utility;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.eggs.PipeEgg;
import com.github.igotyou.FactoryMod.eggs.SorterEgg;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.factories.Pipe;
import com.github.igotyou.FactoryMod.factories.Sorter;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;

public class FileHandler {
	private FactoryMod plugin;
	private FactoryModManager manager;
	File saveFile;
	File backup;

	public FileHandler(FactoryModManager manager) {
		plugin = FactoryMod.getPlugin();
		this.manager = manager;
		saveFile = new File(plugin.getDataFolder().getAbsolutePath()
				+ File.separator + "factoryData.yml");
		backup = new File(plugin.getDataFolder().getAbsolutePath()
				+ File.separator + "factoryDataPreviousSave.yml");
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
			YamlConfiguration config = YamlConfiguration
					.loadConfiguration(saveFile);
			for (Factory f : factories) {
				String current = serializeLocation(f.getMultiBlockStructure()
						.getCenter());
				config.set(current + ".name", f.getName());
				config.getConfigurationSection(current).createSection("blocks");
				for (Location b : f.getMultiBlockStructure().getAllBlocks()) {
					configureLocation(config.getConfigurationSection(current)
							.getConfigurationSection("blocks"), b);
				}
				if (f instanceof FurnCraftChestFactory) {
					FurnCraftChestFactory fccf = (FurnCraftChestFactory) f;
					config.set(current + ".type", "FCC");
					config.set(current + ".health",
							((PercentageHealthRepairManager) fccf
									.getRepairManager()).getRawHealth());
					config.set(current + ".runtime", fccf.getRunningTime());
					config.set(current + ".selectedRecipe", fccf
							.getCurrentRecipe().getRecipeName());
				} else if (f instanceof Pipe) {
					Pipe p = (Pipe) f;
					config.set(current + ".type", "PIPE");
					config.set(current + ".runtime", p.getRunTime());
					List<String> mats = new LinkedList<String>();
					List<Material> materials = p.getAllowedMaterials();
					if (materials != null) {
						for (Material m : materials) {
							mats.add(m.toString());
						}
					}
					config.set(current + ".materials", mats);
				} else if (f instanceof Sorter) {
					Sorter s = (Sorter) f;
					config.set(current + ".runtime", s.getRunTime());
					config.set(current + ".type", "SORTER");
					for (BlockFace face : MultiBlockStructure.allBlockSides) {
						config.set(current + ".faces." + face.toString(), s
								.getItemsForSide(face)
								.getItemStackRepresentation().toArray());
					}
				}
			}
			config.save(saveFile);
		} catch (Exception e) {
			// In case anything goes wrong while saving we always keep the
			// latest valid backup
			plugin.severe("Fatal error while trying to save factory data");
			e.printStackTrace();
			saveFile.delete();
		}
	}

	private void configureLocation(ConfigurationSection config, Location loc) {
		String identifier = serializeLocation(loc);
		config.set(identifier + ".world", loc.getWorld().getName());
		config.set(identifier + ".x", loc.getBlockX());
		config.set(identifier + ".y", loc.getBlockY());
		config.set(identifier + ".z", loc.getBlockZ());
	}

	private String serializeLocation(Location loc) {
		return loc.getWorld().getName() + "#" + loc.getBlockX() + "#"
				+ loc.getBlockY() + "#" + loc.getBlockZ();
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
		YamlConfiguration config = YamlConfiguration
				.loadConfiguration(saveFile);
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			String type = current.getString("type");
			String name = current.getString("name");
			int runtime = current.getInt("runtime");
			List<Location> blocks = new LinkedList<Location>();
			for (String blockKey : current.getConfigurationSection("blocks")
					.getKeys(false)) {
				ConfigurationSection currSec = current.getConfigurationSection(
						"blocks").getConfigurationSection(blockKey);
				String worldName = currSec.getString("world");
				int x = currSec.getInt("x");
				int y = currSec.getInt("y");
				int z = currSec.getInt("z");
				World w = Bukkit.getWorld(worldName);
				blocks.add(new Location(w, x, y, z));
			}
			switch (type) {
			case "FCC":
				FurnCraftChestEgg egg = (FurnCraftChestEgg) eggs.get(name);
				if (egg == null) {
					plugin.warning("Save file contained factory named "
							+ name
							+ " , but no factory with this name was found in the config");
					continue;
				}
				int health = current.getInt("health");
				String selectedRecipe = current.getString("selectedRecipe");
				Factory fac = egg.revive(blocks, health, selectedRecipe,
						runtime);
				manager.addFactory(fac);
				counter++;
				break;
			case "PIPE":
				PipeEgg pipeEgg = (PipeEgg) eggs.get(name);
				List<Material> mats = new LinkedList<Material>();
				if (current.isSet("materials")) {
					for (String mat : current.getStringList("materials")) {
						mats.add(Material.valueOf(mat));
					}
				} else {
					mats = null;
				}
				Factory p = pipeEgg.revive(blocks, mats, runtime);
				manager.addFactory(p);
				counter++;
				break;
			case "SORTER":
				Map<BlockFace, ItemMap> assignments = new HashMap<BlockFace, ItemMap>();
				SorterEgg sorterEgg = (SorterEgg) eggs.get(name);
				for (String face : current.getConfigurationSection("faces")
						.getKeys(false)) {
					List<ItemStack> stacks = (List<ItemStack>) current
							.getConfigurationSection("faces").get(face);
					// it works, okay?
					ItemMap map = new ItemMap(stacks);
					assignments.put(BlockFace.valueOf(face), map);
				}
				Factory s = sorterEgg.revive(blocks, assignments, runtime);
				manager.addFactory(s);
				counter++;
				break;
			}
		}
		plugin.info("Loaded " + counter + " factory from save file");
	}
}
