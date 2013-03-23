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
import com.github.igotyou.FactoryMod.utility.InventoryMethods;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class BlockListener implements Listener
{
	private FactoryModManager factoryMan;
	//this is a lazy fix...
	private ProductionManager productionMan;
	
	/**
	 * Constructor
	 */
	public BlockListener(FactoryModManager factoryManager, ProductionManager productionManager)
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
					productionMan.removeFactory(factory);
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
			if (factoryMan.factoryExistsAt(block.getLocation()))
			{
				if (productionMan.factoryExistsAt(block.getLocation()))
				{
					ProductionFactory factory = (ProductionFactory) productionMan.getFactory(block.getLocation());
					e.setCancelled(true);
					
					if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)
					{
						factory.destroy(block.getLocation());
						productionMan.removeFactory(factory);
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
					productionMan.removeFactory(factory);
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
						//if the player is allowed to interact with that block.
						if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || 
								(((PlayerReinforcement) getReinforcement(clicked)).isAccessible(player)))
						{
							//if there is a production Factory at the clicked location
							if (productionMan.factoryExistsAt(clicked.getLocation()))
							{
								ProductionFactory production = (ProductionFactory) productionMan.getFactory(clicked.getLocation());
								//toggle the recipe, and print the returned message.
								InteractionResponse.messagePlayerResult(player, production.toggleRecipes());
							}
						}
						//if the player does NOT have acssess to the block that was clicked
						else
						{
							//return a error message
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use that block!" ));
						}
					}
					//if no factory exists at the clicked location
					else
					{
						//if the player is allowed to interact with that block.
						if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || 
								(((PlayerReinforcement) getReinforcement(clicked)).isAccessible(player)))
						{
							createFactory(clicked.getLocation(), player);
						}
						//if the player does NOT have acssess to the block that was clicked
						else
						{
							//return a error message
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use that block!" ));
						}
					}
				}
				//if the clicked block is a furnace
				else if (clicked.getType() == Material.FURNACE || clicked.getType() == Material.BURNING_FURNACE)
				{
					//if there is a factory at that location
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						//if the player is allowed to interact with that block.
						if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || 
								(((PlayerReinforcement) getReinforcement(clicked)).isAccessible(player)))
						{
							if (productionMan.factoryExistsAt(clicked.getLocation()))
							{
								ProductionFactory production = (ProductionFactory) productionMan.getFactory(clicked.getLocation());
								//toggle the power, and print the returned message
								InteractionResponse.messagePlayerResult(player, production.togglePower());
							}
						}
						//if the player is NOT allowed to interact with the clicked block.
						else
						{
							//return error message
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use that block!" ));
						}
					}
				}
				//if the block clicked is a chest
				else if (clicked.getType() == Material.CHEST)
				{
					//is there a factory there?
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						//if the player is allowed to interact with that block?
						if ((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || 
								(((PlayerReinforcement) getReinforcement(clicked)).isAccessible(player)))
						{
							if (productionMan.factoryExistsAt(clicked.getLocation()))
							{
								ProductionFactory production = (ProductionFactory) productionMan.getFactory(clicked.getLocation());
								int procentDone = Math.round(production.getProductionTimer()*100/production.getCurrentRecipe().getProductionTime());
								//if the clicked factory is turned on, print information
								if (production.getActive() == true)
								{
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "----------Factory Information---------"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Type     : " + production.getProductionFactoryProperties().getName()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Status  : On"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe  : " + production.getCurrentRecipe().getRecipeName()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe output: " + production.getCurrentRecipe().getBatchAmount() + " " + production.getCurrentRecipe().getOutput().getData().toString() + InventoryMethods.getEnchantmentsMessage(production.getCurrentRecipe().getEnchantments())));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe input: " + InventoryMethods.getMaterialsNeededMessage(production.getCurrentRecipe().getInput())));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe fuel requirement: " + production.getCurrentRecipe().getProductionTime()/production.getProductionFactoryProperties().getEnergyTime()*production.getProductionFactoryProperties().getEnergyMaterial().getAmount() + " " + production.getProductionFactoryProperties().getEnergyMaterial().getData().toString()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe production time: " +  production.getCurrentRecipe().getProductionTime() + " seconds("+ production.getCurrentRecipe().getProductionTime()*FactoryModPlugin.TICKS_PER_SECOND + " ticks)"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Progress: " + procentDone + "%"));
								}
								//if the factory is turned off, print information
								else
								{
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "----------Factory Information---------"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Type   : " + production.getProductionFactoryProperties().getName()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Status: Off"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe: " + production.getCurrentRecipe().getRecipeName()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe output: " + production.getCurrentRecipe().getBatchAmount() + " " + production.getCurrentRecipe().getOutput().getData().toString() + InventoryMethods.getEnchantmentsMessage(production.getCurrentRecipe().getEnchantments())));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe input: " + InventoryMethods.getMaterialsNeededMessage(production.getCurrentRecipe().getInput())));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe fuel requirement: " + production.getCurrentRecipe().getProductionTime()/production.getProductionFactoryProperties().getEnergyTime()*production.getProductionFactoryProperties().getEnergyMaterial().getAmount() + " " + production.getProductionFactoryProperties().getEnergyMaterial().getData().toString()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe production time: " +  production.getCurrentRecipe().getProductionTime() + " seconds("+ production.getCurrentRecipe().getProductionTime()*FactoryModPlugin.TICKS_PER_SECOND + " ticks)"));
								}
							}
						}
						//if the player is NOT allowed to interact with the clicked block
						else
						{
							//return error message
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use that block!" ));
						}
					}
				}
			}
		}
		//if the player right clicked a block
		else if(e.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			//if the player right clicked a chest
			if (clicked.getType() == Material.CHEST)
			{
				//is the chest part of a factory?
				if (factoryMan.factoryExistsAt(clicked.getLocation()))
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
	
	private void createFactory(Location loc, Player player)
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
	      
		if(northType.getId()== 61 || northType.getId()== 62)  
		{
			if(southType.getId()== 54) 
			{
				InteractionResponse.messagePlayerResult(player, productionMan.createFactory(loc, southLocation, northLocation));
			}
		}
	        
		if(westType.getId()== 61 || westType.getId() == 62)  
		{  
			if(eastType.getId()== 54) 
			{
				InteractionResponse.messagePlayerResult(player, productionMan.createFactory(loc, eastLocation, westLocation));
			}
		}
	            
		if(southType.getId()== 61 || southType.getId()== 62)  
		{
			if(northType.getId()== 54) 
			{
				InteractionResponse.messagePlayerResult(player, productionMan.createFactory(loc, northLocation, southLocation));
			}
		}        
	        
		if(eastType.getId()== 61 || eastType.getId()== 62)  
		{
			if(westType.getId()== 54) 
			{
				InteractionResponse.messagePlayerResult(player, productionMan.createFactory(loc, westLocation, eastLocation));
			}
		} 
	 }		
}
