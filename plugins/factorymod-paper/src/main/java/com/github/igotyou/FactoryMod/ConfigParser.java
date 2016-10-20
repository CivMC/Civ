package com.github.igotyou.FactoryMod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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
import com.github.igotyou.FactoryMod.recipes.DummyParsingRecipe;
import com.github.igotyou.FactoryMod.recipes.FactoryMaterialReturnRecipe;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.LoreEnchantRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.recipes.PylonRecipe;
import com.github.igotyou.FactoryMod.recipes.RandomOutputRecipe;
import com.github.igotyou.FactoryMod.recipes.RecipeScalingUpgradeRecipe;
import com.github.igotyou.FactoryMod.recipes.RepairRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.github.igotyou.FactoryMod.recipes.scaling.ProductionRecipeModifier;
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
	private HashMap<RecipeScalingUpgradeRecipe, String []> recipeScalingUpgradeMapping;
	private String defaultMenuFactory;
	private long defaultBreakGracePeriod;
	private int defaultDamagePerBreakPeriod;
	private boolean useYamlIdentifers;
	private int defaultHealth;

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
		if (!useYamlIdentifers) {
			plugin.warning("You have usage of yaml identifiers turned off, names will be used instead to identify factories and recipes. This behavior"
					+ "is not recommended and not compatible with config inheritation");
		}
		defaultUpdateTime = (int) parseTime(config.getString(
				"default_update_time", "5"));
		defaultHealth = config.getInt("default_health", 10000);
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
		defaultBreakGracePeriod = 50 * parseTime(config
				.getString("default_break_grace_period"));
		defaultDamagePerBreakPeriod = config.getInt("default_decay_amount", 21);
		long savingIntervall = parseTime(config.getString("saving_intervall", "15m"));
		//save factories on a regular base, unless disabled
		if (savingIntervall != -1) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					FactoryMod.getManager().saveFactories();
					
				}
			}.runTaskTimerAsynchronously(plugin, savingIntervall, savingIntervall);
		}
		defaultMenuFactory = config.getString("default_menu_factory");
		int globalPylonLimit = config.getInt("global_pylon_limit");
		PylonRecipe.setGlobalLimit(globalPylonLimit);
		Map <String,String> factoryRenames = parseRenames(config.getConfigurationSection("renames"));
		manager = new FactoryModManager(plugin, factoryInteractionMaterial,
				citadelEnabled, nameLayerEnabled, redstonePowerOn, redstoneRecipeChange,
				logInventories, factoryRenames);
		upgradeEggs = new HashMap<String, IFactoryEgg>();
		recipeLists = new HashMap<IFactoryEgg, List<String>>();
		recipeScalingUpgradeMapping = new HashMap<RecipeScalingUpgradeRecipe, String[]>();
		parseFactories(config.getConfigurationSection("factories"));
		parseRecipes(config.getConfigurationSection("recipes"));
		assignRecipeScalingRecipes();
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
		List <String> recipeKeys = new LinkedList<String>();
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				plugin.warning("Found invalid section that should not exist at " + config.getCurrentPath() + key);
				continue;
			}
			recipeKeys.add(key);
		}
		while (!recipeKeys.isEmpty()) {
			String currentIdent = recipeKeys.get(0);
			ConfigurationSection current = config.getConfigurationSection(currentIdent);
			if (useYamlIdentifers) {
				//no support for inheritation when not using yaml identifiers
				boolean foundParent = false;
				while(!foundParent) {
					//keep track of already parsed sections, so we dont get stuck forever in cyclic dependencies
					List <String> children = new LinkedList<String>();
					children.add(currentIdent);
					if (current.isString("inherit")) {
						//parent is defined for this recipe
						String parent = current.getString("inherit");
						if (recipes.containsKey(parent)) {
							//we already parsed the parent, so parsing this recipe is fine
							foundParent = true;
						}
						else{
							if (!recipeKeys.contains(parent)) {
								//specified parent doesnt exist
								plugin.warning("The recipe " + currentIdent + " specified " + parent + " as parent, but this recipe could not be found");
								current = null;
								foundParent = true;
							}
							else {
								
								//specified parent exists, but wasnt parsed yet, so we do it first
								if (children.contains(parent)) {
									//cyclic dependency
									plugin.warning("The recipe " + currentIdent + " specified a cyclic dependency with parent " + parent + " it was skipped");
									current = null;
									foundParent = true;
									break;
								}
								currentIdent = parent;
								current = config.getConfigurationSection(parent);
							}
						}
					}
					else {
						//no parent is a parent as well
						foundParent = true;
					}
				}
			}
			recipeKeys.remove(currentIdent);
			if (current == null) {
				plugin.warning(String.format("Recipe %s unable to be added.", currentIdent));
				continue;
			}
			IRecipe recipe = parseRecipe(current);
			if (recipe == null) {
				plugin.warning(String.format("Recipe %s unable to be added.", currentIdent));
			} else {
				if (recipes.containsKey(recipe.getIdentifier())) {
					plugin.warning("Recipe identifier " + recipe.getIdentifier() + " was found twice in the config. One instance was skipped");
				}
				else {
					recipes.put(recipe.getIdentifier(), recipe);
					manager.registerRecipe(recipe);
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
		int maxLength = config.getInt("maximum_length");
		return new PipeEgg(name, update, fuel, fuelIntervall, null,
				transferTimeMultiplier, transferAmount, color, returnRate, maxLength);
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
		int health;
		if (config.contains("health")) {
			health = config.getInt("health");
		}
		else {
			health = defaultHealth;
		}
		int fuelIntervall;
		if (config.contains("fuel_consumption_intervall")) {
			fuelIntervall = (int) parseTime(config
					.getString("fuel_consumption_intervall"));
		} else {
			fuelIntervall = defaultFuelConsumptionTime;
		}
		long gracePeriod;
		if (config.contains("grace_period")) {
			//milliseconds
			gracePeriod = 50 * parseTime(config.getString("grace_period"));
		}
		else {
			gracePeriod = defaultBreakGracePeriod;
		}
		int healthPerDamageIntervall;
		if (config.contains("decay_amount")) {
			healthPerDamageIntervall = config.getInt("decay_amount");
		}
		else {
			healthPerDamageIntervall = defaultDamagePerBreakPeriod;
		}
		double citadelBreakReduction = config.getDouble("citadelBreakReduction", 1.0);
		FurnCraftChestEgg egg = new FurnCraftChestEgg(name, update, null, fuel,
				fuelIntervall, returnRate, health, gracePeriod, healthPerDamageIntervall, citadelBreakReduction);
		recipeLists.put(egg, config.getStringList("recipes"));
		return egg;
	}

	public void enableFactoryDecay(ConfigurationSection config) {
		long interval = parseTime(config.getString("decay_intervall"));
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
						new FactoryGarbageCollector(), interval, interval);
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
		IRecipe parentRecipe = null;
		if (config.isString("inherit") && useYamlIdentifers) {
			parentRecipe = recipes.get(config.get("inherit"));
		}
		String name = config.getString("name", (parentRecipe != null) ? parentRecipe.getName() : null);
		if (name == null) {
			plugin.warning("No name specified for recipe at " + config.getCurrentPath() +". Skipping the recipe.");
			return null;
		}
		//we dont inherit identifier, because that would make no sense
		String identifier = config.getString("identifier");
		if (identifier == null) {
			if (useYamlIdentifers) {
				identifier = config.getName();
			}
			else {
				identifier = name;
			}
		}
		String prodTime = config.getString("production_time");
		if (prodTime == null && parentRecipe == null) {
			plugin.warning("No production time specied for recipe " + name + ". Skipping it");
			return null;
		}
		int productionTime;
		if (parentRecipe == null) {
			productionTime = (int) parseTime(prodTime);
		}
		else {
			productionTime = parentRecipe.getProductionTime();
		}
		String type = config.getString("type", (parentRecipe != null) ? parentRecipe.getTypeIdentifier() : null);
		if (type == null) {
			plugin.warning("No type specified for recipe at " + config.getCurrentPath() +". Skipping the recipe.");
			return null;
		}
		ConfigurationSection inputSection = config.getConfigurationSection("input");
		ItemMap input;
		if (inputSection == null) {
			//no input specified, check parent
			if (!(parentRecipe instanceof InputRecipe)) {
				//default to empty input
				input = new ItemMap();
			}
			else {
				input = ((InputRecipe) parentRecipe).getInput();
			}
		}
		else {
			input = parseItemMap(inputSection);
		}
		switch (type) {
		case "PRODUCTION":
			ConfigurationSection outputSection = config.getConfigurationSection("output");
			ItemMap output;
			if (outputSection == null) {
				if (!(parentRecipe instanceof ProductionRecipe)) {
					output = new ItemMap();
				}
				else {
					output = ((ProductionRecipe) parentRecipe).getOutput();
				}
			}
			else {
				output = parseItemMap(outputSection);
			}
			ProductionRecipeModifier modi = parseProductionRecipeModifier(config.getConfigurationSection("modi"));
			if (modi == null && parentRecipe instanceof ProductionRecipe) {
				modi = ((ProductionRecipe) parentRecipe).getModifier().clone();
			}
			result = new ProductionRecipe(identifier, name, productionTime, input, output, modi);
			break;
		case "COMPACT":
			String compactedLore = config.getString("compact_lore", 
					(parentRecipe instanceof CompactingRecipe) ? ((CompactingRecipe)parentRecipe).getCompactedLore() : null);
			if (compactedLore == null) {
				plugin.warning("No special lore specified for compaction recipe " + name + " it was skipped");
				result = null;
				break;
			}
			manager.addCompactLore(compactedLore);
			List<Material> excluded = new LinkedList<Material>();
			if (config.isList("excluded_materials")) {
				for (String mat : config.getStringList("excluded_materials")) {
					try {
						excluded.add(Material.valueOf(mat));
					} catch (IllegalArgumentException iae) {
						plugin.warning(mat + " is not a valid material to exclude: " + config.getCurrentPath());
					}
				}
			}
			else {
				if (parentRecipe instanceof CompactingRecipe) {
					//copy so they are not using same instance
					for(Material m : ((CompactingRecipe) parentRecipe).getExcludedMaterials()) {
						excluded.add(m);
					}
				}
				//otherwise just leave list empty, as nothing is specified, which is fine
			}
			result = new CompactingRecipe(identifier, input, excluded, name,
					productionTime, compactedLore);
			break;
		case "DECOMPACT":
			String decompactedLore = config.getString("compact_lore", 
					(parentRecipe instanceof DecompactingRecipe) ? ((DecompactingRecipe)parentRecipe).getCompactedLore() : null);
			if (decompactedLore == null) {
				plugin.warning("No special lore specified for decompaction recipe " + name + " it was skipped");
				result = null;
				break;
			}
			manager.addCompactLore(decompactedLore);
			result = new DecompactingRecipe(identifier, input, name, productionTime, decompactedLore);
			break;
		case "REPAIR":
			int health = config.getInt("health_gained", 
					(parentRecipe instanceof RepairRecipe) ? ((RepairRecipe)parentRecipe).getHealth() : 0);
			if (health == 0) {
				plugin.warning("Health gained from repair recipe " + name + " is set to or was defaulted to 0, this might not be what was intended");
			}
			result = new RepairRecipe(identifier, name, productionTime, input, health);
			break;
		case "UPGRADE":
			String upgradeName = config.getString("factory");
			IFactoryEgg egg;
			if (upgradeName == null) {
				if (parentRecipe instanceof Upgraderecipe) {
					egg = ((Upgraderecipe) parentRecipe).getEgg();
				}
				else {
					egg = null;
				}
			}
			else {
				egg = upgradeEggs.get(upgradeName);
			}
			if (egg == null) {
				plugin.warning("Could not find factory " + upgradeName
						+ " for upgrade recipe " + name);
				result = null;
			} else {
				result = new Upgraderecipe(identifier, name, productionTime, input, egg);
			}
			break;
		case "AOEREPAIR":
			//This is untested and should not be used for now
			plugin.warning("This recipe is not tested or even completly developed, use it with great care and don't expect it to work");
			ItemMap tessence = parseItemMap(
					config.getConfigurationSection("essence"));
			if (tessence.getTotalUniqueItemAmount() > 0){
				ItemStack essence = tessence
						.getItemStackRepresentation().get(0);
				int repPerEssence = config.getInt("repair_per_essence");
				int range = config.getInt("range");
				result = new AOERepairRecipe(identifier, name, productionTime, essence, range,
						repPerEssence);
			} else {
				plugin.severe("No essence specified for AOEREPAIR " + config.getCurrentPath());
				result = null;
			}
			break;
		case "PYLON":
			ConfigurationSection outputSec = config.getConfigurationSection("output");
			ItemMap outputMap;
			if (outputSec == null) {
				if (!(parentRecipe instanceof PylonRecipe)) {
					outputMap = new ItemMap();
				}
				else {
					outputMap = ((PylonRecipe) parentRecipe).getOutput().clone();
				}
			}
			else {
				outputMap = parseItemMap(outputSec);
			}
			if (outputMap.getTotalItemAmount() == 0) {
				plugin.warning("Pylon recipe " + name + " has an empty output specified");
			}
			int weight = config.getInt("weight", 
					(parentRecipe instanceof PylonRecipe) ? ((PylonRecipe)parentRecipe).getWeight() : 20);
			result = new PylonRecipe(identifier, name, productionTime, input, outputMap, weight);
			break;
		case "ENCHANT":
			Enchantment enchant = Enchantment.getByName(config.getString("enchant", 
					(parentRecipe instanceof DeterministicEnchantingRecipe) ? ((DeterministicEnchantingRecipe)parentRecipe).getEnchant().getName() : null));
			if (enchant == null) {
				plugin.warning("No enchant specified for deterministic enchanting recipe " + name + ". It was skipped.");
				result = null;
				break;
			}
			int level = config.getInt("level", 
					(parentRecipe instanceof DeterministicEnchantingRecipe) ? ((DeterministicEnchantingRecipe)parentRecipe).getLevel() : 1);
			ConfigurationSection toolSection = config.getConfigurationSection("enchant_item");
			ItemMap tool;
			if (toolSection == null) {
				if (!(parentRecipe instanceof DeterministicEnchantingRecipe)) {
					tool = new ItemMap();
				}
				else {
					tool = ((DeterministicEnchantingRecipe) parentRecipe).getTool().clone();
				}
			}
			else {
				tool = parseItemMap(toolSection);
			}
			if (tool.getTotalItemAmount() == 0) {
				plugin.warning("Deterministic enchanting recipe " + name + " had no tool to enchant specified, it was skipped");
				result = null;
				break;
			}
			result = new DeterministicEnchantingRecipe(identifier, name, productionTime, input, tool, enchant, level);
			break;
		case "RANDOM":
			ConfigurationSection outputSect = config.getConfigurationSection("outputs");
			Map <ItemMap, Double> outputs = new HashMap<ItemMap, Double>();
			ItemMap displayThis = null;
			if (outputSect == null) {
				if (parentRecipe instanceof RandomOutputRecipe) {
					//clone it
					for(Entry <ItemMap, Double> entry :  ((RandomOutputRecipe) parentRecipe).getOutputs().entrySet()) {
						outputs.put(entry.getKey().clone(), entry.getValue());
					}
					displayThis = ((RandomOutputRecipe) parentRecipe).getDisplayMap();
				}
				else {
					plugin.severe("No outputs specified for random recipe " + name + " it was skipped");
					result = null;
					break;
				}
			}
			else {
				double totalChance = 0.0;
				String displayMap = outputSect.getString("display");
				for(String key : outputSect.getKeys(false)) {
					ConfigurationSection keySec = outputSect.getConfigurationSection(key);
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
				if (Math.abs(totalChance - 1.0) > 0.0001) {
					plugin.warning("Sum of output chances for recipe " + name + " is not 1.0. Total sum is: " + totalChance);
				}
			}
			result = new RandomOutputRecipe(identifier, name, productionTime, input, outputs, displayThis);
			break;
		case "COSTRETURN":
			double factor = config.getDouble("factor",
					(parentRecipe instanceof FactoryMaterialReturnRecipe) ? ((FactoryMaterialReturnRecipe)parentRecipe).getFactor() : 1.0);
			result = new FactoryMaterialReturnRecipe(identifier, name, productionTime, input, factor);
			break;
		case "LOREENCHANT":
			ConfigurationSection toolSec = config.getConfigurationSection("loredItem");
			ItemMap toolMap;
			if (toolSec == null) {
				if (!(parentRecipe instanceof LoreEnchantRecipe)) {
					toolMap = new ItemMap();
				}
				else {
					toolMap = ((LoreEnchantRecipe) parentRecipe).getTool().clone();
				}
			}
			else {
				toolMap = parseItemMap(toolSec);
			}
			if (toolMap.getTotalItemAmount() == 0) {
				plugin.warning("Lore enchanting recipe " + name + " had no tool to enchant specified, it was skipped");
				result = null;
				break;
			}
			List <String> appliedLore = config.getStringList("appliedLore");
			if (appliedLore == null || appliedLore.size() == 0) {
				if (parentRecipe instanceof LoreEnchantRecipe) {
					appliedLore = ((LoreEnchantRecipe) parentRecipe).getAppliedLore();
				}
				else {
					plugin.warning("No lore to apply found for lore enchanting recipe " + name + ". It was skipped");
					result = null;
					break;
				}
			}
			List <String> overwrittenLore = config.getStringList("overwrittenLore");
			if (overwrittenLore == null || overwrittenLore.size() == 0) {
				if (parentRecipe instanceof LoreEnchantRecipe) {
					overwrittenLore = ((LoreEnchantRecipe) parentRecipe).getOverwrittenLore();
				}
				else {
					//having no lore to be overwritten is completly fine
					overwrittenLore = new LinkedList<String>();
				}
			}
			result = new LoreEnchantRecipe(identifier, name, productionTime, input, toolMap, appliedLore, overwrittenLore);
			break;
		case "RECIPEMODIFIERUPGRADE":
			int rank = config.getInt("rank");
			String toUpgrade = config.getString("recipeUpgraded");
			if (toUpgrade == null) {
				plugin.warning("No recipe to upgrade specified at " + config.getCurrentPath());
				return null;
			}
			String followUpRecipe = config.getString("followUpRecipe");
			result = new RecipeScalingUpgradeRecipe(identifier, name, productionTime, input, null, rank, null);
			String [] data = {toUpgrade, followUpRecipe};
			recipeScalingUpgradeMapping.put((RecipeScalingUpgradeRecipe) result, data);
			break;
		case "DUMMY":
			result = new DummyParsingRecipe(identifier, name, productionTime, null);
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
					if (rec instanceof DummyParsingRecipe) {
						plugin.warning("You can't add dummy recipes to factories! Maybe you are the dummy here?");
						continue;
					}
					if (rec != null) {
						recipeList.add(rec);
						usedRecipes.add(rec);
					}
					else {
						plugin.warning("Could not find specified recipe " + recipeName 
								+ " for factory " + entry.getKey().getName());
					}
				}
				((FurnCraftChestEgg) entry.getKey()).setRecipes(recipeList);
			}
		}
		for(IRecipe reci : recipes.values()) {
			if (!usedRecipes.contains(reci)) {
				plugin.warning("The recipe " + reci.getName() + " is specified in the config, but not used in any factory");
			}
		}
	}
	
	private ProductionRecipeModifier parseProductionRecipeModifier(ConfigurationSection config) {
		ProductionRecipeModifier modi = new ProductionRecipeModifier();
		if (config == null) {
			return null;
		}
		for(String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				plugin.warning("Found invalid config value at " + config.getCurrentPath() + " " + key + ". Only identifiers for recipe modifiers allowed at this level");
				continue;
			}
			int minimumRunAmount = current.getInt("minimumRunAmount");
			int maximumRunAmount = current.getInt("maximumRunAmount");
			double minimumMultiplier = current.getDouble("baseMultiplier");
			double maximumMultiplier = current.getDouble("maximumMultiplier");
			int rank = current.getInt("rank");
			modi.addConfig(minimumRunAmount, maximumRunAmount, minimumMultiplier, maximumMultiplier, rank);
		}
		return modi;
	}
	
	private void assignRecipeScalingRecipes() {
		for(Entry <RecipeScalingUpgradeRecipe, String []> entry : recipeScalingUpgradeMapping.entrySet()) {
			IRecipe prod = recipes.get(entry.getValue() [0]);
			if (prod == null) {
				plugin.warning("The recipe " + entry.getValue() [0] + ", which the recipe " + entry.getKey().getName() + " is supposed to upgrade doesnt exist");
				continue;
			}
			if (!(prod instanceof ProductionRecipe)) {
				plugin.warning("The recipe "  + entry.getKey().getName() + " has a non production recipe specified as recipe to upgrade, this doesnt work");
				continue;
			}
			entry.getKey().setUpgradedRecipe((ProductionRecipe) prod);
			String followUp = entry.getValue() [1];
			if (followUp != null) {
				IRecipe followRecipe = recipes.get(followUp);
				if (followRecipe == null) {
					plugin.warning("The recipe " + entry.getValue() [0] + ", which the recipe " + entry.getKey().getName() + " is supposed to use as follow up recipe doesnt exist");
					continue;
				}
				if (!(followRecipe instanceof RecipeScalingUpgradeRecipe)) {
					plugin.warning("The recipe "  + entry.getKey().getName() + " has a non recipe scaling upgrade recipe specified as recipe to follow up with, this doesnt work");
					continue;
				}
				entry.getKey().setFollowUpRecipe((RecipeScalingUpgradeRecipe) followRecipe);
			}
		}
	}
	
	public String getDefaultMenuFactory() {
		return defaultMenuFactory;
	}
}
