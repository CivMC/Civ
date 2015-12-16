package com.github.igotyou.FactoryMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.listeners.NetherPortalListener;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.recipes.CompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.DecompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.recipes.RepairRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.google.common.collect.Lists;

public class ConfigParser {
	private FactoryModPlugin plugin;
	private HashMap<String, IRecipe> recipes;
	private FactoryModManager manager;
	private int defaultUpdateTime;
	private ItemMap defaultFuel;
	private int defaultFuelConsumptionTime;

	public ConfigParser(FactoryModPlugin plugin) {
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
		boolean citadelEnabled = config.getBoolean("citadel_enabled", true);
		Material factoryInteractionMaterial = Material.getMaterial(config
				.getString("factory_interaction_material", "STICK"));
		// TODO disable experience
		boolean disableNether = config.getBoolean("disable_nether", false);
		if (disableNether) {
			plugin.getServer().getPluginManager()
					.registerEvents(new NetherPortalListener(), plugin);
		}
		defaultUpdateTime = (int) parseTime(config.getString(
				"default_update_time", "5"));
		defaultFuel = parseItemMap(config
				.getConfigurationSection("default_fuel"));
		defaultFuelConsumptionTime = (int) parseTime(config.getString(
				"default_fuel_consumption_intervall", "20"));
		int redstonePowerOn = config.getInt("redstone_power_on", 7);
		int redstoneRecipeChange = config.getInt("redstone_recipe_change", 2);
		manager = new FactoryModManager(plugin, factoryInteractionMaterial,
				citadelEnabled, redstonePowerOn, redstoneRecipeChange);
		handleEnabledAndDisabledRecipes(config
				.getConfigurationSection("crafting"));
		parseRecipes(config.getConfigurationSection("recipes"));
		parseFactories(config.getConfigurationSection("factories"));
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
			IRecipe recipe = parseRecipe(config.getConfigurationSection(key));
			recipes.put(recipe.getRecipeName(), recipe);
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
		switch (config.getString("type")) {
		case "FCC": // Furnace, chest, craftingtable
			egg = parseFactoryEgg(config);
			ItemMap setupCost = parseItemMap(config
					.getConfigurationSection("setupcost"));
			manager.addFactoryCreationEgg(FurnCraftChestStructure.class,
					setupCost, egg);
			break;

		default:
			plugin.severe("Could not identify factory type "
					+ config.getString("type"));
		}
		plugin.info("Parsed factory " + egg.getName());

	}

	public IFactoryEgg parseFactoryEgg(ConfigurationSection config) {
		String name = config.getString("name");
		switch (config.getString("type")) {
		case "FCC": // Furnace, chest, craftingtable
			int update;
			if (config.contains("updatetime")) {
				update = (int)parseTime(config.getString("updatetime"));
			} else {
				update = defaultUpdateTime;
			}
			List<IRecipe> recipeList = new LinkedList<IRecipe>();
			for (String recipe : config.getStringList("recipes")) {
				recipeList.add(recipes.get(recipe));
			}
			ItemMap fuel;
			if (config.contains("fuel")) {
				fuel = parseItemMap(config.getConfigurationSection("fuel"));
			} else {
				fuel = defaultFuel;
			}
			int fuelIntervall;
			if (config.contains("fuel_consumption_intervall")) {
				fuelIntervall = (int)parseTime(config.getString("fuel_consumption_intervall"));
			} else {
				fuelIntervall = defaultFuelConsumptionTime;
			}
			FurnCraftChestEgg egg = new FurnCraftChestEgg(name, update,
					recipeList, fuel, fuelIntervall);
			return egg;
		default:
			plugin.severe("Could not identify factory type "
					+ config.getString("type"));
			return null;
		}
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
					plugin.info("Disabling recipe "
							+ recipe.getResult().toString());
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
		int productionTime = (int)parseTime(config.getString("production_time"));
		switch (config.getString("type")) {
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
				excluded.add(Material.valueOf(mat));
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
			IFactoryEgg egg = parseFactoryEgg(config
					.getConfigurationSection("factory"));
			result = new Upgraderecipe(name, productionTime, upgradeCost, egg);
			break;
		default:
			plugin.severe("Could not identify type " + config.getString("type")
					+ " as a valid recipe identifier");
			result = null;
		}
		plugin.info("Parsed recipe " + name);
		return result;
	}

