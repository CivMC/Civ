package com.untamedears.ItemExchange.utility;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * Contains the rules pertaining to an item which can particpate in the exchange
 */

/**
 *
 * @author Brian Landry
 */
public class ExchangeRule {
	private Material material;
	private int amount;
	private short durability;
	private Map<Enchantment,Integer> requiredEnchantments;
	private Map<Enchantment,Integer> excludedEnchantments;
	private boolean unlistedEnchantmentsAllowed;
	private String displayName;
	private String[] lore;
	private RuleType ruleType;
	/*
	* Describes whether the Exchange Rule functions as an input or an output
	*/
	public static enum RuleType
	{
		INPUT,
		OUTPUT
	}
	public ExchangeRule(Material material, int amount, short durability,RuleType ruleType) {
		this(material,amount,durability,new HashMap<Enchantment,Integer>(), new HashMap<Enchantment,Integer>(), false, "", new String[0],ruleType);
	}
	public ExchangeRule(Material material, int amount, short durability,
		Map<Enchantment,Integer> requiredEnchantments, Map<Enchantment,Integer> excludedEnchantments,
		boolean otherEnchantmentsAllowed, String displayName, String[] lore,RuleType ruleType){
		this.material=material;
		this.amount=amount;
		this.durability=durability;
		this.requiredEnchantments=requiredEnchantments;
		this.excludedEnchantments=excludedEnchantments;
		this.unlistedEnchantmentsAllowed=otherEnchantmentsAllowed;
		this.displayName=displayName;
		this.lore=lore;
		this.ruleType=ruleType;
	}
	/*
	 * Parses an ItemStack into an ExchangeRule which represents that ItemStack
	 */
	public static ExchangeRule parseItemStack(ItemStack itemStack,RuleType ruleType)
	{
		Map<Enchantment,Integer> requiredEnchantments=new HashMap<Enchantment,Integer>();
		for(Enchantment enchantment:itemStack.getEnchantments().keySet())
		{
			requiredEnchantments.put(enchantment, itemStack.getEnchantments().get(enchantment));
		}
		ItemExchangePlugin.sendConsoleMessage(String.valueOf(requiredEnchantments.size()));
		String displayName="";
		String[] lore=new String[0];
		if(itemStack.hasItemMeta())
		{
			ItemMeta itemMeta=itemStack.getItemMeta();
			if(itemMeta.hasDisplayName())
			{
				displayName=itemMeta.getDisplayName();
			}
			if(itemMeta.hasLore())
			{
				lore=itemMeta.getLore().toArray(new String[itemMeta.getLore().size()]);
			}
		}
		return new ExchangeRule(itemStack.getType(),itemStack.getAmount(),itemStack.getDurability(),requiredEnchantments,new HashMap<Enchantment,Integer>(),false,displayName,lore,ruleType);
	}
	
