package com.github.devotedmc.hiddenore;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *  WOOD_PICKAXE:
  template:
   ==: org.bukkit.inventory.ItemStack
   type: WOOD_PICKAXE
   amount: 1
  ignore:
   amount: true
   durability: true
   enchants: true
   lore: true
   name: true
 * @author ProgrammerDan
 *
 */
public class ToolConfig {
	private ItemStack template;
	private boolean ignoreAmount;
	private boolean ignoreDurability;
	private boolean ignoreEnchants;
	private boolean ignoreEnchantsLvl;
	private boolean ignoreLore;
	private boolean ignoreName;
	private boolean ignoreMeta;

	/* TODO */
	private double rateModifier;
	private double dropInflation;
	private double dropInflationChance;
	
	private static Map<String, ToolConfig> tools;
	
	protected ToolConfig(ItemStack template, boolean ignoreAmount, boolean ignoreDurability,
			boolean ignoreEnchants, boolean ignoreEnchantsLvl, boolean ignoreLore, boolean ignoreName) {
		this.template = template;
		this.ignoreAmount = ignoreAmount;
		this.ignoreDurability = ignoreDurability;
		this.ignoreEnchants = ignoreEnchants;
		this.ignoreEnchantsLvl = ignoreEnchantsLvl;
		this.ignoreLore = ignoreLore;
		this.ignoreName = ignoreName;
		this.ignoreMeta = ignoreEnchants && ignoreEnchantsLvl && ignoreLore && ignoreName;
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
	
	public boolean ignoreLore() {
		return ignoreLore;
	}
	
	public boolean ignoreName() {
		return ignoreName;
	}
	
	public boolean ignoreMeta() {
		return ignoreMeta;
	}
	
	public static void clear() {
		tools = new HashMap<String, ToolConfig>();
	}
	
	public static void initTool(ConfigurationSection tool) {
		if (tools == null) clear();
		if (!tool.contains("template")) return;
		
		if (tool.contains(tool.getName())){
			HiddenOre.getPlugin().getLogger().info("Duplicate definition for tool: " + tool.getName());
		}
		
		tools.put(tool.getName(),
				new ToolConfig((ItemStack) tool.get("template"),
						tool.getBoolean("ignore.amount", true),
						tool.getBoolean("ignore.durability", true),
						tool.getBoolean("ignore.enchants", true),
						tool.getBoolean("ignore.enchantsLvl", true),
						tool.getBoolean("ignore.lore", true),
						tool.getBoolean("ignore.name", true)
					)
				);
	}

	public static ToolConfig getConfig(String t) {
		return tools.get(t);
	}

	public static boolean dropsWithTool(Set<String> t, ItemStack tool) {
		return getTool(t, tool) != null;
	}

	public static ToolConfig getAnyTool(ItemStack tool) {
		return getTool(tools.keySet(), tool);
	}

	public static ToolConfig getTool(Set<String> t, ItemStack tool) {
		for (String test : t) {
			ToolConfig comp = tools.get(test);
			if (comp != null) {
				ItemStack compare = comp.getTemplate();
				if (compare == null) continue;
				if (compare.getType() != tool.getType()) continue;
				if (!comp.ignoreDurability() && 
						compare.getDurability() != tool.getDurability()) continue;
				if (!comp.ignoreAmount() && compare.getAmount() != tool.getAmount()) continue;

				// Short circuit of metachecks.
				if (comp.ignoreMeta()) return comp;

				// Metachecks.
				ItemMeta compmeta = compare.getItemMeta();
				ItemMeta toolmeta = tool.getItemMeta();
				if (toolmeta == null && toolmeta == compmeta) return true; // equal but no further compare
				if (compmeta == null) continue; // toolmeta != null but compmeta == null
				// both non-null.
				if (!comp.ignoreName() &&
						!toolmeta.getDisplayName().equals(compmeta.getDisplayName())) continue;
				if (!comp.ignoreLore() &&
						!(toolmeta.hasLore() ? toolmeta.getLore().equals(compMeta.getLore()) : !compmeta.hasLore())) continue;
				// Expensive enchantment checks.
				if (!comp.ignoreEnchants()) {
					Map<Enchantment, Integer> compench = compmeta.getEnchants();
					Map<Enchantment, Integer> toolench = toolmeta.getEnchants();
					if (!compench.keySet().equals(toolench.keySet())) continue; // check that set of enchants is same
					if (!comp.ignoreEnchantsLvl()) { // also check _level_ of enchants
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
		
		return null;
	}
}
