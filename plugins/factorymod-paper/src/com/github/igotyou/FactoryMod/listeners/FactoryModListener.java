package com.github.igotyou.FactoryMod.listeners;

import static com.untamedears.citadel.Utility.isReinforced;
import static com.untamedears.citadel.Utility.getReinforcement;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.ProductionFactory;
import com.github.igotyou.FactoryMod.managers.FactoryModManager;
import com.github.igotyou.FactoryMod.managers.ProductionManager;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.untamedears.citadel.entity.PlayerReinforcement;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class FactoryModListener implements Listener
{
	private FactoryModManager factoryMan;
	//this is a lazy fix...
	private ProductionManager productionMan;
	
	/**
	 * Constructor
	 */
	public FactoryModListener(FactoryModManager factoryManager, ProductionManager productionManager)
	{
		this.factoryMan = factoryManager;
		this.productionMan = productionManager;
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
		if(block.getType() == FactoryModPlugin.CENTRAL_BLOCK_MATERIAL || block.getType() == Material.CHEST||
			block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE)
		{
			if (factoryMan.factoryExistsAt(block.getLocation()))
			{
				//Is the factory a production factory?
				if (productionMan.factoryExistsAt(block.getLocation()))
				{
					ProductionFactory factory = (ProductionFactory) productionMan.getFactory(block.getLocation());
					e.setCancelled(true);

					//if the blocks is not reinforced destroy it
					if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)
					{
						factory.destroy(block.getLocation());
						if(FactoryModPlugin.DESTRUCTIBLE_FACTORIES)
						{
							productionMan.removeFactory(factory);
						}
					}
				}
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
			if(block.getType() == FactoryModPlugin.CENTRAL_BLOCK_MATERIAL || block.getType() == Material.CHEST||
			block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE)
			{
				if (factoryMan.factoryExistsAt(block.getLocation()))
				{
					if (productionMan.factoryExistsAt(block.getLocation()))
					{
						ProductionFactory factory = (ProductionFactory) productionMan.getFactory(block.getLocation());
						e.setCancelled(true);

						if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)
						{
							factory.destroy(block.getLocation());
							if(FactoryModPlugin.DESTRUCTIBLE_FACTORIES)
							{		
								productionMan.removeFactory(factory);
							}
						}
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
			if (productionMan.factoryExistsAt(block.getLocation()))
			{
				ProductionFactory factory = (ProductionFactory) productionMan.getFactory(block.getLocation());
				e.setCancelled(true);
				
				if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)
				{
					factory.destroy(block.getLocation());
					if(FactoryModPlugin.DESTRUCTIBLE_FACTORIES)
					{
						productionMan.removeFactory(factory);
					}
				}
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
							if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || 
									(((PlayerReinforcement) getReinforcement(clicked)).isAccessible(player)))
							{
								//if there is a production Factory at the clicked location
								if (productionMan.factoryExistsAt(clicked.getLocation()))
								{
									ProductionFactory production = (ProductionFactory) productionMan.getFactory(clicked.getLocation());
									//toggle the recipe, and print the returned message.
									InteractionResponse.messagePlayerResults(player, production.toggleRecipes());
								}
							}
							//if the player does NOT have acssess to the block that was clicked
							else
							{
								//return a error message
								InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use this factory!" ));
							}
						}
					}
					//if no factory exists at the clicked location
					else
					{
						//if the player is allowed to interact with that block.
						if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || 
								(((PlayerReinforcement) getReinforcement(clicked)).isAccessible(player)))
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
					if (factoryMan.factoryExistsAt(clicked.getLocation())&&factoryMan.factoryWholeAt(clicked.getLocation()))
					{
						//if the player is allowed to interact with that block.
						if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || 
								(((PlayerReinforcement) getReinforcement(clicked)).isAccessible(player)))
						{
							if (productionMan.factoryExistsAt(clicked.getLocation()))
							{
								InteractionResponse.messagePlayerResults(player,((ProductionFactory) productionMan.getFactory(clicked.getLocation())).togglePower());
							}
						}
						//if the player is NOT allowed to interact with the clicked block.
						else
						{
							//return error message
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use this factory!" ));
						}
					}
				}
				//if the block clicked is a chest
				else if (clicked.getType() == Material.CHEST)
				{
					//is there a factory there? and if it has all its blocks
					if (factoryMan.factoryExistsAt(clicked.getLocation())&&factoryMan.factoryWholeAt(clicked.getLocation()))
					{
						//if the player is allowed to interact with that block?
						if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || 
								(((PlayerReinforcement) getReinforcement(clicked)).isAccessible(player)))
						{
							if (productionMan.factoryExistsAt(clicked.getLocation()))
							{
								InteractionResponse.messagePlayerResults(player,((ProductionFactory) productionMan.getFactory(clicked.getLocation())).getChestResponse());
							}
						}
						//if the player is NOT allowed to interact with the clicked block
						else
						{
							//return error message
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use this factory!" ));
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
	@EventHandler
	public void onPlayerExpChangeEvent(PlayerExpChangeEvent j)
	{
		if(FactoryModPlugin.DISABLE_EXPERIENCE)
		{
			j.setAmount(0);			
		}
		return;
	}
	
	@EventHandler
	public void onExpBottleEvent(ExpBottleEvent j)
	{
		if(FactoryModPlugin.DISABLE_EXPERIENCE)
		{
			Player player=(Player)j.getEntity().getShooter();
			player.giveExp(7);
			j.setExperience(0);
		}
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
		
		//For each of the four orientations check: 1. For a furnance or burning furnance and that a factory doesn't exist there
		//Then check for a chest and that a factory doesn't exist at the chest
		//This still allows a double chest to be shared between two factories, which may lead to undesirable behavior
		
		if((northType.getId()== 61 || northType.getId()== 62) && ! productionMan.factoryExistsAt(northLocation))  
		{
			if(southType.getId()== 54 && ! productionMan.factoryExistsAt(southLocation)) 
			{
				return productionMan.createFactory(loc, southLocation, northLocation);
			}
		}
		if((westType.getId()== 61 || westType.getId() == 62) && ! productionMan.factoryExistsAt(westLocation))  
		{  
			if(eastType.getId()== 54 && ! productionMan.factoryExistsAt(eastLocation)) 
			{
				return productionMan.createFactory(loc, eastLocation, westLocation);
			}
		}
		if((southType.getId()== 61 || southType.getId()== 62) && ! productionMan.factoryExistsAt(southLocation))  
		{
			if(northType.getId()== 54 && ! productionMan.factoryExistsAt(northLocation)) 
			{
				return productionMan.createFactory(loc, northLocation, southLocation);
			}
		}
		if((eastType.getId()== 61 || eastType.getId()== 62) && ! productionMan.factoryExistsAt(eastLocation))  
		{
			if(westType.getId()== 54 && ! productionMan.factoryExistsAt(westLocation)) 
			{
				return productionMan.createFactory(loc, westLocation, eastLocation);
			}
		} 
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a factory there!");
	 }
	
}
