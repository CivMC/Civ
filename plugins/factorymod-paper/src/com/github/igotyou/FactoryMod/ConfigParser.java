package com.github.igotyou.FactoryMod;

import java.util.ArrayList;
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

import com.github.igotyou.FactoryMod.listeners.NetherPortalListener;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.google.common.collect.Lists;

public class ConfigParser {
	private FactoryModPlugin plugin;

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
		//TODO disable experience
		boolean disableNether = config.getBoolean("disable_nether");
		if (disableNether) {
			plugin.getServer().getPluginManager()
					.registerEvents(new NetherPortalListener(), plugin);
		}
		int defaultUpdateTime = config.getInt("default_update_time");
		FactoryModManager manager = new FactoryModManager(plugin,
				factoryInteractionMaterial, citadelEnabled);
		handleEnabledAndDisabledRecipes(config
				.getConfigurationSection("crafting"));

		return manager;
	}

	public void handleEnabledAndDisabledRecipes(ConfigurationSection config) {
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

	public ItemMap parseItemMap(ConfigurationSection config) {
		ItemMap result = new ItemMap();
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			Material m = Material.valueOf(current.getString("material"));
			ItemStack toAdd = new ItemStack(m);
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

	private List<PotionEffect> parsePotionEffects(
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
