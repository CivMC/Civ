package com.github.igotyou.FactoryMod.Factorys;

import static com.untamedears.citadel.Utility.getReinforcement;
import static com.untamedears.citadel.Utility.isReinforced;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.managers.NetherFactoryManager;
import com.github.igotyou.FactoryMod.properties.NetherFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.untamedears.citadel.entity.PlayerReinforcement;

import java.util.ArrayList;
import java.util.List;

public class NetherFactory extends BaseFactory
{

	private NetherFactoryProperties netherFactoryProperties;//the properties of the production factory
	private Location netherTeleportPlatform;
	private Location overworldTeleportPlatform;
	private NetherOperationMode mode;
	private NetherFactoryManager netherManager;
	public NetherOperationMode getMode() {
		return mode;
	}
	
	/**
	 * Constructor called when creating portal
	 */
	public NetherFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource, Location nTeleportPlatform, Location oTeleportPlatform,
			NetherFactoryProperties netherFactoryProperties, NetherFactoryManager netherManager)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, FactoryType.NETHER_FACTORY, "Nether factory");
		this.netherTeleportPlatform = nTeleportPlatform;
		this.overworldTeleportPlatform = oTeleportPlatform;
		this.netherFactoryProperties = netherFactoryProperties;
		this.mode = NetherOperationMode.REPAIR;
		this.netherManager = netherManager;
	}

	/**
	 * Constructor
	 */
	public NetherFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource, Location nTeleportPlatform, Location oTeleportPlatform,
			boolean active, double currentMaintenance,
			long timeDisrepair, NetherOperationMode mode, NetherFactoryProperties netherFactoryProperties,
			NetherFactoryManager netherManager)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, FactoryType.NETHER_FACTORY, active, "Nether factory", 0 , 0, currentMaintenance, timeDisrepair);
		this.netherFactoryProperties = netherFactoryProperties;
		this.netherTeleportPlatform = nTeleportPlatform;
		this.overworldTeleportPlatform = oTeleportPlatform;
		this.mode = mode;
		this.netherManager = netherManager;
	}
		
	@Override
	public boolean isRepairing() {
		return mode == NetherOperationMode.REPAIR;
	}
	
	@Override
	public void destroy(Location destroyLocation)
	{
		if (destroyLocation.equals(overworldTeleportPlatform) || destroyLocation.equals(netherTeleportPlatform))
		{
			powerOff();
		}
		else if (destroyLocation.equals(factoryLocation) || destroyLocation.equals(factoryInventoryLocation) || destroyLocation.equals(factoryPowerSourceLocation))
		{
			powerOff();
			currentRepair=getMaxRepair();
			timeDisrepair=System.currentTimeMillis();	
		}
	}
		
	/**
	 * Returns either a success or error message.
	 * Called by the blockListener when a player left clicks the center block, with the InteractionMaterial
	 */
	@Override
	public void update()
	{
		if (mode == NetherOperationMode.REPAIR)
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
		else if (mode == NetherOperationMode.TELEPORT)
		{
			if(!isFuelAvailable())
			{
				togglePower();
			}
		}
	}
	
	public List<InteractionResponse> getTeleportationBlockResponse(Player player, Location clickedBlock)
	{
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		//does the player have acsess to the nether factory via ciatdel?
		if ((!FactoryModPlugin.CITADEL_ENABLED || (FactoryModPlugin.CITADEL_ENABLED && !isReinforced(factoryLocation))) || 
				(((PlayerReinforcement) getReinforcement(factoryLocation)).isAccessible(player)))
		{
			if (mode == NetherOperationMode.TELEPORT)
			{
				if (active)
				{
					if (isFuelAvailable() || !netherFactoryProperties.getUseFuelOnTeleport())
					{
						Location playerLocation = player.getLocation();
						if (		playerLocation.getBlockX()	 	== clickedBlock.getBlockX()
								&& (playerLocation.getBlockY()-1)	== clickedBlock.getBlockY()
								&& 	playerLocation.getBlockZ() 		== clickedBlock.getBlockZ())
						{
							responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Commencing teleportation..."));
							if (clickedBlock.getWorld().getName().equalsIgnoreCase(FactoryModPlugin.WORLD_NAME))
							{
								if (FactoryModPlugin.REMOVE_BLOCK_ABOVE_TELEPORT_PLATFORM_ON_TELEPORT)
								{
									removeBlocksAboveTeleportPlatform(netherTeleportPlatform);
								}
								Location destination = new Location(netherTeleportPlatform.getWorld(), netherTeleportPlatform.getX(), netherTeleportPlatform.getY(), netherTeleportPlatform.getZ(), playerLocation.getYaw(), playerLocation.getPitch());
								destination.add(0.5, 1.5, 0.5);								
								player.teleport(destination);
								if (netherFactoryProperties.getUseFuelOnTeleport())
								{
									getFuel().removeFrom(getPowerSourceInventory());
								}
							}
							else if (clickedBlock.getWorld().getName().equalsIgnoreCase(FactoryModPlugin.NETHER_NAME))
							{
								if (FactoryModPlugin.REMOVE_BLOCK_ABOVE_TELEPORT_PLATFORM_ON_TELEPORT)
								{
									removeBlocksAboveTeleportPlatform(overworldTeleportPlatform);
								}
								Location destination = new Location(overworldTeleportPlatform.getWorld(), overworldTeleportPlatform.getX(), overworldTeleportPlatform.getY(), overworldTeleportPlatform.getZ(), playerLocation.getYaw(), playerLocation.getPitch());
								destination.add(0.5, 1.5, 0.5);
								player.teleport(destination);
								if (netherFactoryProperties.getUseFuelOnTeleport())
								{
									getFuel().removeFrom(getPowerSourceInventory());
								}
							}
						}
						else
						{
							responses.add(new InteractionResponse(InteractionResult.FAILURE, "Can't teleport, you must stand on the teleportation block!"));
						}
					}
					else
					{
						responses.add(new InteractionResponse(InteractionResult.FAILURE, "Can't teleport, factory is missing fuel! ("+getFuel().getMultiple(1).toString()+")"));
					}
				}
				else
				{
					responses.add(new InteractionResponse(InteractionResult.FAILURE, "Can't teleport, factory is turned off!"));
				}
			}
			else
			{
				responses.add(new InteractionResponse(InteractionResult.FAILURE, "Can't teleport, factory is not in teleport mode."));
			}
			return responses;
		}
		else
		{
			//is the player potentialy holding a security note/ticket?
			ItemStack itemInHand = player.getItemInHand();
			if (itemInHand.getType() == Material.PAPER)
			{
				if (isInTicketMode())
				{
					int ticketCheck = checkTicket(itemInHand);
					if (ticketCheck > 0)
					{
						if (mode == NetherOperationMode.TELEPORT)
						{
							if (active)
							{
								if (isFuelAvailable())
								{
									Location playerLocation = player.getLocation();
									if (		playerLocation.getBlockX()	 	== clickedBlock.getBlockX()
											&& (playerLocation.getBlockY()-1)	== clickedBlock.getBlockY()
											&& 	playerLocation.getBlockZ() 		== clickedBlock.getBlockZ())
									{
										responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Commencing teleportation..."));
										if (clickedBlock.getWorld().getName().equalsIgnoreCase(FactoryModPlugin.WORLD_NAME))
										{
											if (FactoryModPlugin.REMOVE_BLOCK_ABOVE_TELEPORT_PLATFORM_ON_TELEPORT)
											{
												removeBlocksAboveTeleportPlatform(netherTeleportPlatform);
											}
											Location destination = new Location(netherTeleportPlatform.getWorld(), netherTeleportPlatform.getX(), netherTeleportPlatform.getY(), netherTeleportPlatform.getZ(), playerLocation.getYaw(), playerLocation.getPitch());
											destination.add(0.5, 1.5, 0.5);
											player.teleport(destination);
											if (ticketCheck == 2)
											{
												transferTicket(player, itemInHand);
											}
											if (netherFactoryProperties.getUseFuelOnTeleport())
											{
												getFuel().removeFrom(getPowerSourceInventory());
											}
										}
										else if (clickedBlock.getWorld().getName().equalsIgnoreCase(FactoryModPlugin.NETHER_NAME))
										{
											if (FactoryModPlugin.REMOVE_BLOCK_ABOVE_TELEPORT_PLATFORM_ON_TELEPORT)
											{
												removeBlocksAboveTeleportPlatform(overworldTeleportPlatform);
											}
											Location destination = new Location(overworldTeleportPlatform.getWorld(), overworldTeleportPlatform.getX(), overworldTeleportPlatform.getY(), overworldTeleportPlatform.getZ(), playerLocation.getYaw(), playerLocation.getPitch());
											destination.add(0.5, 1.5, 0.5);
											player.teleport(destination);
											if (ticketCheck == 2)
											{
												transferTicket(player, itemInHand);
											}
											if (netherFactoryProperties.getUseFuelOnTeleport())
											{
												getFuel().removeFrom(getPowerSourceInventory());
											}
										}
									}
									else
									{
										responses.add(new InteractionResponse(InteractionResult.FAILURE, "Can't teleport, you must stand on the teleportation block!"));
									}
								}
								else
								{
									responses.add(new InteractionResponse(InteractionResult.FAILURE, "Can't teleport, factory is missing fuel! ("+getFuel().getMultiple(1).toString()+")"));
								}
							}
							else
							{
								responses.add(new InteractionResponse(InteractionResult.FAILURE, "Can't teleport, factory is turned off!"));
							}
						}
						else
						{
							responses.add(new InteractionResponse(InteractionResult.FAILURE, "Can't teleport, factory is not in teleport mode."));
						}
					}
					else
					{
						responses.add(new InteractionResponse(InteractionResult.FAILURE, "Your ticket does not match any in the factory."));
					}
				}
				else
				{
					responses.add(new InteractionResponse(InteractionResult.FAILURE, "You don't have permission to use this factory."));
				}
			}
			else
			{
				responses.add(new InteractionResponse(InteractionResult.FAILURE, "You don't have permission to use this factory."));
			}
		}
		return responses;
	}
	
	@Override
	public List<InteractionResponse> getCentralBlockResponse()
	{
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		//Is the factory off
		if (!active)
		{
			//is the recipe is initiated
			if (mode == null) 
			{
				mode = NetherOperationMode.REPAIR;
			}
			else 
			{		
				mode = mode.getNext();
			}
				
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "-----------------------------------------------------"));
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Switched mode to: " + mode.getDescription()+"."));
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Next mode is: "+mode.getNext().getDescription()+"."));
		}
		else
		{
			responses.add(new InteractionResponse(InteractionResult.FAILURE, "You can't change modes while the nether factory is on! Turn it off first."));
		}
		return responses;
	}
	
	@Override
	public ItemList<NamedItemStack> getFuel() {
		return netherFactoryProperties.getFuel();
	}
	
	/**
	 * Returns the factory's properties
	 */
	public NetherFactoryProperties getProperties()
	{
		return netherFactoryProperties;
	}
	
	@Override
	public List<InteractionResponse> getChestResponse()
	{
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		String status=active ? "On" : "Off";
		//Name: Status with XX% health.
		int maxRepair = netherFactoryProperties.getRepair();
		boolean maintenanceActive = maxRepair!=0;
		int health =(!maintenanceActive) ? 100 : (int) Math.round(100*(1-currentRepair/(maxRepair)));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, netherFactoryProperties.getName()+": "+status+" with "+String.valueOf(health)+"% health."));
		//Current mode: mode description
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Current mode: " + mode.getDescription()));
		//Overworld side teleport platform is at X: Y: Z:
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Overworld side teleport platform is:" + overworldTeleportPlatform.getBlockX() + " Y:" + overworldTeleportPlatform.getBlockY() + " Z:" + overworldTeleportPlatform.getBlockZ()));
		//Nether side teleport platform is at X: Y: Z:
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Nether side teleport platform is:" + netherTeleportPlatform.getBlockX() + " Y:" + netherTeleportPlatform.getBlockY() + " Z:" + netherTeleportPlatform.getBlockZ()));
		//[Will repair XX% of the factory]
		if(!getRepairs().isEmpty()&&maintenanceActive)
		{
			int amountAvailable=getRepairs().amountAvailable(getInventory());
			int amountRepaired=amountAvailable>currentRepair ? (int) Math.ceil(currentRepair) : amountAvailable;
			int percentRepaired=(int) (( (double) amountRepaired)/netherFactoryProperties.getRepair()*100);
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Will repair "+String.valueOf(percentRepaired)+"% of the factory with "+getRepairs().getMultiple(amountRepaired).toString()+"."));
		}
		return responses;
	}
	
	protected void recipeFinished() {
	}

	@Override
	public ItemList<NamedItemStack> getInputs() {
		if(mode == NetherOperationMode.REPAIR)
		{
			return new ItemList<NamedItemStack>();
		}
		else
		{
			return new ItemList<NamedItemStack>();
		}
	}

	@Override
	public ItemList<NamedItemStack> getOutputs() {
		return new ItemList<NamedItemStack>();
	}

	@Override
	public ItemList<NamedItemStack> getRepairs() {
		ItemList<NamedItemStack> repairMaterials = new ItemList<NamedItemStack>();
		switch(mode) {
		case REPAIR:
			repairMaterials.addAll(netherFactoryProperties.getRepairMaterials());
			repairMaterials = repairMaterials.getMultiple(netherManager.getScalingFactor(factoryLocation));
			break;
		default:
			break;
		}
		return repairMaterials;
	}

	@Override
	public double getEnergyTime() {
		return netherFactoryProperties.getEnergyTime();
	}

	@Override
	public double getProductionTime() {
		switch(mode) {
		case REPAIR:
			return netherFactoryProperties.getRepairTime();
		default:
			return 1;
		}
	}

	@Override
	public int getMaxRepair() {
		return netherFactoryProperties.getRepair();
	}
	
	public Location getNetherTeleportPlatform()
	{
		return netherTeleportPlatform;
	}
	

	public Location getOverworldTeleportPlatform() 
	{
		return overworldTeleportPlatform;
	}
	
	@Override
	public boolean isWhole(boolean initCall)
	{
		//Check if power source exists
		if(factoryPowerSourceLocation.getBlock().getType().getId()== 61 || factoryPowerSourceLocation.getBlock().getType().getId()== 62)
		{
			//Check inventory location
			if(factoryInventoryLocation.getBlock().getType().getId()== 54) 	
			{
				//Check Interaction block location
				if(factoryLocation.getBlock().getType()==FactoryModPlugin.CENTRAL_BLOCK_MATERIAL)
				{
					if (netherTeleportPlatform == null && overworldTeleportPlatform == null && initCall)
					{
						return true;
					}
					else
					{
						if (netherTeleportPlatform.getBlock().getType() == FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL)
						{
							if (overworldTeleportPlatform.getBlock().getType() == FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL)
							{
								return true;
							}
						}	
					}
				}
			}
		}
	return false;
	}
	
	public boolean isInTicketMode()
	{
		for (ItemStack itemSlot : getInventory().getContents())
		{
			if (itemSlot != null && itemSlot.getType() == Material.PAPER)
			{
				return true;
			}
		}
		return false;
	}
	
	public int checkTicket(ItemStack ticket)
	{
		int amount = 0;
		for(ItemStack itemStack: getInventory().getContents())
		{
			if (itemStack == null)
			{
				continue;
			}
			if (itemStack.isSimilar(ticket))
			{
				amount = amount+itemStack.getAmount();
			}
		}
		if (amount == 1)
		{
			return 1;
		}
		else if (amount >= 2)
		{
			return 2;
		}
		else
		{
			return 0;
		}
	}
	
	public void removeBlocksAboveTeleportPlatform(Location teleportPlatform)
	{
		Location netherLocation1 = teleportPlatform.clone();
		netherLocation1.add(0, 1, 0);
		Location netherLocation2 = teleportPlatform.clone();
		netherLocation2.add(0, 2, 0);
		Location netherLocation3 = teleportPlatform.clone();
		netherLocation3.add(0, 3, 0);
		netherLocation1.getBlock().setType(Material.AIR);
		netherLocation1.getBlock().getState().update(true);
		netherLocation2.getBlock().setType(Material.AIR);
		netherLocation2.getBlock().getState().update(true);
		netherLocation3.getBlock().setType(Material.AIR);
		netherLocation3.getBlock().getState().update(true);
	}
	
	public void regenerateTeleportBlock(Location location)
	{
		if (location.equals(overworldTeleportPlatform))
		{
			netherTeleportPlatform.getBlock().setType(FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL);
			netherTeleportPlatform.getBlock().getState().update(true);
		}
		else if(location.equals(netherTeleportPlatform))
		{
			overworldTeleportPlatform.getBlock().setType(FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL);
			overworldTeleportPlatform.getBlock().getState().update(true);
		}
		
	}
	
	public void transferTicket(Player player, ItemStack ticket)
	{
		ItemStack clonedTicket = ticket.clone();
		clonedTicket.setAmount(1);
		ticket.setAmount(ticket.getAmount()-1);
		player.setItemInHand(ticket);
		getInventory().addItem(clonedTicket);
	}
	
	public enum NetherOperationMode {
		REPAIR(0, "Repair"),
		TELEPORT(1, "Teleport");
		
		private static final int MAX_ID = 2;
		private int id;
		private String description;

		private NetherOperationMode(int id, String description) {
			this.id = id;
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}

		public static NetherOperationMode byId(int id) {
			for (NetherOperationMode mode : NetherOperationMode.values()) {
				if (mode.getId() == id)
					return mode;
			}
			return null;
		}
		
		public int getId() {
			return id;
		}

		public NetherOperationMode getNext() {
			int nextId = (getId() + 1) % MAX_ID;
			return NetherOperationMode.byId(nextId);
		}
	}
}
