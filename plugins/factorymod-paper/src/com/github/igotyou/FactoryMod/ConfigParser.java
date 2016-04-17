package com.github.igotyou.FactoryMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseItemMap;
import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseTime;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.eggs.PipeEgg;
import com.github.igotyou.FactoryMod.eggs.SorterEgg;
import com.github.igotyou.FactoryMod.listeners.NetherPortalListener;
import com.github.igotyou.FactoryMod.recipes.AOERepairRecipe;
import com.github.igotyou.FactoryMod.recipes.CompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.DecompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.DeterministicEnchantingRecipe;
import com.github.igotyou.FactoryMod.recipes.FactoryMaterialReturnRecipe;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.recipes.PylonRecipe;
import com.github.igotyou.FactoryMod.recipes.RandomEnchantingRecipe;
import com.github.igotyou.FactoryMod.recipes.RandomOutputRecipe;
import com.github.igotyou.FactoryMod.recipes.RepairRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;
import com.github.igotyou.FactoryMod.utility.FactoryGarbageCollector;

public class ConfigParser {
	private FactoryMod plugin;
	private HashMap<String, IRecipe> recipes;
	private FactoryModManager manager;
	private int defaultUpdateTime;
	private ItemStack defaultFuel;
	private int defaultFuelConsumptionTime;
	private double defaultReturnRate;
	private HashMap<String, IFactoryEgg> upgradeEggs;
	private HashMap<IFactoryEgg, List<String>> recipeLists;
	private String defaultMenuFactory;
	private boolean useYamlIdentifers;

	public ConfigParser(FactoryMod plugin) {
		this.plugin = plugin;
	}

