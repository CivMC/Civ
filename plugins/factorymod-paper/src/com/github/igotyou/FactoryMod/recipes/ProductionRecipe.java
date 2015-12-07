package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;

public class ProductionRecipe implements IRecipe
{
	private String title;
	private String recipeName;
	private int productionTime;
	private ItemList<AdvancedItemStack> inputs;
	private ItemList<AdvancedItemStack> upgrades;
	private ItemList<AdvancedItemStack> outputs;
	private ItemList<AdvancedItemStack> repairs;
	private List<ProductionRecipe> outputRecipes;
	private List<ProbabilisticEnchantment> enchantments;
	private EnchantmentOptions enchantmentOptions;
	private boolean useOnce;
	
	public ProductionRecipe(String title,String recipeName,int productionTime,ItemList<AdvancedItemStack> inputs,ItemList<AdvancedItemStack> upgrades,
		ItemList<AdvancedItemStack> outputs, EnchantmentOptions enchantmentOptions, List<ProbabilisticEnchantment> enchantments,boolean useOnce, ItemList<AdvancedItemStack> repairs)
	{
		this.title=title;
		this.recipeName = recipeName;
		this.productionTime = productionTime;
		this.inputs = inputs;
		this.upgrades=upgrades;
		this.outputs = outputs;
		this.outputRecipes=new ArrayList<ProductionRecipe>();
		this.enchantmentOptions = enchantmentOptions;
		this.enchantments=enchantments;
		this.useOnce=useOnce;
		this.repairs=repairs;
	}
	
	public ProductionRecipe(String title,String recipeName,int productionTime,ItemList<AdvancedItemStack> repairs)
	{
		this(title,recipeName,productionTime,new ItemList<AdvancedItemStack>(),new ItemList<AdvancedItemStack>(),new ItemList<AdvancedItemStack>(),null,new ArrayList<ProbabilisticEnchantment>(),false,repairs);
	}
	
	public boolean hasMaterials(Inventory inventory)
	{
		return inputs.allIn(inventory)&&upgrades.oneIn(inventory)&&repairs.allIn(inventory);
	}
	public void addOutputRecipe(ProductionRecipe outputRecipe)
	{
		this.outputRecipes.add(outputRecipe);
	}

	public ItemList<AdvancedItemStack> getInputs()
	{
		return inputs;
	}
	
	public ItemList<AdvancedItemStack> getUpgrades()
	{
		return upgrades;
	}
	
	public ItemList<AdvancedItemStack> getOutputs() 
	{
		return outputs;
	}
	
	public ItemList<AdvancedItemStack> getRepairs()
	{
		return repairs;
	}

	public List<ProbabilisticEnchantment> getEnchantments()
	{
		return enchantments;
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

	public EnchantmentOptions getEnchantmentOptions() {
		if (enchantmentOptions == null) {
			return EnchantmentOptions.DEFAULT;
		} else {
			return enchantmentOptions;
		}
	}
}
