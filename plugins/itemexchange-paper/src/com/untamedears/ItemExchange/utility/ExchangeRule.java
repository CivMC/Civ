package com.untamedears.ItemExchange.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleCreateException;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import com.untamedears.ItemExchange.metadata.AdditionalMetadata;
import com.untamedears.ItemExchange.metadata.BookMetadata;
import com.untamedears.ItemExchange.metadata.EnchantmentStorageMetadata;
import com.untamedears.ItemExchange.metadata.PotionMetadata;
import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;

/*
 * Contains the rules pertaining to an item which can participate in the exchange
 */

/**
 * 
 * @author Brian Landry
 */
public class ExchangeRule {
	private static final List<Material> NOT_SUPPORTED = Arrays.asList(Material.MAP, Material.WRITTEN_BOOK, Material.ENCHANTED_BOOK, Material.FIREWORK, Material.FIREWORK_CHARGE, Material.POTION);
	
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
	private List<Enchantment> excludedEnchantments;
	private boolean unlistedEnchantmentsAllowed;
	private String displayName;
	private String[] lore;
	private RuleType ruleType;
	private AdditionalMetadata additional = null;
	private Faction citadelGroup = null;

	/*
	 * Describes whether the Exchange Rule functions as an input or an output
	 */
	public static enum RuleType {
		INPUT, OUTPUT
	}

	public ExchangeRule(Material material, int amount, short durability, RuleType ruleType) {
		this(material, amount, durability, new HashMap<Enchantment, Integer>(), new ArrayList<Enchantment>(), false, "", new String[0], ruleType);
	}

