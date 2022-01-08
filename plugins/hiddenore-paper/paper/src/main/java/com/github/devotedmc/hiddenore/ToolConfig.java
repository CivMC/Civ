package com.github.devotedmc.hiddenore;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 wood_pickaxe:
  template:
   ==: org.bukkit.inventory.ItemStack
   type: WOOD_PICKAXE
   amount: 1
  ignore:
   amount: true
   durability: true
   enchants: true
   otherEnchants: true
   enchantsLvl: true
   lore: true
   name: true
  modifiers:
   dropChance: 1.0
   minAmount: 0
   maxAmount: 0
 *
 * special case for anything: (catchall)
 *  "ignore" section is ignored but "modifiers" are observed
 *  value after "all" is ignored, just something needs to be there.
 *
 name:
  ignore:
   all: true
  modifiers:
   dropChance: 1.0
   minAmount: 0
   maxAmount: 0
 *
 * @author ProgrammerDan
 *
 */
public class ToolConfig {
	private ItemStack template;
	private boolean ignoreAmount;
	private boolean ignoreDurability;
	private boolean ignoreEnchants;
	private boolean ignoreOtherEnchants;
	private boolean ignoreEnchantsLvl;
	private boolean ignoreLore;
	private boolean ignoreName;
	private boolean ignoreMeta;

	private double minAmountModifier;
	private double maxAmountModifier;
	private double dropChanceModifier;
	
	private static Map<String, ToolConfig> tools = new HashMap<>();
	private static List<String> toolList = new LinkedList<>();
	
	protected ToolConfig(ItemStack template, boolean ignoreAmount, boolean ignoreDurability,
			boolean ignoreEnchants, boolean ignoreOtherEnchants, boolean ignoreEnchantsLvl, 
			boolean ignoreLore, boolean ignoreName,
			Double dropChanceModifier, Double minAmountModifier, Double maxAmountModifier) {
		this.template = template;
		this.ignoreAmount = ignoreAmount;
		this.ignoreDurability = ignoreDurability;
		this.ignoreEnchants = ignoreEnchants;
		this.ignoreOtherEnchants = ignoreOtherEnchants;
		this.ignoreEnchantsLvl = ignoreEnchantsLvl;
		this.ignoreLore = ignoreLore;
		this.ignoreName = ignoreName;
		this.ignoreMeta = ignoreEnchants && ignoreLore && ignoreName;
		this.dropChanceModifier = (dropChanceModifier == null ? 1.0 : dropChanceModifier);
		this.minAmountModifier = (minAmountModifier == null ? 0.0 : minAmountModifier);
		this.maxAmountModifier = (maxAmountModifier == null ? 0.0 : maxAmountModifier);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(template);
		sb.append(",ignore:");
		if (this.ignoreAmount) sb.append("amount");
		if (this.ignoreMeta) {
			sb.append("meta");
		} else {
			if (this.ignoreEnchants) sb.append("enchants");
			if (this.ignoreOtherEnchants) sb.append("otherench");
			if (this.ignoreEnchantsLvl) sb.append("enchantslvl");
			if (this.ignoreLore) sb.append("lore");
			if (this.ignoreName) sb.append("name");
		}
		
		sb.append(",mods:").append("c:").append(this.dropChanceModifier);
		sb.append("n:").append(this.minAmountModifier);
		sb.append("m:").append(this.maxAmountModifier);
		
		return sb.toString();
	}
	
	public ItemStack getTemplate() {
		return template;
	}
	
	public boolean ignoreAmount() {
		return ignoreAmount;
	}
	
	public boolean ignoreDurability() {
		return ignoreDurability;
	}
	
	public boolean ignoreEnchants() {
		return ignoreEnchants;
	}

	public boolean ignoreOtherEnchants() {
		return ignoreOtherEnchants;
	}
	
	public boolean ignoreEnchantsLvl() {
		return ignoreEnchantsLvl;
	}
	
	public boolean ignoreLore() {
		return ignoreLore;
	}
	
	public boolean ignoreName() {
		return ignoreName;
	}
	
	public boolean ignoreMeta() {
		return ignoreMeta;
	}
	
	public double getDropChanceModifier() {
		return dropChanceModifier;
	}
	
	public double getMinAmountModifier() {
		return minAmountModifier;
	}
	
	public double getMaxAmountModifier() {
		return maxAmountModifier;
	}
	
	public static void clear() {
		tools = new HashMap<>();
		toolList = new LinkedList<>();
	}
	
