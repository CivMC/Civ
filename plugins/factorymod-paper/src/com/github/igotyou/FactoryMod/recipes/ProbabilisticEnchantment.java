package com.github.igotyou.FactoryMod.recipes;

import org.bukkit.enchantments.Enchantment;

/**
 *
 * @author Brian Landry
 */
public class ProbabilisticEnchantment {
	private String name;
	private Enchantment enchantment;
	private int level;
	private double probability;

	public ProbabilisticEnchantment(String name, String enchantment,Integer level,double probability){
		this.name=name;
		this.enchantment=Enchantment.getByName(enchantment);
		this.level=level.intValue();
		this.probability=probability;
	}
		
	public String getName()
	{
		return name;
	}
	
	public Enchantment getEnchantment()
	{
		return enchantment;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public double getProbability()
	{
		return probability;
	}

}
