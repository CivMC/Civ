package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import java.util.HashMap;
import java.util.Random;
	
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Recipe;
import java.util.ArrayList;

public class ProductionRecipe implements Recipe
{
	private HashMap <Integer, ItemStack> input;
	private HashMap<Integer, ItemStack> output;
	private int productionTime;
	private String recipeName;
	private int number;
	private ArrayList <ProductionRecipe> outputRecipes;
	private boolean useOnce;
	private ArrayList<ProbabilisticEnchantment> probabilisticEnchantments;
	
	public ProductionRecipe(HashMap<Integer, ItemStack> input, HashMap<Integer, ItemStack>  output, String recipeName, 
			int productionTime,int number,boolean useOnce, ArrayList<Enchantment> enchantments,
			ArrayList<Integer> enchantmentLevels,ArrayList<Double> enchantmentProbabilities)
	{
		this.input = input;
		this.output = output;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
		this.number=number;
		this.outputRecipes=new ArrayList<ProductionRecipe>();
		this.useOnce=useOnce;
		probabilisticEnchantments = new ArrayList<ProbabilisticEnchantment>();
		for(int i=0;i<enchantments.size();i++)
		{
			probabilisticEnchantments.add(new ProbabilisticEnchantment(enchantments.get(i),enchantmentLevels.get(i),enchantmentProbabilities.get(i)));
		}
	}
	
	public void addOutputRecipe(ProductionRecipe outputRecipe)
	{
		this.outputRecipes.add(outputRecipe);
	}

	public HashMap<Integer, ItemStack>  getOutput() 
	{
		return output;
	}

	public HashMap<Integer, ItemStack> getInput()
	{
		return input;
	}
	
	public HashMap<Enchantment, Integer> getEnchantments()
	{
		HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
		Random rand = new Random();
		for(int i=0;i<probabilisticEnchantments.size();i++)
		{
			FactoryModPlugin.sendConsoleMessage(String.valueOf(probabilisticEnchantments.get(i).probability)+":"+String.valueOf(rand.nextDouble()));
			if(probabilisticEnchantments.get(i).probability<=rand.nextDouble())
			{
				enchantments.put(probabilisticEnchantments.get(i).enchantment,probabilisticEnchantments.get(i).level);
			}
		}
		return enchantments;
	}
	
	public String getRecipeName() 
	{
		return recipeName;
	}

	public int getProductionTime() 
	{
		return productionTime;
	}
	
	public int getNumber()
	{
		return number;
	}

	public ArrayList<ProductionRecipe> getOutputRecipes()
	{
		return outputRecipes;
	}

	public boolean getUseOnce()
	{
		return useOnce;
	}
	private class ProbabilisticEnchantment
	{
		private Enchantment enchantment;
		private int level;
		private double probability;
		
		
		private ProbabilisticEnchantment(Enchantment enchantment,Integer level,double probability){
			this.enchantment=enchantment;
			this.level=level.intValue();
			this.probability=probability;
		}
		
		
	}
}