	/*
	 * Parses an RuleBlock into an ExchangeRule
	 * It uses the escape character to hide the information being stored
	 * from being visible to the character. It also includes an easily read
	 * but not parse version of the rule for the player.
	 * Might fail if the display name contains an &.
	 */
	public static ExchangeRule parseRuleBlock(ItemStack ruleBlock) throws ExchangeRuleParseException{
		try{
			String catorgorySpacer="§&§&§&§r";
			String secondarySpacer="§&§&§r";
			String tertiarySpacer="§&§r";
			//[Type,Material ID,Durability,Amount,RequiredEnchantments[],ExcludedEnchantments[],UnlistedEnchantments[],DisplayName,Lore]
			String[] compiledRule=ruleBlock.getItemMeta().getLore().get(0).split(catorgorySpacer);
			//Check length is correct
			if(compiledRule.length<9){
				throw new ExchangeRuleParseException("Compiled Rule too short: "+String.valueOf(compiledRule.length));
			}
			//Get Rule Type
			RuleType ruleType;
			if(showString(compiledRule[0]).equals("i")){
				ruleType=RuleType.INPUT;
			}
			else if(showString(compiledRule[0]).equals("o")){
				ruleType=RuleType.OUTPUT;
			}
			else{
				throw new ExchangeRuleParseException("Invalid Rule Type");
			}
			//Get Material
			Material material=Material.getMaterial(Integer.valueOf(showString(compiledRule[1])));
			//Get Durability
			short durability=Short.valueOf(showString(compiledRule[2]));
			//Get Amount
			int amount=Integer.parseInt(showString(compiledRule[3]));
			//Get Required Enchantments
			Map<Enchantment,Integer> requiredEnchantments=new HashMap<Enchantment,Integer>();
			for(int i=1;i<compiledRule[4].split(secondarySpacer).length;i++){
				String compiledEnchant=compiledRule[4].split(secondarySpacer)[i];
				Enchantment enchantment=Enchantment.getById(Integer.valueOf(showString(compiledEnchant.split(tertiarySpacer)[0])));
				Integer level=Integer.valueOf(showString(compiledEnchant.split(tertiarySpacer)[1]));
				requiredEnchantments.put(enchantment, level);
			}
			//Get Excluded Enchantments
			Map<Enchantment,Integer> excludedEnchantments=new HashMap<Enchantment,Integer>();
			for(int i=1;i<compiledRule[5].split(secondarySpacer).length;i++){
				String compiledEnchant=compiledRule[4].split(secondarySpacer)[i];
				Enchantment enchantment=Enchantment.getById(Integer.valueOf(showString(compiledEnchant.split(tertiarySpacer)[0])));
				Integer level=Integer.valueOf(showString(compiledEnchant.split(tertiarySpacer)[1]));
				excludedEnchantments.put(enchantment, level);
			}
			//Get if unlisted enchantments are allowed
			boolean unlistedEnchantmentsAllowed;
			if(showString(compiledRule[6]).equals("0")){
				unlistedEnchantmentsAllowed=false;
			}
			else if(showString(compiledRule[6]).equals("1")){
				unlistedEnchantmentsAllowed=true;
			}
			else{
				throw new ExchangeRuleParseException("Invalid Rule Type");
			}
			//Get DisplayName
			String displayName="";
			if(!compiledRule[7].equals("")){
				displayName=showString(compiledRule[7]);
			}
			//Get Lore
			String[] lore=new String[0];
			if(!compiledRule[8].equals("")){
				lore=showString(compiledRule[8]).split(secondarySpacer);
			}
			return new ExchangeRule(material, amount, durability, requiredEnchantments,excludedEnchantments, unlistedEnchantmentsAllowed, displayName,lore,ruleType);
		}
		catch(Exception e){
			e.printStackTrace();
			throw new ExchangeRuleParseException("Invalid Exchange Rule");
		}
	}
	/*
	 * Removes § from string
	 * 
	 */
	private static String showString(String string){
		return StringUtils.join(string.split("§"));
	}
	/*
	 * Adds a § infront of every character in a string
	 */
	private static String hideString(String string){
		String hiddenString="";
		for(char character:string.toCharArray()){
			hiddenString+="§"+character;
		}
		return hiddenString;
	}
	/*
	 * Parse create command into an exchange rule
	 */
	public static ExchangeRule parseCreateCommand(String[] args) throws ExchangeRuleParseException{
		try{
			//Parse ruletype
			RuleType ruleType=null;
			if(args[0].equalsIgnoreCase("input")) {
				ruleType=ExchangeRule.RuleType.INPUT;
			}
			else if(args[0].equalsIgnoreCase("output")) {
				ruleType=ExchangeRule.RuleType.INPUT;
			}
			if(ruleType!=null){
				Material material=null;
				short durability=0;
				int amount=1;
				if(args.length>=2) {
					if(ItemExchangePlugin.NAME_MATERIAL.containsKey(args[2])){
						ItemStack itemStack=ItemExchangePlugin.NAME_MATERIAL.get(args[2]);
						material=itemStack.getType();
						durability=itemStack.getDurability();
					}
					else {
						String[] split=args[2].split(":");
						material=Material.getMaterial(Integer.valueOf(split[0]));
						if(split.length>1) {
							durability=Short.valueOf(split[1]);
						}
					}
					if(args.length>=3) {
						amount=Integer.valueOf(args[3]);
					}
				}
				return new ExchangeRule(material,amount,durability,ruleType);
			}
			else {
				throw new ExchangeRuleParseException("Please specify and input or output.");
			}
		}
		catch(Exception e){
			throw new ExchangeRuleParseException("Invalid Exchange Rule");
		}
	}
	/*
	 * Stores the exchange rule as an item stack
	 */
	public ItemStack toItemStack()
	{
		ItemStack itemStack=ItemExchangePlugin.ITEM_RULE_ITEMSTACK.clone();
		
		ItemMeta itemMeta=itemStack.getItemMeta();
		itemMeta.setDisplayName("§r"+(ruleType==RuleType.INPUT ? "Input" : "Output")+": "+String.valueOf(amount)+" "+material.name()+":"+String.valueOf(durability));
		List<String> isLore=new ArrayList<>();
		isLore.add(compileRule());
		itemMeta.setLore(isLore);
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
	/*
	 * Saves the exchange rule to lore in a semi-readable fashion
	 */
	public String compileRule() {
		String catorgorySpacer="§&§&§&§r";
		String secondarySpacer="§&§&§r";
		String tertiarySpacer="§&§r";
		String compiledRule="";
		//RuleType
		compiledRule+=ruleType.equals(RuleType.INPUT) ? hideString("i") : hideString("o");
		//Material ID
		compiledRule+=catorgorySpacer+hideString(String.valueOf(material.getId()));
		//Durability
		compiledRule+=catorgorySpacer+hideString(String.valueOf(durability));
		//Amount
		compiledRule+=catorgorySpacer+hideString(String.valueOf(amount));
		compiledRule+=catorgorySpacer;
		ItemExchangePlugin.sendConsoleMessage(String.valueOf(requiredEnchantments.size()));
		for(Entry<Enchantment,Integer> entry:requiredEnchantments.entrySet()){
			compiledRule+=hideString(String.valueOf(entry.getKey().getId()))+tertiarySpacer+hideString(entry.getValue().toString())+secondarySpacer;
		}
		compiledRule+=catorgorySpacer;
		for(Entry<Enchantment,Integer> entry:excludedEnchantments.entrySet()){
			compiledRule+=hideString(String.valueOf(entry.getKey().getId()))+tertiarySpacer+hideString(String.valueOf(entry))+secondarySpacer;
		}
		compiledRule+=catorgorySpacer+(unlistedEnchantmentsAllowed ? hideString("1") : hideString("0"));
		compiledRule+=catorgorySpacer+hideString(displayName);
		compiledRule+=catorgorySpacer;
		for(String line:lore){
			compiledRule+=secondarySpacer+hideString(displayName);
		}
		compiledRule+=catorgorySpacer+"§r";
		return compiledRule;
	}
	/*
	 * Checks if an inventory has enough items which follow the ItemRules
	 */
	public boolean followsRules(Inventory inventory)
	{
		int invAmount=0;
		for(ItemStack itemStack:inventory.getContents())
		{
			if(itemStack!=null&&followsRules(itemStack))
			{
				invAmount+=itemStack.getAmount();
			}
		}
		return invAmount>=amount;
	}
	/*
	 * Checks if the given ItemStack follows the ItemRules except for the amount
	 */
	public boolean followsRules(ItemStack itemStack)
	{
		//check material type and druability
		boolean followsRules=material.getId()==itemStack.getTypeId() && durability==itemStack.getDurability();
		//Check enchantments
		if(itemStack.getEnchantments().size()>0){
			followsRules=followsRules && itemStack.getEnchantments().entrySet().containsAll(requiredEnchantments.entrySet());
			for(Entry<Enchantment,Integer> excludedEnchantment:excludedEnchantments.entrySet()){
				followsRules=followsRules && !itemStack.getEnchantments().entrySet().contains(excludedEnchantment);
			}
		}
		else if(requiredEnchantments.size()>0){
			followsRules=false;
		}
		//Check displayName and Lore
		if(itemStack.hasItemMeta())
		{
			ItemMeta itemMeta=itemStack.getItemMeta();
			if(itemMeta.hasDisplayName())
			{
				followsRules=followsRules && displayName.equals(itemMeta.getDisplayName());
			}
			else
			{
				followsRules=followsRules && displayName.equals("");
			}
			if(itemMeta.hasLore())
			{
				for(int i=0;i<itemMeta.getLore().size()&&i<lore.length;i++)
				{
					followsRules=followsRules && lore[i].equals(itemMeta.getLore().get(i));
				}
				followsRules=followsRules && itemMeta.getLore().size()==lore.length;
			}
			else
			{
				followsRules=followsRules && lore.length==0;
			}
		}
		else
		{
			followsRules=followsRules && displayName.equals("") && lore.length==0;
		}
		return followsRules;
	}
	public String[] display(){
		List<String> displayed=new ArrayList<>();
		//Material type, durability and amount
		displayed.add(new StringBuilder().append(ChatColor.YELLOW).append((ruleType==RuleType.INPUT ? "Input" : "Output")+": "+ChatColor.WHITE).append(amount).append(" "+material.name()+":").append(durability).append(displayName.equals("") ? "" : "\""+displayName+"\"").toString());
		//Enchantments
		displayed.add(displayedEnchantments());
		//Lore
		if(lore.length==1){
			displayed.add(ChatColor.DARK_PURPLE+lore[0]);
		}
		else if(lore.length>1){
			displayed.add(ChatColor.DARK_PURPLE+lore[0]+"...");
		}
		return displayed.toArray(new String[displayed.size()]);
	}
	private String displayedEnchantments(){
		if(requiredEnchantments.size()>0||excludedEnchantments.size()>0){
			StringBuilder stringBuilder=new StringBuilder();
			for(Entry<Enchantment,Integer> entry:requiredEnchantments.entrySet()){
				stringBuilder.append(ChatColor.GREEN);
				stringBuilder.append(ItemExchangePlugin.ENCHANTMENT_ABBRV.get(entry.getKey().getName()));
				stringBuilder.append(entry.getValue());
				stringBuilder.append(" ");
			}
			for(Entry<Enchantment,Integer> entry:excludedEnchantments.entrySet()){
				stringBuilder.append(ChatColor.RED);
				stringBuilder.append(ItemExchangePlugin.ENCHANTMENT_ABBRV.get(entry.getKey().getName()));
				stringBuilder.append(entry.getValue());
				stringBuilder.append(" ");
			}
			stringBuilder.append(unlistedEnchantmentsAllowed ? ChatColor.GREEN+"Other Enchantments Allowed." : ChatColor.RED+"Other Enchantments Disallowed");
			return stringBuilder.toString();
		}
		else{
			return unlistedEnchantmentsAllowed ? ChatColor.GREEN+"Any enchantments allowed" : ChatColor.RED+"No enchantments allowed";
		}
	}
	public void setMaterial(Material material){
		this.material=material;
	}
	public void requireEnchantment(Enchantment enchantment,Integer level){
		requiredEnchantments.put(enchantment, level);
	}
	public void excludeEnchantment(Enchantment enchantment, Integer level){
		excludedEnchantments.put(enchantment, level);
	}
	public void setAmount(int amount){
		this.amount=amount;
	}
	public void setDurability(short durability){
		this.durability=durability;
	}
	public void setDisplayName(String displayName){
		this.displayName=displayName;
	}
	public void setLore(String[] lore){
		this.lore=lore;
	}
	public void switchIO(){
		ruleType=ruleType==RuleType.INPUT ? RuleType.OUTPUT : RuleType.INPUT;
	}
	public int getAmount(){
		return amount;
	}
	public RuleType getType(){
		return ruleType;
	}
}
