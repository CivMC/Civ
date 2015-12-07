package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.properties.IFactoryProperties;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.recipes.EnchantmentOptions;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.ProbabilisticEnchantment;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;

public class ProductionFactory extends ABaseFactory
{

	private ProductionRecipe currentRecipe = null;//the recipe that is currently selected
	private ProductionProperties productionFactoryProperties;//the properties of the production factory
	public static final FactoryType FACTORY_TYPE = FactoryType.PRODUCTION;//the factory's type
	private List<ProductionRecipe> recipes;
	private int currentRecipeNumber = 0;//the array index of the current recipe
	
	/**
	 * Constructor
	 */
	public ProductionFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource
			, String subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, ProductionFactory.FACTORY_TYPE, subFactoryType);
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.recipes=new ArrayList<ProductionRecipe> (productionFactoryProperties.getRecipes());
		this.setRecipeToNumber(0);
	}

	/**
	 * Constructor
	 */
	public ProductionFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			String subFactoryType, boolean active, int currentProductionTimer, int currentEnergyTimer,  List<ProductionRecipe> recipes,
			int currentRecipeNumber,double currentMaintenance,long timeDisrepair)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, ProductionFactory.FACTORY_TYPE, active, subFactoryType, currentProductionTimer, currentEnergyTimer, currentMaintenance, timeDisrepair);
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.recipes=recipes;
		this.setRecipeToNumber(currentRecipeNumber);
	}
	
	@Override
	public boolean checkHasMaterials() {
		return currentRecipe.hasMaterials(getInventory());
	}
	
	@Override
	public boolean isRepairing() {
		return currentRecipe.getRepairs().size()!=0;
	}
	
	
	/**
	 * Returns either a success or error message.
	 * Called by the blockListener when a player left clicks the center block, with the InteractionMaterial
	 */
	@Override
	public List<InteractionResponse> getCentralBlockResponse()
	{
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		//Is the factory off
		if (!active)
		{
			//is the recipe is initiaed
			if (currentRecipe != null)
			{		
				//if we are at the end of the recipe array loop around
				if (currentRecipeNumber == recipes.size() - 1)
				{
					setRecipeToNumber(0);
					currentProductionTimer = 0;
				}
				//if we can just increment the recipe
				else
				{
					setRecipeToNumber(currentRecipeNumber + 1);
					currentProductionTimer = 0;
				}
			}
			//if the recipe for some reason is not initialised, initialise it to recipe 0
			else
			{
				setRecipeToNumber(0);
				currentProductionTimer = 0;
			}
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "-----------------------------------------------------"));
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Switched recipe to: " + currentRecipe.getRecipeName()+"."));
			if(currentRecipeNumber != recipes.size() - 1)
			{
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Next Recipe is: "+recipes.get(currentRecipeNumber+1).getRecipeName()+"."));
			}
			else
			{
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Next Recipe is: "+recipes.get(0).getRecipeName()+"."));
			}
		}
		//if the factory is on, return error message
		else
		{
			responses.add(new InteractionResponse(InteractionResult.FAILURE, "You can't change recipes while the factory is on! Turn it off first."));
		}
		return responses;
	}
	
	@Override
	public ItemList<AdvancedItemStack> getFuel() {
		return productionFactoryProperties.getFuel();
	}

	/**
	 * Sets the factories current recipe.
	 * @param newRecipe the desired recipe
	 */
	public void setRecipe(IRecipe newRecipe)
	{
		if (newRecipe instanceof ProductionRecipe)
		{
			currentRecipe = (ProductionRecipe) newRecipe;
		}
	}
	
	/**
	 * sets the recipe to the supplied index
	 * @param newRecipeNumber the desired recipeArray index
	 */
	public void setRecipeToNumber(int newRecipeNumber)
	{
		if (newRecipeNumber<recipes.size())
		{
			currentRecipe = recipes.get(newRecipeNumber);
			currentRecipeNumber = newRecipeNumber;
		}
		else
		{
			currentRecipe=recipes.get(0);
			currentRecipeNumber=0;
		}
	}
	/**
	 * Returns the currentRecipe
	 */
	public ProductionRecipe getCurrentRecipe()
	{
		return currentRecipe;
	}
	
	/**
	 * Returns the RecipeArray index
	 */
	public int getCurrentRecipeNumber()
	{
		return currentRecipeNumber;
	}
	
	/**
	 * Returns the factory's properties
	 */
	public ProductionProperties getProductionFactoryProperties()
	{
		return productionFactoryProperties;
	}
	
	public List<ProductionRecipe> getRecipes()
	{
		return recipes;
	}
	
	@Override
	public List<InteractionResponse> getChestResponse()
	{
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		String status=active ? "On" : "Off";
		String percentDone=status.equals("On") ? " - "+Math.round(currentProductionTimer*100/currentRecipe.getProductionTime())+"% done." : "";
		//Name: Status with XX% health.
		int health =(getProductionFactoryProperties().getRepair()==0) ? 100 : (int) Math.round(100*(1-currentRepair/(getProductionFactoryProperties().getRepair())));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, getProductionFactoryProperties().getName()+": "+status+" with "+String.valueOf(health)+"% health."));
		//RecipeName: X seconds(Y ticks)[ - XX% done.]
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, currentRecipe.getRecipeName()+": "+currentRecipe.getProductionTime() + " seconds("+ currentRecipe.getProductionTime()*FactoryModPlugin.TICKS_PER_SECOND + " ticks)"+percentDone));
		//[Inputs: amount Name, amount Name.]
		if(!currentRecipe.getInputs().isEmpty())
		{
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Input: "+currentRecipe.getInputs().toString()+"."));
		}
		//[Upgrades: amount Name, amount Name.]
		if(!currentRecipe.getUpgrades().isEmpty())
		{
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Upgrades: "+currentRecipe.getUpgrades().toString()+"."));
		}
		//[Outputs: amount Name, amount Name.]
		if(!getOutputs().isEmpty())
		{
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Output: "+getOutputs().toString()+"."));
		}
		//[Will repair XX% of the factory]
		if(!currentRecipe.getRepairs().isEmpty()&&getProductionFactoryProperties().getRepair()!=0)
		{
			int amountAvailable=currentRecipe.getRepairs().amountAvailable(getPowerSourceInventory());
			int amountRepaired=amountAvailable>currentRepair ? (int) Math.ceil(currentRepair) : amountAvailable;
			int percentRepaired=(int) (( (double) amountRepaired)/getProductionFactoryProperties().getRepair()*100);
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Will repair "+String.valueOf(percentRepaired)+"% of the factory with "+currentRecipe.getRepairs().getMultiple(amountRepaired).toString()+"."));
		}
		if(getProductionFactoryProperties().getRepair()!=0)
		if(!currentRecipe.getOutputRecipes().isEmpty())
		{
			List<ProductionRecipe> outputRecipes=currentRecipe.getOutputRecipes();
			String response="Makes available: ";
			for(int i=0;i<outputRecipes.size();i++)
			{
				response+=outputRecipes.get(i).getRecipeName();
				if(i<outputRecipes.size()-1)
				{
					response+=", ";
				}
			}
			response+=".";
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,response));
		}
		return responses;
	}
	
	protected void recipeFinished() {
		//Remove upgrade and replace it with its upgraded form
		currentRecipe.getUpgrades().removeOneFrom(getInventory()).putIn(getInventory(),currentRecipe.getEnchantments(), currentRecipe.getEnchantmentOptions());
		//Adds new recipes to the factory

		for (int i = 0; i < currentRecipe.getOutputRecipes().size();i++)
		{
			if(!recipes.contains(currentRecipe.getOutputRecipes().get(i)))
			{
				recipes.add(currentRecipe.getOutputRecipes().get(i));
			}
		}
		
		//Remove currentRecipe if it only is meant to be used once
		if(currentRecipe.getUseOnce())
		{
			recipes.remove(currentRecipe);
			setRecipeToNumber(0);
		}
	}

	@Override
	public ItemList<AdvancedItemStack> getInputs() {
		return currentRecipe.getInputs();
	}

	@Override
	public ItemList<AdvancedItemStack> getOutputs() {
		return currentRecipe.getOutputs();
	}

	@Override
	public ItemList<AdvancedItemStack> getRepairs() {
		return currentRecipe.getRepairs();
	}

	@Override
	public List<ProbabilisticEnchantment> getEnchantments() {
		return currentRecipe.getEnchantments();
	}

	@Override
	public double getEnergyTime() {
		return productionFactoryProperties.getEnergyTime();
	}

	@Override
	public double getProductionTime() {
		return currentRecipe.getProductionTime();
	}
	
	@Override
	public EnchantmentOptions getEnchantmentOptions() {
		return currentRecipe.getEnchantmentOptions();
	}

	@Override
	public int getMaxRepair() {
		return productionFactoryProperties.getRepair();
	}

	@Override
	public IFactoryProperties getProperties() {
		return productionFactoryProperties;
	}
}
