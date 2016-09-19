package com.github.igotyou.FactoryMod.utility;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;

public class FileHandler {
	private FactoryMod plugin;
	private FactoryModManager manager;
	private File saveFile;
	private File backup;
	
	private Map <String, String> factoryRenames;
	
	private static int saveFileVersion = 2;

	public FileHandler(FactoryModManager manager, Map <String, String> factoryRenames) {
		plugin = FactoryMod.getPlugin();
		this.factoryRenames = factoryRenames;
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
			config.set("version", saveFileVersion);
			for (Factory f : factories) {
				String current = serializeLocation(f.getMultiBlockStructure()
						.getCenter());
				config.set(current + ".name", f.getName());
				ConfigurationSection blockSection = config.getConfigurationSection(current).createSection("blocks");
				configureLocation(blockSection, f.getMultiBlockStructure().getAllBlocks());
				if (f instanceof FurnCraftChestFactory) {
					FurnCraftChestFactory fccf = (FurnCraftChestFactory) f;
					config.set(current + ".type", "FCC");
					config.set(current + ".health",
							((PercentageHealthRepairManager) fccf
									.getRepairManager()).getRawHealth());
					config.set(current + ".breakTime",
							((PercentageHealthRepairManager) fccf
									.getRepairManager()).getBreakTime());
					config.set(current + ".runtime", fccf.getRunningTime());
					config.set(current + ".selectedRecipe", fccf
							.getCurrentRecipe().getName());
					List <String> recipeList = new LinkedList<String>();
					for(IRecipe rec : fccf.getRecipes()) {
						recipeList.add(rec.getIdentifier());
					}
					config.set(current + ".recipes", recipeList);
					if (fccf.getActivator() == null) {
						config.set(current + ".activator", "null");
					}
					else {
						config.set(current + ".activator", fccf.getActivator().toString());
					}
					for(IRecipe i : ((FurnCraftChestFactory) f).getRecipes()) {
						config.set(current + ".runcounts." + i.getName(), fccf.getRunCount(i));
						config.set(current + ".recipeLevels." + i.getName(), fccf.getRecipeLevel(i));
					}
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
			plugin.info("Successfully saved factory data");
		} catch (Exception e) {
			// In case anything goes wrong while saving we always keep the
			// latest valid backup
			plugin.severe("Fatal error while trying to save factory data");
			e.printStackTrace();
			saveFile.delete();
		}
	}

	private void configureLocation(ConfigurationSection config, List <Location> locations) {
		int count = 0;
		for(Location loc : locations) {
			String identifier = "a" + count++ + serializeLocation(loc);
			config.set(identifier + ".world", loc.getWorld().getName());
			config.set(identifier + ".x", loc.getBlockX());
			config.set(identifier + ".y", loc.getBlockY());
			config.set(identifier + ".z", loc.getBlockZ());
		}
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
		int loadedVersion = config.getInt("version", 1);
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				continue;
			}
			String type = current.getString("type");
			String name = current.getString("name");
			int runtime = current.getInt("runtime");
			List<Location> blocks = new LinkedList<Location>();
			Set <String> blockKeys = current.getConfigurationSection("blocks").getKeys(false);
			Collections.sort(new LinkedList <String> (blockKeys));
			for (String blockKey : blockKeys) {
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
				if (loadedVersion == 1) {
					//need to sort the locations properly, because they werent previously
					List <Location> sortedList = new LinkedList<Location>();
					int totalX = 0;
					int totalY = 0;
					int totalZ = 0;
					for(Location loc : blocks) {
						totalX += loc.getBlockX();
						totalY += loc.getBlockY();
						totalZ += loc.getBlockZ();
					}
					Location center = new Location(blocks.get(0).getWorld(), totalX / 3, totalY / 3, totalZ / 3);
					if (!blocks.contains(center)) {
						plugin.warning("Failed to convert location for factory at " + blocks.get(0).toString() + "; calculated center: " + center.toString());
					}
					else {
					blocks.remove(center);
					sortedList.add(center);
					//we cant guarantee that this will work, it might very well fail for partially broken factories, but it's the best thing I got
						if (blocks.get(0).getBlock().getType() == Material.CHEST) {
							sortedList.add(blocks.get(1));
							sortedList.add(blocks.get(0));
						}
						else {
							sortedList.add(blocks.get(0));
							sortedList.add(blocks.get(1));
						}
						blocks = sortedList;
					}
					
					
				}
				FurnCraftChestEgg egg = (FurnCraftChestEgg) eggs.get(name);
				if (egg == null) {
					String replaceName = factoryRenames.get(name);
					if (replaceName != null) {
						egg = (FurnCraftChestEgg) eggs.get(replaceName);
					}
					if (egg == null) {
						plugin.warning("Save file contained factory named "
								+ name
								+ " , but no factory with this name was found in the config");
						continue;
					}
					else {
						name = replaceName;
					}
				}
				int health = current.getInt("health");
				long breakTime = current.getLong("breakTime", 0);
				String selectedRecipe = current.getString("selectedRecipe");
				List <String> recipes = current.getStringList("recipes");
				if (recipes == null) {
					recipes = new LinkedList<String>();
				}
				FurnCraftChestFactory fac = (FurnCraftChestFactory) egg.revive(blocks, health, selectedRecipe,
						runtime, breakTime, recipes);
				String activator = current.getString("activator", "null");
				UUID acti;
				if (activator.equals("null")) {
					acti = null;
				}
				else {
					acti = UUID.fromString(activator);
				}
				fac.setActivator(acti);
				ConfigurationSection runCounts = current.getConfigurationSection("runcounts");
				if(runCounts != null) {
					for(String countKey : runCounts.getKeys(false)) {
						int runs = runCounts.getInt(countKey);
						for(IRecipe r : fac.getRecipes()) {
							if (r.getName().equals(countKey)) {
								fac.setRunCount(r, runs);
								break;
							}
						}
					}
				}
				ConfigurationSection recipeLevels = current.getConfigurationSection("recipeLevels");
				if(recipeLevels != null) {
					for(String countKey : recipeLevels.getKeys(false)) {
						int runs = recipeLevels.getInt(countKey);
						for(IRecipe r : fac.getRecipes()) {
							if (r.getName().equals(countKey)) {
								fac.setRecipeLevel(r, runs);
								break;
							}
						}
					}
				}
				manager.addFactory(fac);
				counter++;
				break;
			case "PIPE":
				PipeEgg pipeEgg = (PipeEgg) eggs.get(name);
				if (pipeEgg == null) {
					String replaceName = factoryRenames.get(name);
					if (replaceName != null) {
						pipeEgg = (PipeEgg) eggs.get(replaceName);
					}
					if (pipeEgg == null) {
						plugin.warning("Save file contained factory named "
								+ name
								+ " , but no factory with this name was found in the config");
						continue;
					}
					else {
						name = replaceName;
					}
				}
				List<Material> mats = new LinkedList<Material>();
				if (current.isSet("materials")) {
					for (String mat : current.getStringList("materials")) {
						mats.add(Material.valueOf(mat));
					}
				} else {
					mats = null;
				}
				if (mats.size() == 0) {
					mats = null;
				}
				Factory p = pipeEgg.revive(blocks, mats, runtime);
				manager.addFactory(p);
				counter++;
				break;
			case "SORTER":
				Map<BlockFace, ItemMap> assignments = new HashMap<BlockFace, ItemMap>();
				SorterEgg sorterEgg = (SorterEgg) eggs.get(name);
				if (sorterEgg == null) {
					String replaceName = factoryRenames.get(name);
					if (replaceName != null) {
						sorterEgg = (SorterEgg) eggs.get(replaceName);
					}
					if (sorterEgg == null) {
						plugin.warning("Save file contained factory named "
								+ name
								+ " , but no factory with this name was found in the config");
						continue;
					}
					else {
						name = replaceName;
					}
				}
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
