package com.github.igotyou.FactoryMod.listeners;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.NetherFactory;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.managers.FactoryModManager;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.StringUtils;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public class FactoryModListener implements Listener
{
	private FactoryModManager factoryMan;
	private ReinforcementManager rm = Citadel.getReinforcementManager();
	
	/**
	 * Constructor
	 */
	public FactoryModListener(FactoryModManager factoryManager)
	{
		this.factoryMan = factoryManager;
	}
	
	private boolean isPotentialFactoryBlock(Block block) {
		return block.getType() == FactoryModPlugin.CENTRAL_BLOCK_MATERIAL || block.getType() == Material.IRON_BLOCK || block.getType() == Material.CHEST||
				block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE;
	}
	
	/**
	 * Called when a block is broken
	 * If the block that is destroyed is part of a factory, call the required methods.
	 */
	@EventHandler
	public void blockBreakEvent(BlockBreakEvent e)
	{
		Block block = e.getBlock();
		//Is the block part of a factory?
		if(isPotentialFactoryBlock(block))
		{
			if (factoryMan.factoryExistsAt(block.getLocation()))
			{
				//if the blocks is not reinforced destroy it
				if ((FactoryModPlugin.CITADEL_ENABLED && !rm.isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)
				{
					if(e.getPlayer() == null) {
						FactoryModPlugin.sendConsoleMessage(new StringBuilder("Factory block broken: ")
							.append(e.getBlock().getType())
							.append(" at ")
							.append(StringUtils.formatCoords(block.getLocation()))
							.toString());
					} 
					else
					{
						FactoryModPlugin.sendConsoleMessage(new StringBuilder("Factory block broken: ")
							.append(e.getBlock().getType())
							.append(" by ")
							.append(e.getPlayer().getUniqueId())
							.append(" at ")
							.append(StringUtils.formatCoords(block.getLocation()))
							.toString());
					}
					destroyFactoryAt(block);
				}
			}
		}
	}
	
	private void destroyFactoryAt(Block block) {
		//Is the factory a production factory?
		if (factoryMan.factoryExistsAt(block.getLocation()))
		{
			Factory factory = factoryMan.getFactory(block.getLocation());
			factory.destroy(block.getLocation());
			if(FactoryModPlugin.DESTRUCTIBLE_FACTORIES)
			{
				factoryMan.getManager(block.getLocation()).removeFactory(factory);
			}
		}
	}
	
	/**
	 * Called when a entity explodes(creeper,tnt etc.)
	 * Nearly the same as blockBreakEvent
	 */
	@EventHandler
	public void explosionListener(EntityExplodeEvent e)
	{
		List<Block> blocks = e.blockList();
		for (Block block : blocks)
		{
			if(isPotentialFactoryBlock(block))
			{
				if (factoryMan.factoryExistsAt(block.getLocation()))
				{
					if ((FactoryModPlugin.CITADEL_ENABLED && !rm.isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)
					{
						FactoryModPlugin.sendConsoleMessage(new StringBuilder("Factory block exploded: ")
							.append(block.getType())
							.append(" by ")
							.append(e.getEntityType())
							.append(" at ")
							.append(StringUtils.formatCoords(block.getLocation()))
							.toString());
						destroyFactoryAt(block);
					}
				}
			}
		}
	}
	
	/**
	 * Called when a block burns
	 * Nearly the same as blockBreakEvent
	 */
	@EventHandler
	public void burnListener(BlockBurnEvent e)
	{
		Block block = e.getBlock();
		if (factoryMan.factoryExistsAt(block.getLocation()))
		{
			if (factoryMan.factoryExistsAt(block.getLocation()))
			{
				FactoryModPlugin.sendConsoleMessage(new StringBuilder("Factory block burned: ")
					.append(block.getType())
					.append(" at ")
					.append(StringUtils.formatCoords(block.getLocation()))
					.toString());
				destroyFactoryAt(block);
			}
		}
	}
	
	/**
	 * Called when a player left or right clicks.
	 * Takes care of cycling recipes turning factory's on and off, etc.
	 */
	@EventHandler
	public void playerInteractionEvent(PlayerInteractEvent e)
	{
		Block clicked = e.getClickedBlock();
		Player player = e.getPlayer();
		
		//if the player left clicked a block
		if (e.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			//If the player was holding a item matching the interaction material
			if (player.getItemInHand().getType() == FactoryModPlugin.FACTORY_INTERACTION_MATERIAL)
			{
				//If the material which was clicked is the central block material
				if (clicked.getType() == FactoryModPlugin.CENTRAL_BLOCK_MATERIAL)
				{
					//is there a factory at the clicked location?
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						//if the factory has all its blocks
						if(factoryMan.factoryWholeAt(clicked.getLocation()))
						{
							//if the player is allowed to interact with that block.
							if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !rm.isReinforced(clicked)) || 
									(((PlayerReinforcement) rm.getReinforcement(clicked)).isAccessible(player)))
							{
								//if there is a production Factory at the clicked location
								if (factoryMan.factoryExistsAt(clicked.getLocation()))
								{
									Factory factory = factoryMan.getFactory(clicked.getLocation());
									//toggle the recipe, and print the returned message.
									InteractionResponse.messagePlayerResults(player, factory.getCentralBlockResponse());
								}
							}
							//if the player does NOT have acssess to the block that was clicked
							else
							{
								//return a error message
								InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use this factory!" ));
							}
						}
						else
						{
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"Factory blocks are misplaced!" ));
						}
					}
					//if no factory exists at the clicked location
					else
					{
						//if the player is allowed to interact with that block.
						if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !rm.isReinforced(clicked)) || 
								(((PlayerReinforcement) rm.getReinforcement(clicked)).isAccessible(player)))
						{
							InteractionResponse.messagePlayerResult(player, createFactory(clicked.getLocation(), player));
						}
						//if the player does NOT have acssess to the block that was clicked
						else
						{
							//return a error message
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use this factory!" ));
						}
					}
				}
				//if the clicked block is a furnace
				else if (clicked.getType() == Material.FURNACE || clicked.getType() == Material.BURNING_FURNACE)
				{
					//if there is a factory at that location and it has all its blocks
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						if(factoryMan.factoryWholeAt(clicked.getLocation()))
						{
							//if the player is allowed to interact with that block.
							if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !rm.isReinforced(clicked)) || 
									(((PlayerReinforcement) rm.getReinforcement(clicked)).isAccessible(player)))
							{
								InteractionResponse.messagePlayerResults(player,(factoryMan.getFactory(clicked.getLocation())).togglePower());
							}
							//if the player is NOT allowed to interact with the clicked block.
							else
							{
								//return error message
								InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use this factory!" ));
							}
						}
						else
						{
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"Factory blocks are misplaced!" ));
						}
					}
				}
				//if the block clicked is a chest
				else if (clicked.getType() == Material.CHEST)
				{
					//is there a factory there? and if it has all its blocks
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						if(factoryMan.factoryWholeAt(clicked.getLocation()))
						{
							//if the player is allowed to interact with that block?
							if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !rm.isReinforced(clicked)) || 
									(((PlayerReinforcement) rm.getReinforcement(clicked)).isAccessible(player)))
							{
								if (factoryMan.factoryExistsAt(clicked.getLocation()))
								{
									InteractionResponse.messagePlayerResults(player,(factoryMan.getFactory(clicked.getLocation())).getChestResponse());
								}
							}
							//if the player is NOT allowed to interact with the clicked block
							else
							{
								//return error message
								InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use this factory!" ));
							}
						}
						else
						{
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"Factory blocks are misplaced!" ));
						}
						
					}
				}
				else if (clicked.getType() == FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL)
				{
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						if(factoryMan.getFactory(clicked.getLocation()).getClass() == NetherFactory.class)
						{
							NetherFactory netherFactory = (NetherFactory) factoryMan.getFactory(clicked.getLocation());
							if (FactoryModPlugin.REGENERATE_TELEPORT_BLOCK_ON_TELEPORT)
							{
								netherFactory.regenerateTeleportBlock(clicked.getLocation());
							}
							if(factoryMan.factoryWholeAt(clicked.getLocation()))
							{						
								//toggle the recipe, and print the returned message.
								InteractionResponse.messagePlayerResults(player, netherFactory.getTeleportationBlockResponse(player, clicked.getLocation()));
								e.setCancelled(true);
							}
						}
					}
				}
			}
			else if (player.getItemInHand().getType() == Material.PAPER)
			{
				if (clicked.getType() == FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL)
				{
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						if(factoryMan.getFactory(clicked.getLocation()).getClass() == NetherFactory.class)
						{
							NetherFactory netherFactory = (NetherFactory) factoryMan.getFactory(clicked.getLocation());
							if (FactoryModPlugin.REGENERATE_TELEPORT_BLOCK_ON_TELEPORT)
							{
								netherFactory.regenerateTeleportBlock(clicked.getLocation());
							}
							if(factoryMan.factoryWholeAt(clicked.getLocation()))
							{						
								//toggle the recipe, and print the returned message.
								InteractionResponse.messagePlayerResults(player, netherFactory.getTeleportationBlockResponse(player, clicked.getLocation()));
								e.setCancelled(true);
							}
						}
					}
				}
			}
		}
		/* Section commented out since there exists range of bugs that circumvent 
		 * this protection and this protection should not be necessary
		 * it will only complicate and obfuscate possible workaround bugs
		//if the player right clicked a block
		else if(e.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			//if the player right clicked a chest
			if (clicked.getType() == Material.CHEST)
			{
				//is the chest part of a factory? and does the factory have all its blocks
				if (factoryMan.factoryExistsAt(clicked.getLocation())&&factoryMan.factoryWholeAt(clicked.getLocation()))
				{
					//if the player is allowed to interact with that block.
					if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || 
							(((PlayerReinforcement) getReinforcement(clicked)).isAccessible(player)))
					{
						if (productionMan.factoryExistsAt(clicked.getLocation()))
						{
							ProductionFactory production = (ProductionFactory) productionMan.getFactory(clicked.getLocation());
							//is the factory turned on?
							if (production.getActive() == true)
							{
								//return error message
								InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You can't access the chest while the factory is active! Turn it off first!" ));
								e.setCancelled(true);
							}
						}
					}
					//if the player is NOT allowed to interact with the block
					else
					{
						//No need to get 2 messages, citadel already does 1. e InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use that block!" ));
					}
				}
			}
		}
		*/
	}
	private Location westLoc(Location loc)
	{
		Location newLoc = loc.clone();
		newLoc.add(-1, 0, 0);
		return newLoc;
	}
	private Location eastLoc(Location loc)
	{
		Location newLoc = loc.clone();
		newLoc.add(1, 0, 0);
		return newLoc;
	}
	private Location northLoc(Location loc)
	{
		Location newLoc = loc.clone();
		newLoc.add(0, 0, -1);
		return newLoc;
	}
	private Location southLoc(Location loc)
	{
		Location newLoc = loc.clone();
		newLoc.add(0, 0, 1);
		return newLoc;
	}
	
	private InteractionResponse createFactory(Location loc, Player player)
	{
		Location northLocation = northLoc(loc);
		Location southLocation = southLoc(loc);
		Location eastLocation = eastLoc(loc);
		Location westLocation = westLoc(loc);
		 
		Block northBlock = northLocation.getBlock();
		Block southBlock = southLocation.getBlock();
		Block eastBlock = eastLocation.getBlock();
		Block westBlock = westLocation.getBlock();
		 
		Material northType = northBlock.getType();
		Material southType = southBlock.getType();
		Material eastType = eastBlock.getType();
		Material westType = westBlock.getType();
 
		//For each two orientations check if a factory exists at any of the locations
		//For each of the four permutations check if the correct blocks are present
		//This still allows a double chest to be shared between two factories, which may lead to undesirable behavior
		InteractionResponse response=new InteractionResponse(InteractionResult.FAILURE, "Blocks are not arranged correctly for a factory.");
		if(! factoryMan.factoryExistsAt(westLocation) && ! factoryMan.factoryExistsAt(eastLocation))
		{
			if((westType == Material.FURNACE || westType == Material.BURNING_FURNACE) && eastType == Material.CHEST)
			{
				return createFactory(player, loc, eastLocation, westLocation);
			}
			else if ((eastType == Material.FURNACE || eastType == Material.BURNING_FURNACE) && westType == Material.CHEST)
			{
				return createFactory(player, loc, westLocation, eastLocation);
			}
		}
		else
		{
			response=new InteractionResponse(InteractionResult.FAILURE, "There is already a factory there!");
		}
		if(! factoryMan.factoryExistsAt(southLocation) && !factoryMan.factoryExistsAt(northLocation))
		{
			
			if((northType == Material.FURNACE || northType == Material.BURNING_FURNACE) && southType == Material.CHEST)
			{
				return createFactory(player, loc, southLocation, northLocation);
			}
			else if((southType == Material.FURNACE || southType == Material.BURNING_FURNACE) && northType == Material.CHEST)
			{
				return createFactory(player, loc, northLocation, southLocation);
			}
		}
		else
		{
			response=new InteractionResponse(InteractionResult.FAILURE, "There is already a factory there!");
		}
		return response;
	 }
	
	private InteractionResponse createFactory(Player player, Location center, Location inventory, Location power) {

		if(rm.isReinforced(center)) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("Factory creation attempted: ")
				.append(player.getUniqueId())
				.append(" with group ")
				.append(((PlayerReinforcement)rm.getReinforcement(center)).getGroup().getName())
				.append(" at ")
				.append(StringUtils.formatCoords(center))
				.toString());
		} else {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("Factory creation attempted: ")
				.append(player.getUniqueId())
				.append(" at ")
				.append(StringUtils.formatCoords(center))
				.toString());
		}
		return factoryMan.createFactory(center, inventory, power);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void handlePortalTelportEvent(PlayerPortalEvent e) {
		if (e.isCancelled()) {
			return;
		}
		
		// Disable normal nether portal teleportation
		if (e.getCause() == TeleportCause.NETHER_PORTAL && FactoryModPlugin.DISABLE_PORTALS) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void playerTeleportEvent(PlayerTeleportEvent e) {
		if (e.isCancelled() || e.getCause() != TeleportCause.NETHER_PORTAL) {
			return;
		}
		
		// Disable normal nether portal teleportation
		if (FactoryModPlugin.DISABLE_PORTALS) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void entityTeleportEvent(EntityPortalEvent event){
		if (FactoryModPlugin.DISABLE_PORTALS) {
			event.setCancelled(true);
		}
	}
}
