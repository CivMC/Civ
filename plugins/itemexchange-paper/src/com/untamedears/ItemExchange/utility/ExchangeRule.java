package com.untamedears.ItemExchange.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import com.untamedears.ItemExchange.metadata.AdditionalMetadata;
import com.untamedears.ItemExchange.metadata.BookMetadata;
import com.untamedears.ItemExchange.metadata.EnchantmentStorageMetadata;

/*
 * Contains the rules pertaining to an item which can particpate in the exchange
 */

/**
 * 
 * @author Brian Landry
 */
public class ExchangeRule {
	public static final String hiddenRuleSpacer = "§&§&§&§&§r";
	public static final String hiddenCategorySpacer = "§&§&§&§r";
	public static final String hiddenSecondarySpacer = "§&§&§r";
	public static final String hiddenTertiarySpacer = "§&§r";
	
	public static final String ruleSpacer = "&&&&r";
	public static final String categorySpacer = "&&&r";
	public static final String secondarySpacer = "&&r";
	public static final String tertiarySpacer = "&r";
	
	
	private Material material;
	private int amount;
	private short durability;
	private Map<Enchantment, Integer> requiredEnchantments;
	private Map<Enchantment, Integer> excludedEnchantments;
	private boolean unlistedEnchantmentsAllowed;
	private String displayName;
	private String[] lore;
	private RuleType ruleType;
	private AdditionalMetadata additional = null;

	/*
	 * Describes whether the Exchange Rule functions as an input or an output
	 */
	public static enum RuleType {
		INPUT, OUTPUT
	}

	public ExchangeRule(Material material, int amount, short durability, RuleType ruleType) {
		this(material, amount, durability, new HashMap<Enchantment, Integer>(), new HashMap<Enchantment, Integer>(), false, "", new String[0], ruleType);
	}

	public ExchangeRule(Material material, int amount, short durability, Map<Enchantment, Integer> requiredEnchantments, Map<Enchantment, Integer> excludedEnchantments, boolean otherEnchantmentsAllowed, String displayName, String[] lore, RuleType ruleType) {
		this.material = material;
		this.amount = amount;
		this.durability = durability;
		this.requiredEnchantments = requiredEnchantments;
		this.excludedEnchantments = excludedEnchantments;
		this.unlistedEnchantmentsAllowed = otherEnchantmentsAllowed;
		this.displayName = displayName;
		this.lore = lore;
		this.ruleType = ruleType;
	}
	
	public void setAdditionalMetadata(AdditionalMetadata meta) {
		this.additional = meta;
	}