	public ExchangeRule(Material material, int amount, short durability, Map<Enchantment, Integer> requiredEnchantments, List<Enchantment> excludedEnchantments, boolean otherEnchantmentsAllowed, String displayName, String[] lore, RuleType ruleType) {
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
	public static ExchangeRule parseItemStack(ItemStack itemStack, RuleType ruleType) throws ExchangeRuleCreateException {
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
			else if(itemMeta instanceof PotionMeta) {
				additional = new PotionMetadata((PotionMeta) itemMeta);
			}
			//I've removed the PotionMeta block since it is not required if only vanilla potions are used, PotionMeta support should be added in the future
			if(itemMeta instanceof FireworkEffectMeta || itemMeta instanceof FireworkMeta || itemMeta instanceof LeatherArmorMeta || itemMeta instanceof MapMeta || itemMeta instanceof SkullMeta) {
				throw new ExchangeRuleCreateException("This item is not yet supported by ItemExchange.");
			}
		}

		ExchangeRule exchangeRule = new ExchangeRule(itemStack.getType(), itemStack.getAmount(), itemStack.getDurability(), requiredEnchantments, new ArrayList<Enchantment>(), false, displayName, lore, ruleType);

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
			throw new ExchangeRuleParseException("Invalid exchange rule");
		}
	}

	public static ExchangeRule parseRuleString(String ruleString) throws ExchangeRuleParseException {
		try {
			// [Type,Material
			// ID,Durability,Amount,RequiredEnchantments[],ExcludedEnchantments[],UnlistedEnchantments[],DisplayName,Lore]
			String[] compiledRule = ruleString.split(hiddenCategorySpacer);
			// Check length is correct
			if (compiledRule.length < 12) {
				throw new ExchangeRuleParseException("Compiled rule too short: " + String.valueOf(compiledRule.length));
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
				throw new ExchangeRuleParseException("Invalid rule type");
			}

			String transactionType = showString(compiledRule[1]);

			if(!transactionType.equals("item")) {
				throw new ExchangeRuleParseException("Invalid transaction type");
			}

			// Get Material
			Material material = Material.getMaterial(Integer.valueOf(showString(compiledRule[2])));
			// Get Durability
			short durability = Short.valueOf(showString(compiledRule[3]));
			// Get Amount
			int amount = Integer.parseInt(showString(compiledRule[4]));
			// Get Required Enchantments
			Map<Enchantment, Integer> requiredEnchantments = new HashMap<Enchantment, Integer>();
			for (String compiledEnchant : compiledRule[5].split(hiddenSecondarySpacer)) {
				if (compiledEnchant.equals("")) {
					continue;
				}
				Enchantment enchantment = Enchantment.getById(Integer.valueOf(showString(compiledEnchant.split(hiddenTertiarySpacer)[0])));
				Integer level = Integer.valueOf(showString(compiledEnchant.split(hiddenTertiarySpacer)[1]));
				requiredEnchantments.put(enchantment, level);
			}

			// Get Excluded Enchantments
			List<Enchantment> excludedEnchantments = new ArrayList<Enchantment>();
			for (String compiledEnchant : compiledRule[6].split(hiddenSecondarySpacer)) {
				if (compiledEnchant.equals("")) {
					continue;
				}
				Enchantment enchantment = Enchantment.getById(Integer.valueOf(showString(compiledEnchant)));
				excludedEnchantments.add(enchantment);
			}
			// Get if unlisted enchantments are allowed
			boolean unlistedEnchantmentsAllowed;
			if (showString(compiledRule[7]).equals("0")) {
				unlistedEnchantmentsAllowed = false;
			}
			else if (showString(compiledRule[7]).equals("1")) {
				unlistedEnchantmentsAllowed = true;
			}
			else {
				throw new ExchangeRuleParseException("Invalid Rule Type");
			}
			// Get DisplayName
			String displayName = "";
			if (!compiledRule[8].equals("")) {
				displayName = unescapeString(showString(compiledRule[8]));
			}
			// Get Lore
			String[] lore = new String[0];
			if (!compiledRule[9].equals("")) {
				lore = showString(compiledRule[9]).split(secondarySpacer);
				
				for(int i = 0; i < lore.length; i++) {
					lore[i] = unescapeString(lore[i]);
				}
			}

			AdditionalMetadata additional = null;

			if(material == Material.WRITTEN_BOOK) {
				additional = BookMetadata.deserialize(showString(compiledRule[10]));
			}
			else if(material == Material.ENCHANTED_BOOK) {
				additional = EnchantmentStorageMetadata.deserialize(showString(compiledRule[10]));
			}
			else if(material == Material.POTION) {
				if(!compiledRule[10].isEmpty()) {
					additional = PotionMetadata.deserialize(showString(compiledRule[10]));
				}
			}

			Faction group;

			if(!compiledRule[11].equals("")) {
				group = Citadel.getGroupManager().getGroup(unescapeString(showString(compiledRule[11])));
			}
			else {
				group = null;
			}

			ExchangeRule exchangeRule = new ExchangeRule(material, amount, durability, requiredEnchantments, excludedEnchantments, unlistedEnchantmentsAllowed, displayName, lore, ruleType);

			exchangeRule.setAdditionalMetadata(additional);
			exchangeRule.setCitadelGroup(group);

			return exchangeRule;
		}
		catch (Exception e) {
			throw new ExchangeRuleParseException("Invalid Exchange Rule");
		}
	}

	/*
	 * Removes every other character from a string
	 */
	private static String showString(String string) {
		StringBuilder result = new StringBuilder();
		
		char[] chars = string.toCharArray();
		
		for(int i = 1; i < chars.length; i += 2) {
			result.append(chars[i]);
		}
		
		return result.toString();
	}

	/*
	 * Adds a § in front of every character in a string
	 */
	private static String hideString(String string) {
		String hiddenString = "";
		for (char character : string.toCharArray()) {
			hiddenString += "§" + character;
		}
		return hiddenString;
	}
	
	/*
	 * Escapes all 'r' and '\' characters in a string
	 */
	private static String escapeString(String string) {
		return string.replaceAll("([\\\\r])", "\\\\$1");
	}
	
	/*
	 * Un-escapes all 'r' and '\' characters in a string
	 */
	private static String unescapeString(String string) {
		return string.replaceAll("\\\\([\\\\r])", "$1");
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
				ruleType = ExchangeRule.RuleType.OUTPUT;
			}
			if (ruleType != null) {
				Material material = null;
				short durability = 0;
				int amount = 1;
				if (args.length >= 2) {
					if (ItemExchangePlugin.NAME_MATERIAL.containsKey(args[1].toLowerCase())) {
						ItemStack itemStack = ItemExchangePlugin.NAME_MATERIAL.get(args[1].toLowerCase());
						material = itemStack.getType();
						durability = itemStack.getDurability();
					}
					else {
						String[] split = args[1].split(":");
						material = Material.getMaterial(Integer.valueOf(split[0]));
						if (split.length > 1) {
							durability = Short.valueOf(split[1]);
						}
					}
					if (args.length == 3) {
						amount = Integer.valueOf(args[2]);
					}
				}
				
				if(NOT_SUPPORTED.contains(material)) {
					throw new ExchangeRuleParseException("This material is not supported.");
				}
				
				return new ExchangeRule(material, amount, durability, ruleType);
			}
			else {
				throw new ExchangeRuleParseException("Please specify an input or output.");
			}
		}
		catch (Exception e) {
			throw new ExchangeRuleParseException("Invalid exchange rule.");
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
		if(ItemExchangePlugin.ENCHANTABLE_ITEMS.contains(material)) {
			newLore.add(displayedEnchantments());
		}
		
		for (String line : displayedLore()) {
			newLore.add(line);
		}
		
		if(citadelGroup != null) {
			newLore.add(ChatColor.RED + "Restricted with Citadel.");
		}
		
		if(newLore.size() > 0) {
			newLore.set(0, compileRule() + newLore.get(0));
		}
		else {
			newLore.add(compileRule());
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
		// Transaction type
		compiledRule += hiddenCategorySpacer + hideString("item");
		// Material ID
		compiledRule += hiddenCategorySpacer + hideString(String.valueOf(material.getId()));
		// Durability
		compiledRule += hiddenCategorySpacer + hideString(String.valueOf(durability));
		// Amount
		compiledRule += hiddenCategorySpacer + hideString(String.valueOf(amount));
		compiledRule += hiddenCategorySpacer;
		boolean enchantable = ItemExchangePlugin.ENCHANTABLE_ITEMS.contains(material);
		if(enchantable) {
			for (Entry<Enchantment, Integer> entry : requiredEnchantments.entrySet()) {
				compiledRule += hideString(String.valueOf(entry.getKey().getId())) + hiddenTertiarySpacer + hideString(entry.getValue().toString()) + hiddenSecondarySpacer;
			}
		}
		compiledRule += hiddenCategorySpacer;
		if(enchantable) {
			for (Enchantment enchantment : excludedEnchantments) {
				compiledRule += hideString(String.valueOf(enchantment.getId())) + hiddenSecondarySpacer;
			}
		}
		compiledRule += hiddenCategorySpacer + ((unlistedEnchantmentsAllowed && enchantable) ? hideString("1") : hideString("0"));
		compiledRule += hiddenCategorySpacer + hideString(escapeString(displayName));
		compiledRule += hiddenCategorySpacer;
		for (int i = 0; i < lore.length; i++) {
			String line = lore[i];
			
			if(i > 0)
				compiledRule += hiddenSecondarySpacer;
			
			compiledRule += hideString(escapeString(line));
		}
		compiledRule += hiddenCategorySpacer;
		if(additional != null) {
			compiledRule += hideString(additional.serialize());
		}
		compiledRule += hiddenCategorySpacer;
		if(citadelGroup != null) {
			compiledRule += hideString(escapeString(citadelGroup.getName()));
		}
		compiledRule += hiddenCategorySpacer + "§r";
		return compiledRule;
	}

	public boolean followsRules(Player player) {
		if(this.ruleType == RuleType.INPUT) {
			if(citadelGroup != null) {
				String playerName = player.getName();

				if(citadelGroup.isMember(playerName) || citadelGroup.isModerator(playerName) || citadelGroup.isFounder(playerName)) {
					return true;
				}
				else {
					return false;
				}
			}
		}

		return true;
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
	 * Counts how many multiples of the specified item an inventory has.
	 */
	public int checkMultiples(Inventory inventory) {
		int invAmount = 0;
		for (ItemStack itemStack : inventory.getContents()) {
			if (itemStack != null && followsRules(itemStack)) {
				invAmount += itemStack.getAmount();
			}
		}
		return invAmount / amount;
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
			for (Enchantment excludedEnchantment : excludedEnchantments) {
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

	public String[] display(Player p) {
		List<String> displayed = new ArrayList<String>();
		// Material type, durability and amount
		displayed.add(displayedItemStackInfo());
		// Additional metadata (books, etc.)
		if(additional != null) {
			displayed.add(additional.getDisplayedInfo());
		}

		// Enchantments
		if(ItemExchangePlugin.ENCHANTABLE_ITEMS.contains(material)) {
			displayed.add(displayedEnchantments());
		}

		// Lore
		for(String line : displayedLore()) {
			displayed.add(line);
		}

		// Citadel group
		if(citadelGroup != null) {
			String playerName = p.getName();
			
			if(citadelGroup.isFounder(playerName) || citadelGroup.isModerator(playerName) || citadelGroup.isMember(playerName)) {
				displayed.add(ChatColor.GREEN + "Restricted with Citadel. You have access to this shop.");
			}
			else {
				displayed.add(ChatColor.RED + "Restricted with Citadel. You do not have access to this shop.");
			}
		}

		return displayed.toArray(new String[displayed.size()]);
	}

	private String displayedItemStackInfo() {
		StringBuilder stringBuilder = new StringBuilder().append(ChatColor.YELLOW).append((ruleType == RuleType.INPUT ? "Input" : "Output") + ": " + ChatColor.WHITE).append(amount);
		if (ItemExchangePlugin.MATERIAL_NAME.containsKey(new ItemStack(material, 1, durability))) {
			stringBuilder.append(" " + ItemExchangePlugin.MATERIAL_NAME.get(new ItemStack(material, 1, durability)));
		}
		else {
			stringBuilder.append(material.name() + ":").append(durability);
		}
		stringBuilder.append(displayName.equals("") ? "" : " \"" + displayName + "\"");
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
			for (Enchantment enchantment : excludedEnchantments) {
				stringBuilder.append(ChatColor.RED);
				stringBuilder.append(ItemExchangePlugin.ENCHANTMENT_ABBRV.get(enchantment.getName()));
				stringBuilder.append(" ");
			}
			stringBuilder.append(unlistedEnchantmentsAllowed ? ChatColor.GREEN + "Other Enchantments Allowed." : ChatColor.RED + "Other Enchantments Disallowed");
			return stringBuilder.toString();
		}
		else {
			return unlistedEnchantmentsAllowed ? ChatColor.GREEN + "Any enchantments allowed" : ChatColor.RED + "No enchantments allowed";
		}
	}

	private String[] displayedLore() {
		if (lore.length == 0) {
			return new String[0];
		}
		else if (lore.length == 1) {
			return new String[] { ChatColor.DARK_PURPLE + lore[0] };
		}
		else {
			return new String[] { ChatColor.DARK_PURPLE + lore[0], ChatColor.DARK_PURPLE + lore[1] + "..." };
		}
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public void setUnlistedEnchantmentsAllowed(boolean allowed) {
		this.unlistedEnchantmentsAllowed = allowed;
	}

	public boolean getUnlistedEnchantmentsAllowed() {
		return unlistedEnchantmentsAllowed;
	}

	public void requireEnchantment(Enchantment enchantment, Integer level) {
		requiredEnchantments.put(enchantment, level);
	}

	public void removeRequiredEnchantment(Enchantment enchantment) {
		requiredEnchantments.remove(enchantment);
	}

	public void excludeEnchantment(Enchantment enchantment) {
		if(!excludedEnchantments.contains(enchantment))
			excludedEnchantments.add(enchantment);
	}

	public void removeExcludedEnchantment(Enchantment enchantment) {
		excludedEnchantments.remove(enchantment);
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

	public void setCitadelGroup(Faction group) {
		this.citadelGroup = group;
	}

	public Faction getCitadelGroup() {
		return citadelGroup;
	}

	public RuleType getType() {
		return ruleType;
	}

	public static boolean isRuleBlock(ItemStack item) {
		try {
			ExchangeRule.parseBulkRuleBlock(item);

			return true;
		}
		catch(ExchangeRuleParseException e) {
			try {
				ExchangeRule.parseRuleBlock(item);

				return true;
			}
			catch(ExchangeRuleParseException e2) {
				return false;
			}
		}
	}
}
