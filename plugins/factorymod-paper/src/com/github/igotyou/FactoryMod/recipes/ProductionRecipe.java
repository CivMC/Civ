package com.github.igotyou.FactoryMod.recipes;

import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Recipe;

public class ProductionRecipe implements Recipe
{
	private String title;
	private String recipeName;
	private int productionTime;
	private Map<ItemStack,String> inputs;
	private Map<ItemStack,String> upgrades;
	private Map<ItemStack,String> outputs;
	private Map<ItemStack,String> repairs;
	private List<ProductionRecipe> outputRecipes;
	private List<ProbabilisticEnchantment> enchantments;
	private boolean useOnce;
	private int maintenance;
	int totalNumber;
	
	public ProductionRecipe(String title,String recipeName,int productionTime,Map<ItemStack,String> inputs,Map<ItemStack,String> upgrades,
		Map<ItemStack,String> outputs,List<ProbabilisticEnchantment> enchantments,boolean useOnce,int maintenance, Map<ItemStack,String> repairs)
	{
		this.title=title;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
		this.inputs = inputs;
		this.upgrades=upgrades;
		this.outputs = outputs;
		this.outputRecipes=new ArrayList<ProductionRecipe>();
		this.enchantments=enchantments;
		this.useOnce=useOnce;
		this.maintenance=maintenance;
		int totalNumber=0;
		this.repairs=repairs;
	}
	
	public ProductionRecipe(String title,String recipeName,int productionTime,Map<ItemStack,String> repairs)
	{
		this(title,recipeName,productionTime,new HashMap<ItemStack,String>(),new HashMap<ItemStack,String>(),new HashMap<ItemStack,String>(),new ArrayList<ProbabilisticEnchantment>(),false,0,repairs);
	}
	
	public void addOutputRecipe(ProductionRecipe outputRecipe)
	{
		this.outputRecipes.add(outputRecipe);
	}

	public Map<ItemStack,String> getInputs()
	{
		return inputs;
	}
	
	public Map<ItemStack,String> getUpgrades()
	{
		return upgrades;
	}
	
	public Map<ItemStack,String> getOutputs() 
	{
		return outputs;
	}
	
	public Map<ItemStack,String> getRepairs()
	{
		return repairs;
	}

	public HashMap<Enchantment, Integer> getEnchantments()
	{
		HashMap<Enchantment, Integer> randomEnchantments = new HashMap<Enchantment, Integer>();
		Random rand = new Random();
		for(int i=0;i<enchantments.size();i++)
		{
			if(enchantments.get(i).getProbability()>=rand.nextDouble())
			{
				randomEnchantments.put(enchantments.get(i).getEnchantment(),enchantments.get(i).getLevel());
			}
		}
		return randomEnchantments;
	}
	
	public boolean hasEnchantments()
	{
		return enchantments.size()>0;
	}
	
	public String getTitle()
	{
		return title;
	}
	public String getRecipeName() 
	{
		return recipeName;
	}

	public int getProductionTime() 
	{
		return productionTime;
	}

	public List<ProductionRecipe> getOutputRecipes()
	{
		return outputRecipes;
	}

	public boolean getUseOnce()
	{
		return useOnce;
	}
	
	public int getMaintenance()
	{
		return maintenance;
	}
	
	public double degradeFactory()
	{
		return maintenance/10.0;
	}
	public void setTotalNumber(int number)
	{
		totalNumber=number;
	}
	public void incrementCount()
	{
		totalNumber++;
	}
}
