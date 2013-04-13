package com.github.igotyou.FactoryMod.Factorys;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.FactoryObject;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Recipe;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import java.util.Iterator;
import java.util.List;

public class ProductionFactory extends FactoryObject implements Factory
{

	private ProductionRecipe currentRecipe = null;//the recipe that is currently selected
	private ProductionProperties productionFactoryProperties;//the properties of the production factory
	private int currentProductionTimer = 0;//The "production timer", which trachs for how long the factory has been producing the selected recipe
	private int currentEnergyTimer = 0;//Time since last energy consumption(if there's no lag, it's in seconds)
	private int currentRecipeNumber = 0;//the array index of the current recipe
	public static final FactoryType FACTORY_TYPE = FactoryType.PRODUCTION;//the factrys type
	public String SUB_FACTORY_TYPE;//the sub-factory Type
	private List<ProductionRecipe> recipes;
	private int totalMaintenance;
	private double currentMaintenance;
	
	/**
	 * Constructor
	 */
	public ProductionFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource
			, String subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, ProductionFactory.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.recipes=productionFactoryProperties.getRecipes();
		this.setRecipeToNumber(0);
		updateMaintenance();
		this.currentMaintenance=0.0;
	}

	/**
	 * Constructor
	 */
	public ProductionFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			String subFactoryType, boolean active, int currentProductionTimer, int currentEnergyTimer,  List<ProductionRecipe> recipes,
			int currentRecipeNumber,double currentMaintenance)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, ProductionFactory.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.active = active;
		this.currentEnergyTimer = currentEnergyTimer;
		this.currentProductionTimer = currentProductionTimer;
		this.recipes=recipes;
		this.setRecipeToNumber(currentRecipeNumber);
		updateMaintenance();
		this.currentMaintenance=currentMaintenance;
	}
	
	/**
	 * called by the manager each update cycle
	 */
	public void update() 
	{
		//if factory is turned on
		if (active)
		{
			//if the materials required to produce the current recipe are in the factory inventory
			if (currentRecipe.hasMaterials(getInventory()))
			{
				//if the factory has been working for less than the required time for the recipe
				if (currentProductionTimer < currentRecipe.getProductionTime())
				{
					//if the factory power source inventory has enough fuel for at least 1 energyCycle
					if (isFuelAvailable())
					{
						//if the time since fuel was last consumed is equal to how often fuel needs to be consumed
						if (currentEnergyTimer == productionFactoryProperties.getEnergyTime())
						{
							//remove one fuel.
							productionFactoryProperties.getFuel().removeFrom(getPowerSourceInventory());
							//0 seconds since last fuel consumption
							currentEnergyTimer = 0;
						}
						//if we don't need to consume fuel, just increment the energy timer
						else
						{
							currentEnergyTimer++;
						}
						//increment the production timer
						currentProductionTimer ++;
					}
					//if there is no fuel Available turn off the factory
					else
					{
						powerOff();
					}
				}
				
				//if the production timer has reached the recipes production time remove input from chest, and add output material
				else if (currentProductionTimer >= currentRecipe.getProductionTime())
				{
					
					//Remove inputs from chest
					currentRecipe.getInputs().removeFrom(getInventory());
					FactoryModPlugin.sendConsoleMessage("InputsRemoveD!");
					//Remove upgrade and replace it with its upgraded form
					currentRecipe.getUpgrades().removeOneFrom(getInventory()).addEnchantments(currentRecipe.getEnchantments()).putIn(getInventory());
					FactoryModPlugin.sendConsoleMessage("Upgraded!");
					//Adds outputs to chest with appropriate enchantments
					currentRecipe.getOutputs().addEnchantments(currentRecipe.getEnchantments()).putIn(getInventory());
					FactoryModPlugin.sendConsoleMessage("Output!");
					//Adds new recipes to the factory
					for (int i = 0; i < currentRecipe.getOutputRecipes().size();i++)
					{
						if(!recipes.contains(currentRecipe.getOutputRecipes().get(i)))
						{
							recipes.add(currentRecipe.getOutputRecipes().get(i));
						}

					}
					updateMaintenance();
					FactoryModPlugin.sendConsoleMessage("Recipes Upgraded");
					//Repairs the factory
					int amountRepaired=currentRecipe.getRepairs().removeMaxFrom(getInventory(),(int)currentMaintenance);
					currentMaintenance-=amountRepaired;
					FactoryModPlugin.sendConsoleMessage("Repaired");
					
					//Remove currentRecipe if it only is meant to be used once
					if(currentRecipe.getUseOnce())
					{
						recipes.remove(currentRecipe);
						setRecipeToNumber(0);
					}
					FactoryModPlugin.sendConsoleMessage("Bye bYe recipe");
					
					currentProductionTimer = 0;
					powerOff();
				}
			}	
			else
			{
				powerOff();
			}
		}	
	}

	/**
	 * Turns the factory on
	 */
	public void powerOn() 
	{
		//lots of code to make the furnace light up, without loosing contents.
		Furnace furnace = (Furnace) factoryPowerSourceLocation.getBlock().getState();
		byte data = furnace.getData().getData();
		ItemStack[] oldContents = furnace.getInventory().getContents();
		furnace.getInventory().clear();
		factoryPowerSourceLocation.getBlock().setType(Material.BURNING_FURNACE);
		furnace = (Furnace) factoryPowerSourceLocation.getBlock().getState();
		furnace.setRawData(data);
		furnace.update();
		furnace.getInventory().setContents(oldContents);
		//put active to true
		active = true;
		//reset the production timer
		currentProductionTimer = 0;
	}

	/**
	 * Turns the factory off.
	 */
	public void powerOff() 
	{
		if(active)
		{
			//lots of code to make the furnace turn off, without loosing contents.
			Furnace furnace = (Furnace) factoryPowerSourceLocation.getBlock().getState();
			byte data = furnace.getData().getData();
			ItemStack[] oldContents = furnace.getInventory().getContents();
			furnace.getInventory().clear();
			factoryPowerSourceLocation.getBlock().setType(Material.FURNACE);
			furnace = (Furnace) factoryPowerSourceLocation.getBlock().getState();
			furnace.setRawData(data);
			furnace.update();
			furnace.getInventory().setContents(oldContents);
			//put active to false
			active = false;
			//reset the production timer
			currentProductionTimer = 0;
		}
	}

	/**
	 * Returns either a success or error message.
	 * Called by the blockListener when a player left clicks the powerSourceLocation with the InteractionMaterial
	 */
	public InteractionResponse togglePower() 
	{
		//if the factory is turned off
		if (!active)
		{
			//if the factory isn't broken or the current recipe can repair it
			if(!isBroken()||!currentRecipe.getRepairs().isEmpty())
			{
				//is there fuel enough for at least once energy cycle?
				if (isFuelAvailable())
				{
					//are there enough materials for the current recipe in the chest?
					if (currentRecipe.hasMaterials(getInventory()))
					{
						//turn the factory on
						powerOn();
						//return a success message
						return new InteractionResponse(InteractionResult.SUCCESS, "Factory activated!");
					}
					//there are not enough materials for the recipe!
					else
					{
						//return a failure message, containing which materials are needed for the recipe
						return new InteractionResponse(InteractionResult.FAILURE, currentRecipe.needMaterialsMessage(getInventory()));
					}
				}
				//if there isn't enough fuel for atleast on energy cycle
				else
				{
					//return a error message
					return new InteractionResponse(InteractionResult.FAILURE, "Factory is missing fuel!");
				}
			}
			else
			{
				return new InteractionResponse(InteractionResult.FAILURE, "Factory is broken!");
			}			
		}
		//if the factory is on already
		else
		{
			//turn the factory off
			powerOff();
			//return success message
			return new InteractionResponse(InteractionResult.FAILURE, "Factory has been deactivated!");
		}
	}
	
	/**
	 * Returns either a success or error message.
	 * Called by the blockListener when a player left clicks the center block, with the InteractionMaterial
	 */
	public InteractionResponse toggleRecipes()
	{
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
					return new InteractionResponse(InteractionResult.SUCCESS, "Recipe switched! Current recipe is:" + currentRecipe.getRecipeName());
				}
				//if we can just increment the recipe
				else
				{
					setRecipeToNumber(currentRecipeNumber + 1);
					currentProductionTimer = 0;
					return new InteractionResponse(InteractionResult.SUCCESS, "Recipe switched! Current recipe is:" + currentRecipe.getRecipeName());
				}
			}
			//if the recipe for some reason is not initialised, initialise it to recipe 0
			else
			{
				setRecipeToNumber(0);
				currentProductionTimer = 0;
				return new InteractionResponse(InteractionResult.SUCCESS, "Recipe selected! Current recipe is:" + currentRecipe.getRecipeName());
			}	
		}
		//if the factory is on, return error message
		else
		{
			return new InteractionResponse(InteractionResult.FAILURE, "You can't change recipes while the factory is on! Turn it off first.");
		}
	}
	
	/**
	 * Sets the factories current recipe.
	 * @param newRecipe the desired recipe
	 */
	public void setRecipe(Recipe newRecipe)
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
		currentRecipe = recipes.get(newRecipeNumber);
		currentRecipeNumber = newRecipeNumber;
	}

	/**
	 * returns the Location of the central block of the factory
	 */
	public Location getCenterLocation() 
	{
		return factoryLocation;
	}

	/**
	 * returns the Location of the factory Inventory
	 */
	public Location getInventoryLocation() 
	{
		return factoryInventoryLocation;
	}

	/**
	 * returns the Location of the factory power source
	 */
	public Location getPowerSourceLocation() 
	{
		return factoryPowerSourceLocation;
	}
	
	/**
	 * Checks if there is enough fuel Available for atleast once energy cycle
	 * @return true if there is enough fuel, false otherwise
	 */
	public boolean isFuelAvailable()
	{
		return productionFactoryProperties.getFuel().allIn(getPowerSourceInventory());
	}

	/**
	 * Called by the block listener when the player(or a entity) destroys the fatory
	 * Drops the build materials if the config says it shouls
	 */
	public void destroy(Location destroyLocation)
	{
		//Turn the factory off.
		powerOff();
		//Return the build materials?
		if (FactoryModPlugin.RETURN_BUILD_MATERIALS&&FactoryModPlugin.DESTRUCTIBLE_FACTORIES)
		{
			Iterator<NamedItemStack> materialItr=productionFactoryProperties.getInputs().iterator();
			while(materialItr.hasNext())
			{
				ItemStack item = materialItr.next();
				factoryLocation.getWorld().dropItemNaturally(destroyLocation, item);
			}
		}
		
		destroyLocation.getBlock().setType(Material.AIR);
	}
	/**
	 * Degrades the factory
	 */
	public void degrade()
	{
		updateMaintenance();
		//No need to run check if already completely degraded
		if(currentMaintenance<totalMaintenance){
			double degradation=0;
			for(ProductionRecipe recipe:recipes)
			{
				degradation+=recipe.degradeFactory();
			}
			currentMaintenance+=degradation;
		}
		//If totalMaintenance was exceeded set currentMaintance back to it
		if(currentMaintenance>totalMaintenance)
		{
			currentMaintenance=totalMaintenance;
		}
	}
	/**
	 * Recalculate the total maintenance of the factory
	 */
	private void updateMaintenance()
	{
		totalMaintenance=1;
		for(ProductionRecipe recipe:recipes)
		{
			totalMaintenance+=recipe.getMaintenance();
		}
	}
	public double getCurrentMaintenance()
	{
		return currentMaintenance;
	}
	public double getMaintenance()
	{
		double percentMaintenance=1;
		if(totalMaintenance!=0)
		{
			percentMaintenance=currentMaintenance/totalMaintenance;
		}
		return percentMaintenance;
	}
	/**
	 * Checks that a factory hasn't degraded too much
	 */
	public boolean isBroken()
	{
		return currentMaintenance>=totalMaintenance;
	}
	/**
	 * Returns the production timer
	 */
	public int getProductionTimer()
	{
		return currentProductionTimer;
	}
	
	/**
	 * Returns the energy timer
	 */
	public int getEnergyTimer()
	{
		return currentEnergyTimer;
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
	
}
