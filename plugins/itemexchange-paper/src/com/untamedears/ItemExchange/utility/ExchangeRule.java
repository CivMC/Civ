package com.untamedears.ItemExchange.utility;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	private String displayName;
	private String[] lore;
	private RuleType ruleType;
	public static enum RuleType
	{
		INPUT,
		OUTPUT
	}
	public ExchangeRule(Material material, int amount, short durability,RuleType ruleType) {
		this(material,amount,durability,new HashMap<Enchantment,Integer>(), new HashMap<Enchantment,Integer>(), "", new String[0],ruleType);
	}
	public ExchangeRule(Material material, int amount, short durability,
		Map<Enchantment,Integer> requiredEnchantments, Map<Enchantment,Integer> excludedEnchantments,
		String displayName, String[] lore,RuleType ruleType){
		this.material=material;
		this.amount=amount;
		this.durability=durability;
		this.requiredEnchantments=requiredEnchantments;
		this.excludedEnchantments=excludedEnchantments;
		this.displayName=displayName;
		this.lore=lore;
		this.ruleType=ruleType;
	}
	/*
	 * Parses an ItemStack into an ExchangeRule which represents the ItemStack
	 */
	public static ExchangeRule parseItemStack(ItemStack itemStack,RuleType ruleType)
	{
		Map<Enchantment,Integer> requiredEnchantments=new HashMap<Enchantment,Integer>();
		for(Enchantment enchantment:itemStack.getEnchantments().keySet())
		{
			requiredEnchantments.put(enchantment, itemStack.getEnchantments().get(enchantment));
		}
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
				lore=(String[])itemMeta.getLore().toArray();
			}
		}
		return new ExchangeRule(itemStack.getType(),itemStack.getAmount(),itemStack.getDurability(),requiredEnchantments,new HashMap<Enchantment,Integer>(),displayName,lore,ruleType);
	}
	
	/*
	 * Parses an RuleBlock into an ExchangeRule
	 */
	public static ExchangeRule parseRuleBlock(ItemStack ruleBlock) throws ExchangeRuleParseException{
		try{
			RuleType ruleType=null;
			if(ruleBlock.getItemMeta().getDisplayName().equals("Input Rule Block "+ItemExchangePlugin.VERSION)){
				ruleType=RuleType.INPUT;
			}
			else if(ruleBlock.getItemMeta().getDisplayName().equals("Output Rule Block "+ItemExchangePlugin.VERSION)){
				ruleType=RuleType.OUTPUT;
			}
			else {
				throw new ExchangeRuleParseException("Invalid DisplayName");
			}
			List<String> savedItemRules=ruleBlock.getItemMeta().getLore();
			String[] itemData=savedItemRules.get(0).split("§a");
			int amount=Integer.valueOf(itemData[1].substring(0, itemData[1].length()-1));
			Material material;
			short durability;
			//Called if ExchangeRule is dictacted by a common name
			if(itemData.length==3)
			{
				ItemStack itemStack=ItemExchangePlugin.NAME_MATERIAL.get(itemData[2]);
				material=itemStack.getType();
				durability=itemStack.getDurability();
			}
			//Called if ExchangeRule is dicacted by a Material and durability value
			else
			{
				material=Material.getMaterial(itemData[2].substring(0,itemData[2].length()-1));
				durability=Short.valueOf(itemData[3]);
			}
			//Parse Enchantments
			Map<Enchantment,Integer> requiredEnchantments=new HashMap<Enchantment,Integer>();
			Map<Enchantment,Integer> excludedEnchantments=new HashMap<Enchantment,Integer>();
			String[] parsedEnchantments=savedItemRules.get(1).split("§");
			for(int i=1;i<parsedEnchantments.length;i++) {
				String parsedEnchantment=parsedEnchantments[i];
				Enchantment enchantment=Enchantment.getByName(ItemExchangePlugin.ABBRV_ENCHANTMENT.get(parsedEnchantment.substring(1, parsedEnchantment.length()-2)));
				Integer level=Integer.valueOf(parsedEnchantment.charAt(parsedEnchantment.length()-1));
				if(parsedEnchantment.charAt(0)=='a'){
					requiredEnchantments.put(enchantment, level);					
				}
				else if(parsedEnchantment.charAt(0)=='b'){
					excludedEnchantments.put(enchantment, level);
				}
			}
			//Parse DisplayName
			String displayName="";
			if(savedItemRules.size()>1)
			{
				displayName=savedItemRules.get(2);
			}
			String[] lore=new String[savedItemRules.size()-3];
			for(int i=3;i<savedItemRules.size();i++)
			{
				lore[i]=savedItemRules.get(i);
			}

			return new ExchangeRule(material, amount, durability, requiredEnchantments,excludedEnchantments,displayName,lore,ruleType);
		}
		catch(Exception e){
			throw new ExchangeRuleParseException("Invalid Exchange Rule");
		}
	}
	/*
	 * Parse create command
	 */
	public static ExchangeRule parseCreateCommand(String[] args) throws ExchangeRuleParseException{
		try{
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
	public ItemStack toItemStack()
	{
		ItemStack itemStack=ItemExchangePlugin.ITEM_RULE_ITEMSTACK.clone();
		ItemMeta itemMeta=itemStack.getItemMeta();
		if(ruleType==RuleType.INPUT)
		{
			itemMeta.setDisplayName("Input Rule Block "+ItemExchangePlugin.VERSION);
		}
		else if(ruleType==RuleType.OUTPUT)
		{
			itemMeta.setDisplayName("Output Rule Block "+ItemExchangePlugin.VERSION);
		}
		itemMeta.setLore(Arrays.asList(saveToLore()));
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
	/*
	 * Saves the ItemRules to lore in a semi-readable fashion
	 */
	public String[] saveToLore() {
		String[] savedLore=new String[3+lore.length];
		savedLore[0]="§a"+String.valueOf(amount)+" ";
		if(ItemExchangePlugin.MATERIAL_NAME.containsKey(new ItemStack(material,Short.valueOf(durability)))) {
			savedLore[0]+="§a"+ItemExchangePlugin.MATERIAL_NAME.get(new ItemStack(material,Short.valueOf(durability)));
		}
		else {
			savedLore[0]+="§a"+material.name()+" §a"+String.valueOf(durability);
		}
		savedLore[1]="";
		for(Map.Entry<Enchantment,Integer> enchantment:requiredEnchantments.entrySet()) {
			savedLore[1]+="§a"+ItemExchangePlugin.ENCHANTMENT_ABBRV.get(enchantment.getKey().getName())+enchantment.getValue().toString()+" ";
		}
		savedLore[2]=displayName;
		for(int i=0;i<lore.length;i++) {
			savedLore[3+i]+=lore[i];
		}
		return savedLore;
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
			followsRules=followsRules && requiredEnchantments.entrySet().containsAll(itemStack.getEnchantments().entrySet());
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
	public void setMaterial(Material material){
		this.material=material;
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