	public static void initTool(ConfigurationSection tool) {
		if (tools == null) clear();
		if (!tool.contains("template")) {
			if (!tool.contains("ignore.all")) {
				return;
			} else {
				HiddenOre.getPlugin().getLogger().info("Catchall tool found: " + tool.getName());
			}
		}
		
		if (tools.containsKey(tool.getName())){
			HiddenOre.getPlugin().getLogger().info("Duplicate definition for tool: " + tool.getName());
		} else {
			toolList.add(tool.getName());
		}

		ItemStack temp = (tool.contains("ignore.all") ? null : (ItemStack) tool.get("template"));
		
		tools.put(tool.getName(),
				new ToolConfig(temp,
						tool.getBoolean("ignore.amount", true),
						tool.getBoolean("ignore.durability", true),
						tool.getBoolean("ignore.enchants", true),
						tool.getBoolean("ignore.otherEnchants", true),
						tool.getBoolean("ignore.enchantsLvl", true),
						tool.getBoolean("ignore.lore", true),
						tool.getBoolean("ignore.name", true),
						tool.getDouble("modifiers.dropChance", 1.0),
						tool.getDouble("modifiers.minAmount", 0.0),
						tool.getDouble("modifiers.maxAmount", 0.0)
					)
				);
		if (Config.isDebug) {
			HiddenOre.getPlugin().getLogger().log(Level.INFO, "Tool {0} defined as: {1}", 
					new Object[] {tool.getName(), tools.get(tool.getName())});
		}
	}

	public static ToolConfig getConfig(String t) {
		return tools.get(t);
	}

	public static boolean dropsWithTool(List<String> t, ItemStack tool) {
		ToolConfig ret = getTool(t, tool);
		/*DIAGNOSTICS* HiddenOre.getPlugin().getLogger().log(Level.INFO, "Out of {0} picked {1} for {2}", 
				new Object[] {t, ret, tool});*/
		return ret != null;
	}

	public static ToolConfig getAnyTool(ItemStack tool) {
		return getTool(toolList, tool);
	}

	public static ToolConfig getTool(List<String> t, ItemStack tool) {
		ToolConfig catchall = null;
		for (String test : t) {
			ToolConfig comp = tools.get(test);
			/*DIAGNOSTICS*HiddenOre.getPlugin().getLogger().log(Level.INFO, " -- Comparing {0} to {1}:{2}", new Object[] {
					tool, test, comp == null ? null : comp.toString()
			});*/
			if (comp != null) {
				ItemStack compare = comp.getTemplate();
				if (compare == null) {
					catchall = comp;
					continue;
				}
				if (compare.getType() != tool.getType()) continue;
				if (!comp.ignoreAmount() && compare.getAmount() != tool.getAmount()) continue;

				// Short circuit of metachecks.
				if (comp.ignoreMeta()) return comp;

				// Metachecks.
				ItemMeta compmeta = compare.getItemMeta();
				ItemMeta toolmeta = tool.getItemMeta();
				if (toolmeta == null && toolmeta == compmeta) return comp; // equal but no further compare
				
				if (compmeta == null) continue; // toolmeta != null but compmeta == null
				// both non-null.
				if (!comp.ignoreDurability && (!(comp instanceof Damageable)  || !(toolmeta instanceof Damageable) ||
						((Damageable) comp).getDamage() != ((Damageable) toolmeta).getDamage())) {
					continue;
				}
				if (!comp.ignoreName() && !(toolmeta.hasDisplayName() ? 
						toolmeta.getDisplayName().equals(compmeta.getDisplayName()) : !compmeta.hasDisplayName() ) ) continue;
				if (!comp.ignoreLore() &&
						!(toolmeta.hasLore() ? toolmeta.getLore().equals(compmeta.getLore()) : !compmeta.hasLore())) continue;
				// Expensive enchantment checks.
				if (!comp.ignoreEnchants()) {
					Map<Enchantment, Integer> compench = compmeta.getEnchants();
					Map<Enchantment, Integer> toolench = toolmeta.getEnchants();

					// check that set of enchants is same (both null or both not null and same) else bail
					if (!comp.ignoreOtherEnchants() && !((compench == null && toolench == null) || 
							(compench != null && toolench != null && compench.keySet().equals(toolench.keySet()) ) ) ) continue; 

					// check that tool has at least the enchantments specified; ignore the rest.
					if (comp.ignoreOtherEnchants() && !(compench == null || 
							(toolench != null && toolench.keySet().containsAll(compench.keySet()) ) ) ) continue; 

					// also check _level_ of enchants
					if (!comp.ignoreEnchantsLvl() && compench != null) { 
						boolean fail = false;
						for(Enchantment ech : compench.keySet()) {
							if (!compench.get(ech).equals(toolench.get(ech))) {
								fail = true;
								break;
							}
						}
						if (fail) continue;
					}
				}
				return comp;
			}
		}
		/*DIAGNOSTICS*HiddenOre.getPlugin().getLogger().log(Level.INFO, " -- Using Catchall with {0} as {1}", new Object[] {
				tool, catchall != null ? catchall.toString() : null
		});*/
		return catchall;
	}
}