	/** 
	 * Parses the whole config and creates a manager containing everything that
	 * was parsed from the config
	 * 
	 * @return manager with everything contained in the config
	 */
	public FactoryModManager parse() {
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		boolean citadelEnabled = plugin.getServer().getPluginManager()
				.isPluginEnabled("Citadel");
		boolean nameLayerEnabled = plugin.getServer().getPluginManager()
				.isPluginEnabled("NameLayer");
		boolean logInventories = config.getBoolean("log_inventories", true);
		Material factoryInteractionMaterial = Material.STICK;
		try {
			factoryInteractionMaterial = Material.getMaterial(config
					.getString("factory_interaction_material", "STICK"));
		} catch (IllegalArgumentException iae) {
			plugin.warning(config.getString("factory_interaction_material") +
					" is not a valid material for factory_interaction_material");
		}
		boolean disableNether = config.getBoolean("disable_nether", false);
		if (disableNether) {
			plugin.getServer().getPluginManager()
					.registerEvents(new NetherPortalListener(), plugin);
		}
		useYamlIdentifers = config.getBoolean("use_recipe_yamlidentifiers", false);
		defaultUpdateTime = (int) parseTime(config.getString(
				"default_update_time", "5"));
		ItemMap dFuel = parseItemMap(config.getConfigurationSection("default_fuel"));
		if (dFuel.getTotalUniqueItemAmount() > 0) {
			defaultFuel = dFuel.getItemStackRepresentation().get(0);
		} else {
			plugin.warning("No default_fuel specified. Should be an ItemMap.");
		}
		defaultFuelConsumptionTime = (int) parseTime(config.getString(
				"default_fuel_consumption_intervall", "20"));
		defaultReturnRate = config.getDouble("default_return_rate", 0.0);
		int redstonePowerOn = config.getInt("redstone_power_on", 7);
		int redstoneRecipeChange = config.getInt("redstone_recipe_change", 2);
		long gracePeriod = 50 * parseTime(config
				.getString("break_grace_period"));
		long savingIntervall = parseTime(config.getString("saving_intervall", "15m"));
		//save factories on a regular base, unless disabled
		if (savingIntervall != -1) {
			Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
				@Override
				public void run() {
					FactoryMod.getManager().saveFactories();	
				}
			}, savingIntervall, savingIntervall);
		}
		defaultMenuFactory = config.getString("default_menu_factory");
		int globalPylonLimit = config.getInt("global_pylon_limit");
		PylonRecipe.setGlobalLimit(globalPylonLimit);
		Map <String,String> factoryRenames = parseRenames(config.getConfigurationSection("renames"));
		manager = new FactoryModManager(plugin, factoryInteractionMaterial,
				citadelEnabled, nameLayerEnabled, redstonePowerOn, redstoneRecipeChange,
				logInventories, gracePeriod, factoryRenames);
		handleEnabledAndDisabledRecipes(config
				.getConfigurationSection("crafting"));
		upgradeEggs = new HashMap<String, IFactoryEgg>();
		recipeLists = new HashMap<IFactoryEgg, List<String>>();
		parseFactories(config.getConfigurationSection("factories"));
		parseRecipes(config.getConfigurationSection("recipes"));
		assignRecipesToFactories();
		enableFactoryDecay(config);
		manager.calculateTotalSetupCosts();
		// Some recipes need references to factories and all factories need
		// references to recipes, so we parse all factories first, set their
		// recipes to null, store the names of the recipes in a map here, parse
		// the recipes which can already get the references to the factories and
		// then fix the recipe references for the factories
		plugin.info("Parsed complete config");
		return manager;
	}

	/**
	 * Parses all recipes and sorts them into a hashmap by their name so they
	 * are ready to assign them to factories
	 * 
	 * @param config
	 *            ConfigurationSection containing the recipe configurations
	 */
	private void parseRecipes(ConfigurationSection config) {
		recipes = new HashMap<String, IRecipe>();
		for (String key : config.getKeys(false)) {
			if (config.getConfigurationSection(key) == null) {
				plugin.warning("Found invalid section that should not exist at " + config.getCurrentPath() + key);
				continue;
			}
			IRecipe recipe = parseRecipe(config.getConfigurationSection(key));
			if (recipe == null) {
				plugin.warning(String.format("Recipe %s unable to be added.", key));
			} else {
				if (useYamlIdentifers) {
					recipes.put(key, recipe);
				}
				{
					recipes.put(recipe.getRecipeName(), recipe);
				}
			}
		}
	}

	/**
	 * Parses all factories
	 * 
	 * @param config
	 *            ConfigurationSection to parse the factories from
	 * @param defaultUpdate
	 *            default intervall in ticks how often factories update, each
	 *            factory can choose to define an own value or to use the
	 *            default instead
	 */
	private void parseFactories(ConfigurationSection config) {
		for (String key : config.getKeys(false)) {
			parseFactory(config.getConfigurationSection(key));
		}

	}

	/**
	 * Parses a single factory and turns it into a factory egg which is add to
	 * the manager
	 * 
	 * @param config
	 *            ConfigurationSection to parse the factory from
	 * @param defaultUpdate
	 *            default intervall in ticks how often factories update, each
	 *            factory can choose to define an own value or to use the
	 *            default instead
	 */
	private void parseFactory(ConfigurationSection config) {
		IFactoryEgg egg = null;
		String type = config.getString("type");
		if (type == null) {
			plugin.warning("No type specified for factory at " + config.getCurrentPath()+". Skipping it.");
			return;
		}
		switch (type) {
		case "FCC": // Furnace, chest, craftingtable
			egg = parseFCCFactory(config);
			if (egg == null) {
				break;
			}
			ItemMap setupCost = parseItemMap(config
					.getConfigurationSection("setupcost"));
			if (setupCost.getTotalUniqueItemAmount() > 0) {
				manager.addFactoryCreationEgg(FurnCraftChestStructure.class,
						setupCost, egg);
			} else {
				plugin.warning(String.format("FCC %s specified with no setup cost, skipping",
						egg.getName()));
			}
			break;
		case "FCCUPGRADE":
			egg = parseFCCFactory(config);
			if (egg == null) {
				break;
			}
			upgradeEggs.put(egg.getName(), egg);
			manager.addFactoryUpgradeEgg(egg);
			break;
		case "PIPE":
			egg = parsePipe(config);
			if (egg == null) {
				break;
			}
			ItemMap pipeSetupCost = parseItemMap(config
					.getConfigurationSection("setupcost"));
			if (pipeSetupCost.getTotalUniqueItemAmount() > 0) {
				manager.addFactoryCreationEgg(PipeStructure.class, pipeSetupCost,
						egg);
			} else {
				plugin.warning(String.format("PIPE %s specified with no setup cost, skipping",
						egg.getName()));
			}
			break;
		case "SORTER":
			egg = parseSorter(config);
			if (egg == null) {
				break;
			}
			ItemMap sorterSetupCost = parseItemMap(config
					.getConfigurationSection("setupcost"));
			if (sorterSetupCost.getTotalUniqueItemAmount() > 0) {
				manager.addFactoryCreationEgg(BlockFurnaceStructure.class,
					sorterSetupCost, egg);
			} else {
				plugin.warning(String.format("SORTER %s specified with no setup cost, skipping",
						egg.getName()));
			}
			break;
		default:
			plugin.severe("Could not identify factory type "
					+ config.getString("type"));
		}
		if (egg != null) {
			plugin.info("Parsed factory " + egg.getName());
		} else {
			plugin.warning(String.format("Failed to set up factory %s", config.getCurrentPath()));
		}

	}

	public SorterEgg parseSorter(ConfigurationSection config) {
		String name = config.getString("name");
		double returnRate;
		if (config.contains("return_rate")) {
			returnRate = config.getDouble("return_rate");
		} else {
			returnRate = defaultReturnRate;
		}
		int update;
		if (config.contains("updatetime")) {
			update = (int) parseTime(config.getString("updatetime"));
		} else {
			update = defaultUpdateTime;
		}
		ItemStack fuel;
		if (config.contains("fuel")) {
			ItemMap tfuel = parseItemMap(config.getConfigurationSection("fuel"));
			if (tfuel.getTotalUniqueItemAmount() > 0) {
				fuel = tfuel.getItemStackRepresentation().get(0);
			} else {
				plugin.warning("Custom fuel was specified incorrectly for " + name);
				fuel = defaultFuel;
			}
		} else {
			fuel = defaultFuel;
		}
		int fuelIntervall;
		if (config.contains("fuel_consumption_intervall")) {
			fuelIntervall = (int) parseTime(config
					.getString("fuel_consumption_intervall"));
		} else {
			fuelIntervall = defaultFuelConsumptionTime;
		}
		int sortTime = (int) parseTime(config.getString("sort_time"));
		int sortamount = config.getInt("sort_amount");
		int matsPerSide = config.getInt("maximum_materials_per_side");
		return new SorterEgg(name, update, fuel, fuelIntervall, sortTime,
				matsPerSide, sortamount, returnRate);
	}

	public PipeEgg parsePipe(ConfigurationSection config) {
		String name = config.getString("name");
		double returnRate;
		if (config.contains("return_rate")) {
			returnRate = config.getDouble("return_rate");
		} else {
			returnRate = defaultReturnRate;
		}
		int update;
		if (config.contains("updatetime")) {
			update = (int) parseTime(config.getString("updatetime"));
		} else {
			update = defaultUpdateTime;
		}
		ItemStack fuel;
		if (config.contains("fuel")) {
			ItemMap tfuel = parseItemMap(config.getConfigurationSection("fuel"));
			if (tfuel.getTotalUniqueItemAmount() > 0) {
				fuel = tfuel.getItemStackRepresentation().get(0);
			} else {
				plugin.warning("Custom fuel was specified incorrectly for " + name);
				fuel = defaultFuel;
			}
		} else {
			fuel = defaultFuel;
		}
		int fuelIntervall;
		if (config.contains("fuel_consumption_intervall")) {
			fuelIntervall = (int) parseTime(config
					.getString("fuel_consumption_intervall"));
		} else {
			fuelIntervall = defaultFuelConsumptionTime;
		}
		int transferTimeMultiplier = (int) parseTime(config
				.getString("transfer_time_multiplier"));
		int transferAmount = config.getInt("transfer_amount");
		byte color = (byte) config.getInt("glass_color");
		return new PipeEgg(name, update, fuel, fuelIntervall, null,
				transferTimeMultiplier, transferAmount, color, returnRate);
	}

	public IFactoryEgg parseFCCFactory(ConfigurationSection config) {
		String name = config.getString("name");
		double returnRate;
		if (config.contains("return_rate")) {
			returnRate = config.getDouble("return_rate");
		} else {
			returnRate = defaultReturnRate;
		}
		int update;
		if (config.contains("updatetime")) {
			update = (int) parseTime(config.getString("updatetime"));
		} else {
			update = defaultUpdateTime;
		}
		ItemStack fuel;
		if (config.contains("fuel")) {
			ItemMap tfuel = parseItemMap(config.getConfigurationSection("fuel"));
			if (tfuel.getTotalUniqueItemAmount() > 0) {
				fuel = tfuel.getItemStackRepresentation().get(0);
			} else {
				plugin.warning("Custom fuel was specified incorrectly for " + name);
				fuel = defaultFuel;
			}
		} else {
			fuel = defaultFuel;
		}
		int fuelIntervall;
		if (config.contains("fuel_consumption_intervall")) {
			fuelIntervall = (int) parseTime(config
					.getString("fuel_consumption_intervall"));
		} else {
			fuelIntervall = defaultFuelConsumptionTime;
		}
		FurnCraftChestEgg egg = new FurnCraftChestEgg(name, update, null, fuel,
				fuelIntervall, returnRate);
		recipeLists.put(egg, config.getStringList("recipes"));
		return egg;
	}

	public void enableFactoryDecay(ConfigurationSection config) {
		long interval = parseTime(config.getString("decay_intervall"));
		int amount = config.getInt("decay_amount");
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
						new FactoryGarbageCollector(amount), interval, interval);
	}

	/**
	 * Disables and enables crafting recipes as specified in the config
	 * 
	 * @param config
	 *            ConfigurationSection to parse from
	 */
	private void handleEnabledAndDisabledRecipes(ConfigurationSection config) {
		// Disabling recipes
		List<Recipe> toDisable = new ArrayList<Recipe>();
		ItemMap disabledRecipes = parseItemMap(config
				.getConfigurationSection("disabled"));
		for (ItemStack recipe : disabledRecipes.getItemStackRepresentation()) {
			plugin.info("Attempting to disable recipes for "
					+ recipe.toString());
			List<Recipe> tempList = plugin.getServer().getRecipesFor(recipe);
			for (Recipe potential : tempList) {
				if (potential.getResult().isSimilar(recipe)) {
					plugin.info("Found a disable recipe match "
							+ potential.toString());
					toDisable.add(potential);
				}
			}
		}
		Iterator<Recipe> it = plugin.getServer().recipeIterator();
		while (it.hasNext()) {
			Recipe recipe = it.next();
			for (Recipe disable : toDisable) {
				if (disable.getResult().isSimilar(recipe.getResult())) {
					it.remove();
					plugin.info("Disabling recipe "	+ recipe.getResult().toString());
				}
			}
		}

		// TODO enable shaped and unshaped recipes here
	}

	/**
	 * Parses a single recipe
	 * 
	 * @param config
	 *            ConfigurationSection to parse the recipe from
	 * @return The recipe created based on the data parse
	 */
	private IRecipe parseRecipe(ConfigurationSection config) {
		IRecipe result;
		String name = config.getString("name");
		if (name == null) {
			plugin.warning("No name specified for recipe at " + config.getCurrentPath() +". Skipping the recipe.");
			return null;
		}
		String prodTime = config.getString("production_time");
		if (prodTime == null) {
			plugin.warning("No production time specied for recipe " + name + ". Skipping it");
			return null;
		}
		int productionTime = (int) parseTime(prodTime);
		String type = config.getString("type");
		if (type == null) {
			plugin.warning("No name specified for recipe at " + config.getCurrentPath() +". Skipping the recipe.");
			return null;
		}
		switch (type) {
		case "PRODUCTION":
			ItemMap input = parseItemMap(config
					.getConfigurationSection("input"));
			ItemMap output = parseItemMap(config
					.getConfigurationSection("output"));
			result = new ProductionRecipe(name, productionTime, input, output);
			break;
		case "COMPACT":
			ItemMap extraMats = parseItemMap(config
					.getConfigurationSection("input"));
			String compactedLore = config.getString("compact_lore");
			manager.setCompactLore(compactedLore);
			List<Material> excluded = new LinkedList<Material>();
			for (String mat : config.getStringList("excluded_materials")) {
				try {
					excluded.add(Material.valueOf(mat));
				} catch (IllegalArgumentException iae) {
					plugin.warning(mat + " is not a valid material to exclude: " + config.getCurrentPath());
				}
			}
			result = new CompactingRecipe(extraMats, excluded, name,
					productionTime, compactedLore);
			break;
		case "DECOMPACT":
			ItemMap extraMate = parseItemMap(config
					.getConfigurationSection("input"));
			String decompactedLore = config.getString("compact_lore");
			result = new DecompactingRecipe(extraMate, name, productionTime,
					decompactedLore);
			break;
		case "REPAIR":
			ItemMap rep = parseItemMap(config.getConfigurationSection("input"));
			int health = config.getInt("health_gained");
			result = new RepairRecipe(name, productionTime, rep, health);
			break;
		case "UPGRADE":
			ItemMap upgradeCost = parseItemMap(config
					.getConfigurationSection("input"));
			String upgradeName = config.getString("factory");
			IFactoryEgg egg = upgradeEggs.get(upgradeName);
			if (egg == null) {
				plugin.severe("Could not find factory " + upgradeName
						+ " for upgrade recipe " + name);
				result = null;
			} else {
				result = new Upgraderecipe(name, productionTime, upgradeCost, egg);
			}
			break;
		case "AOEREPAIR":
			ItemMap tessence = parseItemMap(
					config.getConfigurationSection("essence"));
			if (tessence.getTotalUniqueItemAmount() > 0){
				ItemStack essence = tessence
						.getItemStackRepresentation().get(0);
				int repPerEssence = config.getInt("repair_per_essence");
				int range = config.getInt("range");
				result = new AOERepairRecipe(name, productionTime, essence, range,
						repPerEssence);
			} else {
				plugin.severe("No essence specified for AOEREPAIR " + config.getCurrentPath());
				result = null;
			}
			break;
		case "PYLON":
			ItemMap in = parseItemMap(config
					.getConfigurationSection("input"));
			ItemMap out = parseItemMap(config
					.getConfigurationSection("output"));
			int weight = config.getInt("weight");
			result = new PylonRecipe(name, productionTime, in, out, weight);
			break;
		case "ENCHANT":
			ItemMap inp = parseItemMap(config
					.getConfigurationSection("input"));
			Enchantment enchant = Enchantment.getByName(config.getString("enchant"));
			int level = config.getInt("level", 1);
			ItemMap tool = parseItemMap(config.getConfigurationSection("enchant_item"));
			result = new DeterministicEnchantingRecipe(name, productionTime, inp, tool, enchant, level);
			break;
		case "RANDOM":
			ItemMap inpu = parseItemMap(config.getConfigurationSection("input"));
			ConfigurationSection outputSec = config.getConfigurationSection("outputs");
			if (outputSec == null) {
				plugin.severe("No outputs specified for recipe " + name);
				return null;
			}
			Map <ItemMap, Double> outputs = new HashMap<ItemMap, Double>();
			double totalChance = 0.0;
			String displayMap = outputSec.getString("display");
			ItemMap displayThis = null;
			for(String key : outputSec.getKeys(false)) {
				ConfigurationSection keySec = outputSec.getConfigurationSection(key);
				if (keySec != null) {
					double chance = keySec.getDouble("chance");
					totalChance += chance;
					ItemMap im = parseItemMap(keySec);
					outputs.put(im,chance);
					if (key.equals(displayMap)) {
						displayThis = im;
						plugin.debug("Displaying " + displayMap + " as recipe label");
					}
				}
			}
			if (Math.abs(totalChance - 1.0) > 0.001) {
				plugin.warning("Sum of output chances for recipe " + name + " is not 1.0. Total sum is: " + totalChance);
			}
			result = new RandomOutputRecipe(name, productionTime, inpu, outputs, displayThis);
			break;
		case "COSTRETURN":
			ItemMap costIn = parseItemMap(config
					.getConfigurationSection("input"));
			double factor = config.getDouble("factor", 1.0);
			result = new FactoryMaterialReturnRecipe(name, productionTime, costIn, factor);
			break;
		default:
			plugin.severe("Could not identify type " + config.getString("type")
					+ " as a valid recipe identifier");
			result = null;
		}
		if (result != null) {
			((InputRecipe)result).setFuelConsumptionIntervall((int)parseTime(config.getString("fuel_consumption_intervall", "-1")));
			plugin.info("Parsed recipe " + name);
		}
		return result;
	}
	
	private Map <String,String> parseRenames(ConfigurationSection config) {
		Map <String,String> renames = new TreeMap<String, String>();
		if (config != null) {
			for(String key : config.getKeys(false)) {
				String oldName = config.getConfigurationSection(key).getString("oldName");
				if (oldName == null) {
					plugin.warning("No old name specified for factory rename at " + config.getConfigurationSection(key).getCurrentPath());
				}
				String newName = config.getConfigurationSection(key).getString("newName");
				if (newName == null) {
					plugin.warning("No new name specified for factory rename at " + config.getConfigurationSection(key).getCurrentPath());
				}
				renames.put(oldName, newName);
			}
		}
		return renames;
	}

	public void assignRecipesToFactories() {
		HashSet <IRecipe> usedRecipes = new HashSet<IRecipe>();
		for (Entry<IFactoryEgg, List<String>> entry : recipeLists.entrySet()) {
			if (entry.getKey() instanceof FurnCraftChestEgg) {
				List<IRecipe> recipeList = new LinkedList<IRecipe>();
				for (String recipeName : entry.getValue()) {
					IRecipe rec = recipes.get(recipeName);
					if (rec != null) {
						recipeList.add(rec);
						usedRecipes.add(rec);
					}
					else {
						plugin.severe("Could not find specified recipe " + recipeName 
								+ " for factory " + entry.getKey().getName());
					}
				}
				((FurnCraftChestEgg) entry.getKey()).setRecipes(recipeList);
			}
		}
		for(IRecipe reci : recipes.values()) {
			if (!usedRecipes.contains(reci)) {
				plugin.warning("The recipe " + reci.getRecipeName() + " is specified in the config, but not used in any factory");
			}
		}
	}
	
	public String getDefaultMenuFactory() {
		return defaultMenuFactory;
	}
}