	/*
	 * Parses an ItemStack into an ExchangeRule which represents that ItemStack
	 */
	public static ExchangeRule parseItemStack(ItemStack itemStack, RuleType ruleType) {
		Map<Enchantment, Integer> requiredEnchantments = new HashMap<Enchantment, Integer>();
		for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
			requiredEnchantments.put(enchantment, itemStack.getEnchantments().get(enchantment));
		}
		String displayName = "";
		String[] lore = new String[0];
		AdditionalMetadata additional = null;
		if (itemStack.hasItemMeta()) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta.hasDisplayName()) {
				displayName = itemMeta.getDisplayName();
			}
			if (itemMeta.hasLore()) {
				lore = itemMeta.getLore().toArray(new String[itemMeta.getLore().size()]);
			}
			
			if(itemMeta instanceof BookMeta) {
				additional = new BookMetadata((BookMeta) itemMeta);
			}
			else if(itemMeta instanceof EnchantmentStorageMeta) {
				additional = new EnchantmentStorageMetadata((EnchantmentStorageMeta) itemMeta);
			}
		}
		
		ExchangeRule exchangeRule = new ExchangeRule(itemStack.getType(), itemStack.getAmount(), itemStack.getDurability(), requiredEnchantments, new HashMap<Enchantment, Integer>(), false, displayName, lore, ruleType);
		
		exchangeRule.setAdditionalMetadata(additional);
		
		return exchangeRule;
	}
	
	public static ExchangeRule[] parseBulkRuleBlock(ItemStack ruleBlock) throws ExchangeRuleParseException {
		try {
			String[] rules = ruleBlock.getItemMeta().getLore().get(1).split(hiddenRuleSpacer);

			List<ExchangeRule> ruleList = new ArrayList<ExchangeRule>();

			for(String rule : rules) {
				ruleList.add(parseRuleString(rule));
			}

			return ruleList.toArray(new ExchangeRule[0]);
		}
		catch(Exception e) {
			throw new ExchangeRuleParseException("Invalid Exchange Rule");
		}
	}

	/*
	 * Parses an RuleBlock into an ExchangeRule It uses the escape character to
	 * hide the information being stored from being visible to the character. It
	 * also includes an easily read but not parse version of the rule for the
	 * player. Might fail if the display name contains an &.
	 */
	public static ExchangeRule parseRuleBlock(ItemStack ruleBlock) throws ExchangeRuleParseException {
		try {
			return parseRuleString(ruleBlock.getItemMeta().getLore().get(0));
		}
		catch(Exception e) {
			throw new ExchangeRuleParseException("Invalid Exchange Rule");
		}
	}
	
	public static ExchangeRule parseRuleString(String ruleString) throws ExchangeRuleParseException {
		try {
			// [Type,Material
			// ID,Durability,Amount,RequiredEnchantments[],ExcludedEnchantments[],UnlistedEnchantments[],DisplayName,Lore]
			String[] compiledRule = ruleString.split(hiddenCategorySpacer);
			// Check length is correct
			if (compiledRule.length < 9) {
				throw new ExchangeRuleParseException("Compiled Rule too short: " + String.valueOf(compiledRule.length));
			}
			// Get Rule Type
			RuleType ruleType;
			if (showString(compiledRule[0]).equals("i")) {
				ruleType = RuleType.INPUT;
			}
			else if (showString(compiledRule[0]).equals("o")) {
				ruleType = RuleType.OUTPUT;
			}
			else {
				throw new ExchangeRuleParseException("Invalid Rule Type");
			}
			// Get Material
			Material material = Material.getMaterial(Integer.valueOf(showString(compiledRule[1])));
			// Get Durability
			short durability = Short.valueOf(showString(compiledRule[2]));
			// Get Amount
			int amount = Integer.parseInt(showString(compiledRule[3]));
			// Get Required Enchantments
			Map<Enchantment, Integer> requiredEnchantments = new HashMap<Enchantment, Integer>();
			for (String compiledEnchant : compiledRule[4].split(hiddenSecondarySpacer)) {
				if (compiledEnchant.equals("")) {
					continue;
				}
				Enchantment enchantment = Enchantment.getById(Integer.valueOf(showString(compiledEnchant.split(hiddenTertiarySpacer)[0])));
				Integer level = Integer.valueOf(showString(compiledEnchant.split(hiddenTertiarySpacer)[1]));
				requiredEnchantments.put(enchantment, level);
			}

			// Get Excluded Enchantments
			Map<Enchantment, Integer> excludedEnchantments = new HashMap<Enchantment, Integer>();
			for (String compiledEnchant : compiledRule[5].split(hiddenSecondarySpacer)) {
				if (compiledEnchant.equals("")) {
					continue;
				}
				Enchantment enchantment = Enchantment.getById(Integer.valueOf(showString(compiledEnchant.split(hiddenTertiarySpacer)[0])));
				Integer level = Integer.valueOf(showString(compiledEnchant.split(hiddenTertiarySpacer)[1]));
				excludedEnchantments.put(enchantment, level);
			}
			// Get if unlisted enchantments are allowed
			boolean unlistedEnchantmentsAllowed;
			if (showString(compiledRule[6]).equals("0")) {
				unlistedEnchantmentsAllowed = false;
			}
			else if (showString(compiledRule[6]).equals("1")) {
				unlistedEnchantmentsAllowed = true;
			}
			else {
				throw new ExchangeRuleParseException("Invalid Rule Type");
			}
			// Get DisplayName
			String displayName = "";
			if (!compiledRule[7].equals("")) {
				displayName = showString(compiledRule[7]);
			}
			// Get Lore
			String[] lore = new String[0];
			if (!compiledRule[8].equals("")) {
				lore = showString(compiledRule[8]).split(hiddenSecondarySpacer);
			}
			
			AdditionalMetadata additional = null;
			
			if(material == Material.WRITTEN_BOOK) {
				additional = BookMetadata.deserialize(showString(compiledRule[9]));
			}
			else if(material == Material.ENCHANTED_BOOK) {
				additional = EnchantmentStorageMetadata.deserialize(showString(compiledRule[9]));
			}
			
			ExchangeRule exchangeRule = new ExchangeRule(material, amount, durability, requiredEnchantments, excludedEnchantments, unlistedEnchantmentsAllowed, displayName, lore, ruleType);
			
			exchangeRule.setAdditionalMetadata(additional);
			
			return exchangeRule;
		}
		catch (Exception e) {
			throw new ExchangeRuleParseException("Invalid Exchange Rule");
		}
	}

	/*
	 * Removes § from string
	 */
	private static String showString(String string) {
		return StringUtils.join(string.split("§"));
	}

	/*
	 * Adds a § infront of every character in a string
	 */
	private static String hideString(String string) {
		String hiddenString = "";
		for (char character : string.toCharArray()) {
			hiddenString += "§" + character;
		}
		return hiddenString;
	}

	/*
	 * Parse create command into an exchange rule
	 */
	public static ExchangeRule parseCreateCommand(String[] args) throws ExchangeRuleParseException {
		try {
			// Parse ruletype
			RuleType ruleType = null;
			if (args[0].equalsIgnoreCase("input")) {
				ruleType = ExchangeRule.RuleType.INPUT;
			}
			else if (args[0].equalsIgnoreCase("output")) {
				ruleType = ExchangeRule.RuleType.INPUT;
			}
			if (ruleType != null) {
				Material material = null;
				short durability = 0;
				int amount = 1;
				if (args.length >= 2) {
					if (ItemExchangePlugin.NAME_MATERIAL.containsKey(args[2])) {
						ItemStack itemStack = ItemExchangePlugin.NAME_MATERIAL.get(args[2]);
						material = itemStack.getType();
						durability = itemStack.getDurability();
					}
					else {
						String[] split = args[2].split(":");
						material = Material.getMaterial(Integer.valueOf(split[0]));
						if (split.length > 1) {
							durability = Short.valueOf(split[1]);
						}
					}
					if (args.length >= 3) {
						amount = Integer.valueOf(args[3]);
					}
				}
				return new ExchangeRule(material, amount, durability, ruleType);
			}
			else {
				throw new ExchangeRuleParseException("Please specify and input or output.");
			}
		}
		catch (Exception e) {
			throw new ExchangeRuleParseException("Invalid Exchange Rule");
		}
	}
	
	public static ItemStack toBulkItemStack(Collection<ExchangeRule> rules) {
		ItemStack itemStack = ItemExchangePlugin.ITEM_RULE_ITEMSTACK.clone();
		
		String ruleSpacer = "§&§&§&§&§r";
		
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(ChatColor.DARK_RED + "Bulk Rule Block");
		List<String> newLore = new ArrayList<String>();
		
		StringBuilder compiledRules = new StringBuilder();
		
		Iterator<ExchangeRule> iterator = rules.iterator();
		
		while(iterator.hasNext()) {
			compiledRules.append(iterator.next().compileRule());
			
			if(iterator.hasNext())
				compiledRules.append(ruleSpacer);
		}
		
		newLore.add("This rule block holds " + rules.size() + (rules.size() > 1 ? " exchange rules." : " exchange rule."));
		newLore.add(compiledRules.toString());

		itemMeta.setLore(newLore);
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	/*
	 * Stores the exchange rule as an item stack
	 */
	public ItemStack toItemStack() {
		ItemStack itemStack = ItemExchangePlugin.ITEM_RULE_ITEMSTACK.clone();

		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(displayedItemStackInfo());
		List<String> newLore = new ArrayList<String>();
		newLore.add(compileRule() + displayedEnchantments());
		if (lore.length > 0) {
			newLore.add(displayedLore());
		}
		itemMeta.setLore(newLore);
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	/*
	 * Saves the exchange rule to lore in a semi-readable fashion
	 */
	public String compileRule() {
		String compiledRule = "";
		// RuleType
		compiledRule += ruleType.equals(RuleType.INPUT) ? hideString("i") : hideString("o");
		// Material ID
		compiledRule += hiddenCategorySpacer + hideString(String.valueOf(material.getId()));
		// Durability
		compiledRule += hiddenCategorySpacer + hideString(String.valueOf(durability));
		// Amount
		compiledRule += hiddenCategorySpacer + hideString(String.valueOf(amount));
		compiledRule += hiddenCategorySpacer;
		for (Entry<Enchantment, Integer> entry : requiredEnchantments.entrySet()) {
			compiledRule += hideString(String.valueOf(entry.getKey().getId())) + hiddenTertiarySpacer + hideString(entry.getValue().toString()) + hiddenSecondarySpacer;
		}
		compiledRule += hiddenCategorySpacer;
		for (Entry<Enchantment, Integer> entry : excludedEnchantments.entrySet()) {
			compiledRule += hideString(String.valueOf(entry.getKey().getId())) + hiddenTertiarySpacer + hideString(String.valueOf(entry)) + hiddenSecondarySpacer;
		}
		compiledRule += hiddenCategorySpacer + (unlistedEnchantmentsAllowed ? hideString("1") : hideString("0"));
		compiledRule += hiddenCategorySpacer + hideString(displayName);
		compiledRule += hiddenCategorySpacer;
		for (String line : lore) {
			compiledRule += hiddenSecondarySpacer + hideString(line);
		}
		compiledRule += hiddenCategorySpacer;
		if(additional != null) {
			compiledRule += hideString(additional.serialize());
		}
		compiledRule += hiddenCategorySpacer + "§r";
		return compiledRule;
	}

	/*
	 * Checks if an inventory has enough items which follow the ItemRules
	 */
	public boolean followsRules(Inventory inventory) {
		int invAmount = 0;
		for (ItemStack itemStack : inventory.getContents()) {
			if (itemStack != null && followsRules(itemStack)) {
				invAmount += itemStack.getAmount();
			}
		}
		return invAmount >= amount;
	}

	/*
	 * Checks if the given ItemStack follows the ItemRules except for the amount
	 */
	public boolean followsRules(ItemStack itemStack) {
		// check material type and druability
		boolean followsRules = material.getId() == itemStack.getTypeId() && durability == itemStack.getDurability();
		// Check enchantments
		if (itemStack.getEnchantments().size() > 0) {
			followsRules = followsRules && itemStack.getEnchantments().entrySet().containsAll(requiredEnchantments.entrySet());
			for (Entry<Enchantment, Integer> excludedEnchantment : excludedEnchantments.entrySet()) {
				followsRules = followsRules && !itemStack.getEnchantments().entrySet().contains(excludedEnchantment);
			}
		}
		else if (requiredEnchantments.size() > 0) {
			followsRules = false;
		}
		
		if(additional != null)
			followsRules = followsRules && additional.matches(itemStack);
		
		// Check displayName and Lore
		if (itemStack.hasItemMeta()) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (itemMeta.hasDisplayName()) {
				followsRules = followsRules && displayName.equals(itemMeta.getDisplayName());
			}
			else {
				followsRules = followsRules && displayName.equals("");
			}
			if (itemMeta.hasLore()) {
				for (int i = 0; i < itemMeta.getLore().size() && i < lore.length; i++) {
					followsRules = followsRules && lore[i].equals(itemMeta.getLore().get(i));
				}
				followsRules = followsRules && itemMeta.getLore().size() == lore.length;
			}
			else {
				followsRules = followsRules && lore.length == 0;
			}
		}
		else {
			followsRules = followsRules && displayName.equals("") && lore.length == 0;
		}
		return followsRules;
	}

	public String[] display() {
		List<String> displayed = new ArrayList<>();
		// Material type, durability and amount
		displayed.add(displayedItemStackInfo());
		// Additional metadata (books, etc.)
		if(additional != null) {
			displayed.add(additional.getDisplayedInfo());
		}
		// Enchantments
		displayed.add(displayedEnchantments());
		// Lore
		if (lore.length == 1) {
			displayed.add(ChatColor.DARK_PURPLE + lore[0]);
		}
		else if (lore.length > 1) {
			displayed.add(ChatColor.DARK_PURPLE + lore[0] + "...");
		}
		return displayed.toArray(new String[displayed.size()]);
	}
	
	private String displayedItemStackInfo() {
		StringBuilder stringBuilder = new StringBuilder().append(ChatColor.YELLOW).append((ruleType==RuleType.INPUT ? "Input" : "Output")+": "+ChatColor.WHITE).append(amount);
		if(ItemExchangePlugin.MATERIAL_NAME.containsKey(new ItemStack(material,durability))) {
			stringBuilder.append(" "+ItemExchangePlugin.MATERIAL_NAME.get(new ItemStack(material,durability)));
		}
		else {
			stringBuilder.append(material.name()+":").append(durability);
		}
		stringBuilder.append(displayName.equals("") ? "" : " \""+displayName+"\"");
		return stringBuilder.toString();
	}

	private String displayedEnchantments() {
		if (requiredEnchantments.size() > 0 || excludedEnchantments.size() > 0) {
			StringBuilder stringBuilder = new StringBuilder();
			for (Entry<Enchantment, Integer> entry : requiredEnchantments.entrySet()) {
				stringBuilder.append(ChatColor.GREEN);
				stringBuilder.append(ItemExchangePlugin.ENCHANTMENT_ABBRV.get(entry.getKey().getName()));
				stringBuilder.append(entry.getValue());
				stringBuilder.append(" ");
			}
			for (Entry<Enchantment, Integer> entry : excludedEnchantments.entrySet()) {
				stringBuilder.append(ChatColor.RED);
				stringBuilder.append(ItemExchangePlugin.ENCHANTMENT_ABBRV.get(entry.getKey().getName()));
				stringBuilder.append(entry.getValue());
				stringBuilder.append(" ");
			}
			stringBuilder.append(unlistedEnchantmentsAllowed ? ChatColor.GREEN + "Other Enchantments Allowed." : ChatColor.RED + "Other Enchantments Disallowed");
			return stringBuilder.toString();
		}
		else {
			return unlistedEnchantmentsAllowed ? ChatColor.GREEN + "Any enchantments allowed" : ChatColor.RED + "No enchantments allowed";
		}
	}

	private String displayedLore() {
		if (lore.length == 0) {
			return "";
		}
		else if (lore.length == 1) {
			return (ChatColor.DARK_PURPLE + lore[0]);
		}
		else {
			return ChatColor.DARK_PURPLE + lore[0] + "...";
		}
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public void requireEnchantment(Enchantment enchantment, Integer level) {
		requiredEnchantments.put(enchantment, level);
	}

	public void excludeEnchantment(Enchantment enchantment, Integer level) {
		excludedEnchantments.put(enchantment, level);
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setDurability(short durability) {
		this.durability = durability;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setLore(String[] lore) {
		this.lore = lore;
	}

	public void switchIO() {
		ruleType = ruleType == RuleType.INPUT ? RuleType.OUTPUT : RuleType.INPUT;
	}

	public int getAmount() {
		return amount;
	}

	public RuleType getType() {
		return ruleType;
	}
}
