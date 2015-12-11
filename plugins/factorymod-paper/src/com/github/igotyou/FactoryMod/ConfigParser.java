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
import com.github.igotyou.FactoryMod.listeners.NetherPortalListener;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.recipes.CompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.DecompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.recipes.RepairRecipe;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.google.common.collect.Lists;

public class ConfigParser {
	private FactoryModPlugin plugin;
	private HashMap<String, IRecipe> recipes;
	private FactoryModManager manager;

	public ConfigParser(FactoryModPlugin plugin) {
		this.plugin = plugin;
	}

	public FactoryModManager parse() {
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		boolean citadelEnabled = config.getBoolean("citadel_enabled", true);
		Material factoryInteractionMaterial = Material.getMaterial(config
				.getString("factory_interaction_material", "STICK"));
		boolean disableExperience = config.getBoolean("disable_experience",
				false);
		// TODO disable experience
		boolean disableNether = config.getBoolean("disable_nether", false);
		if (disableNether) {
			plugin.getServer().getPluginManager()
					.registerEvents(new NetherPortalListener(), plugin);
		}
		int defaultUpdateTime = config.getInt("default_update_time", 4);
		manager = new FactoryModManager(plugin, factoryInteractionMaterial,
				citadelEnabled);
		handleEnabledAndDisabledRecipes(config
				.getConfigurationSection("crafting"));
		parseRecipes(config.getConfigurationSection("recipes"));
		parseFactories(config.getConfigurationSection("factories"),
				defaultUpdateTime);
		plugin.info("Parsed complete config");
		return manager;
	}

	private void parseRecipes(ConfigurationSection config) {
		recipes = new HashMap<String, IRecipe>();
		for (String key : config.getKeys(false)) {
			IRecipe recipe = parseRecipe(config.getConfigurationSection(key));
			recipes.put(recipe.getRecipeName(), recipe);
		}
	}

	private void parseFactories(ConfigurationSection config, int defaultUpdate) {
		for (String key : config.getKeys(false)) {
			parseFactory(config.getConfigurationSection(key), defaultUpdate);
		}

	}

	private void parseFactory(ConfigurationSection config, int defaultUpdate) {
		String name = config.getString("name");
		// One implementation for each egg here
		switch (config.getString("type")) {
		case "FCC": // Furnace, chest, craftingtable
			int update;
			if (config.contains("updatetime")) {
				update = config.getInt("updatetime");
			} else {
				update = defaultUpdate;
			}
			List<IRecipe> recipeList = new LinkedList<IRecipe>();
			for (String recipe : config.getStringList("recipes")) {
				recipeList.add(recipes.get(recipe));
			}
			ItemMap fuel = parseItemMap(config.getConfigurationSection("fuel"));
			int fuelIntervall = config.getInt("fuel_consumption_intervall");
			ItemMap setupCost = parseItemMap(config
					.getConfigurationSection("setupcost"));
			FurnCraftChestEgg egg = new FurnCraftChestEgg(name, update,
					recipeList, fuel, fuelIntervall);
			manager.addFactoryEgg(FurnCraftChestStructure.class, setupCost, egg);
			break;

		default:
			plugin.severe("Could not identify factory type "
					+ config.getString("type"));
		}
		plugin.info("Parsed factory "+name);

	}

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

	private IRecipe parseRecipe(ConfigurationSection config) {
		IRecipe result;
		String name = config.getString("name");
		int productionTime = config.getInt("production_time");
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
		default:
			plugin.severe("Could not identify type " + config.getString("type")
					+ " as a valid recipe identifier");
			result = null;
		}
		plugin.info("Parsed recipe "+name);
		return result;
	}

	private static ItemMap parseItemMap(ConfigurationSection config) {
		ItemMap result = new ItemMap();
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			Material m = Material.valueOf(current.getString("material"));
			ItemStack toAdd = new ItemStack(m);
			int amount = current.getInt("amount");
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
			if (config.contains("enchants")) {
				for (String enchantKey : current.getConfigurationSection(
						"enchants").getKeys(false)) {
					ConfigurationSection enchantConfig = current
							.getConfigurationSection("enchants")
							.getConfigurationSection(enchantKey);
					Enchantment enchant = Enchantment.getByName(enchantConfig
							.getString("enchant"));
					int level = enchantConfig.getInt("level");
					im.addEnchant(enchant, level, true);
				}
			}
			toAdd.setItemMeta(im);
			result.addItemStack(toAdd);
		}
		return result;
	}

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

}
