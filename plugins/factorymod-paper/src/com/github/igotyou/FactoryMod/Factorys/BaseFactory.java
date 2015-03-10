package com.github.igotyou.FactoryMod.Factorys;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.FactoryObject;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.recipes.ProbabilisticEnchantment;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.github.igotyou.FactoryMod.utility.StringUtils;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public abstract class BaseFactory extends FactoryObject implements Factory {
	public static final BlockFace[] REDSTONE_FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

	protected int currentProductionTimer = 0;//The "production timer", which trachs for how long the factory has been producing the selected recipe
	protected int currentEnergyTimer = 0;//Time since last energy consumption(if there's no lag, it's in seconds)
	protected double currentRepair;
	protected long timeDisrepair;//The time at which the factory went into disrepair

	private Logger log = Logger.getLogger(BaseFactory.class.getName());

	public BaseFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, FactoryType factoryType, String subFactoryType) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, active,
				factoryType, subFactoryType);
		this.currentRepair = 0.0;
		this.timeDisrepair=3155692597470L;
	}

	public BaseFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, int tierLevel, FactoryType factoryType,
			Inventory factoryInventory, String subFactoryType) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, active,
				tierLevel, factoryType, factoryInventory, subFactoryType);
		this.currentRepair = 0.0;
		this.timeDisrepair=3155692597470L;
	}

	public BaseFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			FactoryType factoryType, String subFactoryType) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource,
				factoryType, subFactoryType);
		this.currentRepair=0.0;
		this.timeDisrepair=3155692597470L;//Year 2070, default starting value
	}
	
	public BaseFactory(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			FactoryType factoryType, boolean active, String subFactoryType,
			int currentProductionTimer, int currentEnergyTimer,
			double currentMaintenance, long timeDisrepair) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource,
				factoryType, subFactoryType);
		this.active = active;
		this.currentEnergyTimer = currentEnergyTimer;
		this.currentProductionTimer = currentProductionTimer;
		this.currentRepair=currentMaintenance;
		this.timeDisrepair=timeDisrepair;
	}

	private void setActivationLever(boolean state) {
		Block lever = findActivationLever();
		if (lever != null) {
			setLever(lever, state);
			shotGunUpdate(factoryPowerSourceLocation.getBlock());
		}
	}

	/**
	 * Turns the factory off.
	 */
	public void powerOff() 
	{
		if(active)
		{
			if (FactoryModPlugin.LEVER_OUTPUT_ENABLED) {
				setActivationLever(false);
			}
			
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
	
	public boolean checkHasMaterials() {
		return getAllInputs().allIn(getInventory());
	}

	/**
	 * Turns the factory on
	 */
	public void powerOn() 
	{
		//put active to true
		active = true;
		
		// Set attached lever
		if (FactoryModPlugin.LEVER_OUTPUT_ENABLED) {
			setActivationLever(true);
		}
		
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
		//reset the production timer
		currentProductionTimer = 0;
	}

	/**
	 * Returns either a success or error message.
	 * Called by the blockListener when a player left clicks the powerSourceLocation with the InteractionMaterial
	 */
	public List<InteractionResponse> togglePower() 
	{
		List<InteractionResponse> response=new ArrayList<InteractionResponse>();
		//if the factory is turned off
		if (!active)
		{
			//if the factory isn't broken or the current recipe can repair it
			if(!isBroken()||isRepairing())
			{
				//is there fuel enough for at least once energy cycle?
				if (isFuelAvailable())
				{
					//are there enough materials for the current recipe in the chest?
					if (checkHasMaterials())
					{
						//turn the factory on
						powerOn();
						//return a success message
						response.add(new InteractionResponse(InteractionResult.SUCCESS, "Factory activated!"));
						return response;
					}
					//there are not enough materials for the recipe!
					else
					{
						//return a failure message, containing which materials are needed for the recipe
						//[Requires the following: Amount Name, Amount Name.]
						//[Requires one of the following: Amount Name, Amount Name.]
						
						ItemList<NamedItemStack> needAll=new ItemList<NamedItemStack>();
						ItemList<NamedItemStack> allInputs = getAllInputs();
						needAll.addAll(allInputs.getDifference(getInventory()));
						if(!needAll.isEmpty())
						{
							response.add(new InteractionResponse(InteractionResult.FAILURE,"You need all of the following: "+needAll.toString()+"."));
						} else if (allInputs == null || allInputs.isEmpty()) {
							log.warning("getAllInputs() returned null or empty; recipe is returning no expectation of input!");
						}
						return response;
					}
				}
				//if there isn't enough fuel for at least one energy cycle
				else
				{
					//return a error message
					int multiplesRequired=(int)Math.ceil(getProductionTime()/(double)getEnergyTime());
					response.add(new InteractionResponse(InteractionResult.FAILURE, "Factory is missing fuel! ("+getFuel().getMultiple(multiplesRequired).toString()+")"));
					return response;
				}
			}
			else
			{
				response.add(new InteractionResponse(InteractionResult.FAILURE, "Factory is in disrepair!"));
				return response;
			}			
		}
		//if the factory is on already
		else
		{
			//turn the factory off
			powerOff();
			//return success message
			response.add(new InteractionResponse(InteractionResult.FAILURE, "Factory has been deactivated!"));
			return response;
		}
	}


	public abstract ItemList<NamedItemStack> getFuel();
	public abstract double getEnergyTime();
	public abstract double getProductionTime();
	public abstract ItemList<NamedItemStack> getInputs();
	public abstract ItemList<NamedItemStack> getOutputs();
	public abstract ItemList<NamedItemStack> getRepairs();
	
	public void consumeInputs() {
		//Remove inputs from chest
		getInputs().removeFrom(getInventory());
	}
	
	public void produceOutputs() {
		//Adds outputs to chest with appropriate enchantments
		getOutputs().putIn(getInventory(),getEnchantments());
	}

	public ItemList<NamedItemStack> getAllInputs() {
		ItemList<NamedItemStack> allInputs = new ItemList<NamedItemStack>();
		allInputs.addAll(getInputs());
		allInputs.addAll(getRepairs());
		return allInputs;
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
			if (checkHasMaterials())
			{
				//if the factory has been working for less than the required time for the recipe
				if (currentProductionTimer < getProductionTime())
				{
					//if the factory power source inventory has enough fuel for at least 1 energyCycle
					if (isFuelAvailable())
					{
						//if the time since fuel was last consumed is equal to how often fuel needs to be consumed
						if (currentEnergyTimer == getEnergyTime()-1)
						{
							//remove one fuel.
							getFuel().removeFrom(getPowerSourceInventory());
							//0 seconds since last fuel consumption
							currentEnergyTimer = 0;
							fuelConsumed();
						}
						//if we don't need to consume fuel, just increment the energy timer
						else
						{
							currentEnergyTimer++;
						}
						//increment the production timer
						currentProductionTimer ++;
						postUpdate();
					}
					//if there is no fuel Available turn off the factory
					else
					{
						powerOff();
					}
				}
				
				//if the production timer has reached the recipes production time remove input from chest, and add output material
				else if (currentProductionTimer >= getProductionTime())
				{
					consumeInputs();
					produceOutputs();
					//Repairs the factory
					repair(getRepairs().removeMaxFrom(getInventory(),(int)currentRepair));
					recipeFinished();
					
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

	protected void postUpdate() {
		// Hook for subtypes
	}

	protected void fuelConsumed() {
		// Hook for subtypes
	}

	public List<ProbabilisticEnchantment> getEnchantments() {
		return new ArrayList<ProbabilisticEnchantment>();
	}

	protected abstract void recipeFinished();

	/**
	 * returns the date at which the factory went into disrepair
	 */
	public long getTimeDisrepair()
	{
		return timeDisrepair;
	}
	
	public Block findActivationLever() {
    	Block block = getPowerSourceLocation().getBlock();
    	return findAttachedLever(block);
	}
    
    public Block findAttachedLever(Block block) {
		// Check sides for attached lever - required for automation
		Block lever = null;
		for (BlockFace face : REDSTONE_FACES) {
			lever = block.getRelative(face);
			if (lever.getType() == Material.LEVER) {
			    BlockFace attached = getAttachedFace(lever);
			    if (attached != null && attached == face) {
					return lever;
			    }
			}
		}
		
		return null;
    }
    
    private static BlockFace getAttachedFace(Block lever) {
    	BlockState state = lever.getState();
    	MaterialData md = state.getData();
    	if (md instanceof Attachable) {
    		BlockFace face = ((Attachable) md).getAttachedFace();
    		return face.getOppositeFace();
    	} else {
    		return null;
    	}
    }
    
    private void shotGunUpdate(Block block) {
    	for (BlockFace direction : REDSTONE_FACES) {
    		Block neighbour = block.getRelative(direction);
    		
    	}
    }
    
    
	/**
	* Sets the toggled state of a single lever<br>
	* <b>No Lever type check is performed</b>
	*
	* @param lever block
	* @param down state to set to
	*/
	private static void setLever(org.bukkit.block.Block lever, boolean down) {
		if (lever.getType() != Material.LEVER) {
			return;
		}
		
		byte data = lever.getData();
		int newData;
		if (down) {
			newData = data | 0x8;
		} else {
			newData = data & 0x7;
		}
		if (newData != data) {
			// CraftBukkit start - Redstone event for lever
			int old = !down ? 1 : 0;
			int current = down ? 1 : 0;
			BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(lever, old, current);
			Bukkit.getServer().getPluginManager().callEvent(eventRedstone);
			if ((eventRedstone.getNewCurrent() > 0) != down) {
				return;
			}
			// CraftBukkit end
			lever.setData((byte) newData, true);
			lever.getState().update();
			Block attached = lever.getRelative(((Attachable) lever.getState().getData()).getAttachedFace());
		}
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
		Inventory inv = getPowerSourceInventory();
		if (inv == null) {
			return false;
		} else {
			return getFuel().allIn(inv);
		}
	}

	/**
	 * Called by the block listener when the player(or a entity) destroys the factory
	 * Drops the build materials if the config says it should
	 */
	public void destroy(Location destroyLocation)
	{
		FactoryModPlugin.sendConsoleMessage(new StringBuilder("Factory destroyed: ")
			.append(subFactoryType)
			.append(" at ")
			.append(StringUtils.formatCoords(getCenterLocation()))
			.toString());
	
		powerOff();
		currentRepair = getMaxRepair();
		timeDisrepair = System.currentTimeMillis();
	}
	/*
	 * Repairs the factory 
	 */
	protected void repair(int amountRepaired)
	{
		currentRepair-=amountRepaired;
		if(currentRepair<0)
		{
			currentRepair=0;
		}
		if(currentRepair<getMaxRepair())
		{
			timeDisrepair=3155692597470L;//Year 2070, default starting value
		}
	}

	/**
	 * Degrades the factory
	 */
	public void updateRepair(double percent)
	{
		int totalRepair=getMaxRepair();
		currentRepair+=totalRepair*percent;
		if(currentRepair>=totalRepair)
		{
			currentRepair=totalRepair;
			long currentTime=System.currentTimeMillis();
			if (currentTime<timeDisrepair&&getMaxRepair()!=0)
			{
				timeDisrepair=currentTime;
			}	
		}
	}
	
	public abstract int getMaxRepair();

	public double getCurrentRepair()
	{
		return currentRepair;
	}
	/**
	 * Checks that a factory hasn't degraded too much
	 */
	public boolean isBroken()
	{
		return currentRepair>=getMaxRepair()&&getMaxRepair()!=0;
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

	public boolean isRepairing() {
		return false;
	}
	
	public List<InteractionResponse> getChestResponse() {
		return new ArrayList<InteractionResponse>();
	}
	
	public List<InteractionResponse> getCentralBlockResponse() {
		return new ArrayList<InteractionResponse>();
	}
}