	/**
	 * Creates an itemmap containing all the items listed in the given config
	 * section
	 * 
	 * @param config
	 *            ConfigurationSection to parse the items from
	 * @return The item map created
	 */
	private static ItemMap parseItemMap(ConfigurationSection config) {
		ItemMap result = new ItemMap();
		if (config == null) {
			return result;
		}
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			Material m = Material.valueOf(current.getString("material"));
			ItemStack toAdd = new ItemStack(m);
			int amount = current.getInt("amount", 1);
			toAdd.setAmount(amount);
			int durability = current.getInt("durability", 0);
			toAdd.setDurability((short) durability);
			ItemMeta im = toAdd.getItemMeta();
			String name = current.getString("name");
			if (name != null) {
				im.setDisplayName(name);
			}
			String lore = current.getString("lore");
			if (lore != null) {
				List<String> loreList = new LinkedList<String>();
				loreList.add(lore);
				im.setLore(loreList);
			}
			if (current.contains("enchants")) {
				for (String enchantKey : current.getConfigurationSection(
						"enchants").getKeys(false)) {
					ConfigurationSection enchantConfig = current
							.getConfigurationSection("enchants")
							.getConfigurationSection(enchantKey);
					Enchantment enchant = Enchantment.getByName(enchantConfig
							.getString("enchant"));
					int level = enchantConfig.getInt("level", 1);
					im.addEnchant(enchant, level, true);
				}
			}
			toAdd.setItemMeta(im);
			result.addItemStack(toAdd);
		}
		return result;
	}

	/**
	 * Parses a potion effect
	 * 
	 * @param configurationSection
	 *            ConfigurationSection to parse the effect from
	 * @return The potion effect parsed
	 */
	private static List<PotionEffect> parsePotionEffects(
			ConfigurationSection configurationSection) {
		List<PotionEffect> potionEffects = Lists.newArrayList();
		if (configurationSection != null) {
			for (String name : configurationSection.getKeys(false)) {
				ConfigurationSection configEffect = configurationSection
						.getConfigurationSection(name);
				String type = configEffect.getString("type");
				PotionEffectType effect = PotionEffectType.getByName(type);
				int duration = configEffect.getInt("duration", 200);
				int amplifier = configEffect.getInt("amplifier", 0);
				potionEffects
						.add(new PotionEffect(effect, duration, amplifier));
			}
		}
		return potionEffects;
	}

	private long parseTime(String arg) {
		long result = 0;
		boolean set = true;
		try {
			result += Long.parseLong(arg);
		} catch (NumberFormatException e) {
			set = false;
		}
		if (set) {
			return result;
		}
		while (!arg.equals("")) {
			int length = 0;
			switch (arg.charAt(arg.length() - 1)) {
			case 't': // ticks
				long ticks = getLastNumber(arg);
				result += ticks;
				length = String.valueOf(ticks).length() + 1;
				break;
			case 's': // seconds
				long seconds = getLastNumber(arg);
				result += 20 * seconds; // 20 ticks in a second
				length = String.valueOf(seconds).length() + 1;
				break;
			case 'm': // minutes
				long minutes = getLastNumber(arg);
				result += 20 * 60 * minutes;
				length = String.valueOf(minutes).length() + 1;
				break;
			case 'h': // hours
				long hours = getLastNumber(arg);
				result += 20 * 3600 * hours;
				length = String.valueOf(hours).length() + 1;
				break;
			case 'd': // days, mostly here to define a 'never'
				long days = getLastNumber(arg);
				result += 20 * 3600 * 24 * days;
				length = String.valueOf(days).length() + 1;
			default:
				plugin.severe("Invalid time value in config:" + arg);
			}
			arg = arg.substring(0, arg.length() - length);
		}
		return result;
	}

	private long getLastNumber(String arg) {
		StringBuilder number = new StringBuilder();
		for (int i = arg.length() - 2; i >= 0; i--) {
			if (Character.isDigit(arg.charAt(i))) {
				number.insert(0, arg.substring(i, i + 1));
			} else {
				break;
			}
		}
		long result = Long.parseLong(number.toString());
		return result;
	}

}
