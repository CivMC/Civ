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
import com.github.igotyou.FactoryMod.utility.InventoryMethods;

public class ProductionFactory extends FactoryObject implements Factory
{

	private ProductionRecipe currentRecipe = null;//the recipe that is currently selected
	private ProductionProperties productionFactoryProperties;//the properties of the production factory
	private int currentProductionTimer = 0;//The "production timer", which trachs for how long the factory has been producing the selected recipe
	private int currentEnergyTimer = 0;//Time since last energy consumption(if there's no lag, it's in seconds)
	private int currentRecipeNumber = 0;//the array index of the current recipe
	public static final FactoryType FACTORY_TYPE = FactoryType.PRODUCTION;//the factrys type
	public String SUB_FACTORY_TYPE;//the sub-factory Type
	
	/**
	 * Constructor
	 */
	public ProductionFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource
			, String subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, ProductionFactory.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.setRecipeToNumber(0);

	}

	/**
	 * Constructor
	 */
	public ProductionFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			String subFactoryType, boolean active, int currentProductionTimer, int currentEnergyTimer, int currentRecipeNumber)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, ProductionFactory.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.active = active;
		this.currentEnergyTimer = currentEnergyTimer;
		this.currentProductionTimer = currentProductionTimer;
		this.setRecipeToNumber(currentRecipeNumber);
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
			if (InventoryMethods.areItemStacksAvilable(getInventory(), currentRecipe.getInput()))
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
							InventoryMethods.removeItemStack(getPowerSourceInventory(), productionFactoryProperties.getEnergyMaterial());
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
					//if there is no fuel avilable turn off the factory
					else
					{
						powerOff();
					}
				}
				
				//if the production timer has reached the recipes production time remove input from chest, and add output material
				else if (currentProductionTimer == currentRecipe.getProductionTime())
				{
					if (InventoryMethods.removeItemStacks(getInventory(), currentRecipe.getInput()))
					{
						for (int i = 1; i <= currentRecipe.getBatchAmount(); i++)
						{
							ItemStack itemStack = currentRecipe.getOutput();
							if (currentRecipe.getEnchantments() != null)
							{
								itemStack.addEnchantments(currentRecipe.getEnchantments());
							}
							getInventory().addItem(itemStack);
						}
						currentProductionTimer = 0;
						powerOff();
					}
				}
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

	/**
	 * Returns either a succsess or error message.
	 * Called by the blockListener when a player left clicks the powerSourceLocation with the InteractionMaterial
	 */
	public InteractionResponse togglePower() 
	{
		//if the factory is turned off
		if (!active)
		{
			//is there fuel enough for at least once energy cycle?
			if (isFuelAvailable())
			{
				//are there enough materials for the current recipe in the chest?
				if (InventoryMethods.areItemStacksAvilable(getInventory(), currentRecipe.getInput()))
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
					return new InteractionResponse(InteractionResult.FAILURE, "Factory does not have enough materials for the current recipe! You need: " + InventoryMethods.getMaterialsNeededMessage(currentRecipe.getInput()));
				}
			}
			//if there isn't enough fuel for atleast on energy cycle
			else
			{
				//return a error message
				return new InteractionResponse(InteractionResult.FAILURE, "Factory is missing fuel!");
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
	 * Returns either a succsess or error message.
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
				if (currentRecipeNumber == productionFactoryProperties.getRecipes().size() - 1)
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
	 * Sets the factorys current recipe.
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
		currentRecipe = productionFactoryProperties.getRecipes().get(newRecipeNumber);
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
	 * Checks if there is enough fuel avilable for atleast once energy cycle
	 * @return true if there is enough fuel, false otherwise
	 */
	public boolean isFuelAvailable()
	{
		return InventoryMethods.isItemStackAvailable(getPowerSourceInventory(), productionFactoryProperties.getEnergyMaterial());
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
		if (FactoryModPlugin.RETURN_BUILD_MATERIALS)
		{
			for (int i = 1; i <= productionFactoryProperties.getBuildMaterials().size(); i++)
			{
				ItemStack item = productionFactoryProperties.getBuildMaterials().get(i);
				factoryLocation.getWorld().dropItemNaturally(destroyLocation, item);
			}
		}
		
		destroyLocation.getBlock().setType(Material.AIR);
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
}
