package com.untamedears.ItemExchange.utility;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ItemRule {
	private Material material;
	private int amount;
	private short durability;
	private Map<Enchantment,Integer> requiredEnchantments;
	private String displayName;
	private String[] lore;
	public static enum RuleType
	{
		INPUT,
		OUTPUT
	}
	/*
	 * Creates an ItemRules which matches the ItemStack
	 */
	public ItemRule(ItemStack itemStack)
	{
		this.material=itemStack.getType();
		this.amount=itemStack.getAmount();
		this.durability=itemStack.getDurability();
		requiredEnchantments=new HashMap<>();
		for(Enchantment enchantment:itemStack.getEnchantments().keySet())
		{
			requiredEnchantments.put(enchantment, itemStack.getEnchantments().get(enchantment));
		}
		if(itemStack.hasItemMeta())
		{
			ItemMeta itemMeta=itemStack.getItemMeta();
			if(itemMeta.hasDisplayName())
			{
				this.displayName=itemMeta.getDisplayName();
			}
			else
			{
				this.displayName="";
			}
			if(itemMeta.hasLore())
			{
				this.lore=(String[])itemMeta.getLore().toArray();
			}
			else
			{
				this.lore=new String[0];
			}
		}
		else
		{
			this.displayName="";
			this.lore=new String[0];
		}
	}
	
	/*
	 * Creates an ItemRules which parsed from a set of lore
	 */
	public ItemRule(List<String> savedItemRules,String version)
	{
		String[] itemData=savedItemRules.get(0).split("§a");
		this.amount=Integer.valueOf(itemData[0].substring(0,itemData[0].length()-2));
		if(itemData.length==2)
		{
			Pair<Material,Short> pair=ItemExchangePlugin.NAME_MATERIAL.get(itemData[1].substring(0,itemData[1].length()-1));
			this.material=pair.getFirst();
			this.durability=pair.getSecond();
		}
		else
		{
			this.material=Material.getMaterial(itemData[1].substring(0,itemData[1].length()-1));
			this.durability=Short.valueOf(itemData[2]);
		}
		//Parse Enchantments
		for(String abbrv:savedItemRules.get(1).split("§"))
		{
			String abbrvEnchantment=abbrv.subSequence(1,abbrv.length()-2).toString();
			if(abbrv.charAt(0)=='a')
			{
				if(ItemExchangePlugin.ABBRV_ENCHANTMENT.containsKey(abbrvEnchantment))
				{
					Enchantment enchantment=Enchantment.getByName(ItemExchangePlugin.ABBRV_ENCHANTMENT.get(abbrvEnchantment));
					Integer level=Integer.valueOf(abbrv.charAt(abbrv.length()-1));
					requiredEnchantments.put(enchantment, level);
				}
				else
				{
					Enchantment enchantment=Enchantment.getByName(abbrvEnchantment);
					Integer level=Integer.valueOf(abbrv.charAt(abbrv.length()-1));
					requiredEnchantments.put(enchantment, level);
				}
			}
		}
		//Parse DisplayName
		if(savedItemRules.size()>1)
		{
			this.displayName=savedItemRules.get(2);
		}
		lore=new String[savedItemRules.size()-3];
		for(int i=3;i<savedItemRules.size();i++)
		{
			lore[i]=savedItemRules.get(i);
		}
	}
	public ItemStack toItemStack(RuleType itemType)
	{
		ItemStack itemStack=ItemExchangePlugin.ITEM_RULE_ITEMSTACK.clone();
		ItemMeta itemMeta=itemStack.getItemMeta();
		if(itemType==RuleType.INPUT)
		{
			itemMeta.setDisplayName("Input Rule Block "+ItemExchangePlugin.VERSION);
		}
		else if(itemType==RuleType.OUTPUT)
		{
			itemMeta.setDisplayName("Output Rules Block "+ItemExchangePlugin.VERSION);
		}
		itemMeta.setLore(Arrays.asList(saveToLore()));
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
	/*
	 * Saves the ItemRules to lore in a semi-readable fashion
	 */
	public String[] saveToLore()
	{
		String[] savedLore=new String[3+lore.length];
		savedLore[0]="§a"+String.valueOf(amount)+" ";
		if(ItemExchangePlugin.MATERIAL_NAME.containsKey(new Pair(material,Short.valueOf(durability))))
		{
			savedLore[0]+="§a"+ItemExchangePlugin.MATERIAL_NAME.get(new Pair(material,Short.valueOf(durability)));
		}
		else
		{
			savedLore[0]+="§a"+material.name()+" §a"+String.valueOf(durability);
		}
		savedLore[1]="";
		for(Map.Entry<Enchantment,Integer> enchantment:requiredEnchantments.entrySet())
		{
			savedLore[1]+="§a"+ItemExchangePlugin.ENCHANTMENT_ABBRV.get(enchantment.getKey())+enchantment.getValue().toString()+" ";
		}
		savedLore[2]=displayName;
		for(int i=0;i<lore.length;i++)
		{
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
			if(followsRules(itemStack))
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
		boolean followsRules=material.getId()==itemStack.getTypeId() && durability==itemStack.getDurability();
		followsRules=followsRules && requiredEnchantments.entrySet().containsAll(itemStack.getEnchantments().entrySet());
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
	
	public int getAmount()
	{
		return amount;
	}
}
