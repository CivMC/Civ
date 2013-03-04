package com.github.igotyou.FactoryMod.listeners;

import static com.untamedears.citadel.Utility.isReinforced;
import static com.untamedears.citadel.Utility.getReinforcement;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.Production;
import com.github.igotyou.FactoryMod.managers.FactoryModManager;
import com.github.igotyou.FactoryMod.managers.ProductionManager;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class BlockListener implements Listener
{
	private FactoryModManager factoryMan;
	//this is a lazy fix...
	private ProductionManager productionMan;
	
	public BlockListener(FactoryModManager factoryManager, ProductionManager productionManager)
	{
		this.factoryMan = factoryManager;
		this.productionMan = productionManager;
	}
	
/*	@EventHandler
	public void blockPlaceEvent(BlockPlaceEvent e)
	{
		 Player player = e.getPlayer();
		 Block block = e.getBlock();
		 Location centralLoctation = block.getLocation();
		 Material type = block.getType();
		 
		 if (type == FactoryModPlugin.CENTRAL_BLOCK_MATERIAL)
		 {
			 createFactory(centralLoctation, player);
		 }
	}*/
	
	@EventHandler
	public void blockBreakEvent(BlockBreakEvent e)
	{
		Block block = e.getBlock();
		if (factoryMan.factoryExistsAt(block.getLocation()))
		{
			if (productionMan.factoryExistsAt(block.getLocation()))
			{
				Production factory = (Production) productionMan.getFactory(block.getLocation());
				e.setCancelled(true);
				
				if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)
				{
					factory.destroy(block.getLocation());
					productionMan.removeFactory(factory);
				}
			}
		}
	}
	
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
					Production factory = (Production) productionMan.getFactory(block.getLocation());
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
	
	@EventHandler
	public void burnListener(BlockBurnEvent e)
	{
		Block block = e.getBlock();
		if (factoryMan.factoryExistsAt(block.getLocation()))
		{
			if (productionMan.factoryExistsAt(block.getLocation()))
			{
				Production factory = (Production) productionMan.getFactory(block.getLocation());
				e.setCancelled(true);
				
				if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)
				{
					factory.destroy(block.getLocation());
					productionMan.removeFactory(factory);
				}
			}
		}
	}
	
	@EventHandler
	public void playerInteractionEvent(PlayerInteractEvent e)
	{
		Block clicked = e.getClickedBlock();
		Player player = e.getPlayer();
		
		if (e.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			if (player.getItemInHand().getType() == FactoryModPlugin.FACTORY_INTERACTION_MATERIAL)
			{
				if (clicked.getType() == FactoryModPlugin.CENTRAL_BLOCK_MATERIAL)
				{
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						PlayerReinforcement reinforcment = (PlayerReinforcement) getReinforcement(clicked);
						if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || !FactoryModPlugin.CITADEL_ENABLED || 
								(reinforcment.isAccessible(player)))
						{
							if (productionMan.factoryExistsAt(clicked.getLocation()))
							{
								Production production = (Production) productionMan.getFactory(clicked.getLocation());
								InteractionResponse.messagePlayerResult(player, production.toggleRecipes());
							}
						}
						else
						{
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use that block!" ));
						}
					}
					else
					{
						createFactory(clicked.getLocation(), player);
					}
				}
				else if (clicked.getType() == Material.FURNACE || clicked.getType() == Material.BURNING_FURNACE)
				{
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						PlayerReinforcement reinforcment = (PlayerReinforcement) getReinforcement(clicked);
						if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || !FactoryModPlugin.CITADEL_ENABLED || 
								(reinforcment.isAccessible(player)))
						{
							if (productionMan.factoryExistsAt(clicked.getLocation()))
							{
								Production production = (Production) productionMan.getFactory(clicked.getLocation());
								InteractionResponse.messagePlayerResult(player, production.togglePower());
							}
						}
						else
						{
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use that block!" ));
						}
					}
				}
				else if (clicked.getType() == Material.CHEST)
				{
					if (factoryMan.factoryExistsAt(clicked.getLocation()))
					{
						PlayerReinforcement reinforcment = (PlayerReinforcement) getReinforcement(clicked);
						if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || !FactoryModPlugin.CITADEL_ENABLED || 
								(reinforcment.isAccessible(player)))
						{
							if (productionMan.factoryExistsAt(clicked.getLocation()))
							{
								Production production = (Production) productionMan.getFactory(clicked.getLocation());
								int procentDone = Math.round(production.getProductionTimer()*100/production.getCurrentRecipe().getProductionTime());
								if (production.getActive() == true)
								{
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "----------Factory Information---------"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Type     : " + production.getSubFactoryType()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Status  : On"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe  : " + production.getCurrentRecipe().getRecipeName()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe output: " + production.getCurrentRecipe().getBatchAmount() + " " + production.getCurrentRecipe().getOutput().getType().toString() + production.getEnchantmentsMessage(production.getCurrentRecipe().getEnchantments())));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe input: " + production.getMaterialsNeededMessage(production.getCurrentRecipe().getInput())));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe fuel requirement: " + production.getCurrentRecipe().getProductionTime()/production.getProductionFactoryProperties().getEnergyTime()*production.getProductionFactoryProperties().getEnergyMaterial().getAmount()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe production time: " + production.getCurrentRecipe().getProductionTime() + " ticks(" + production.getCurrentRecipe().getProductionTime()/20 + "seconds)"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Progress: " + procentDone + "%"));
								}
								else
								{
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "----------Factory Information---------"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Type   : " + production.getSubFactoryType()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Status: Off"));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe: " + production.getCurrentRecipe().getRecipeName()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe output: " + production.getCurrentRecipe().getBatchAmount() + " " + production.getCurrentRecipe().getOutput().getType().toString() + production.getEnchantmentsMessage(production.getCurrentRecipe().getEnchantments())));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe input: " + production.getMaterialsNeededMessage(production.getCurrentRecipe().getInput())));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe fuel requirement: " + production.getCurrentRecipe().getProductionTime()/production.getProductionFactoryProperties().getEnergyTime()*production.getProductionFactoryProperties().getEnergyMaterial().getAmount()));
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.SUCCESS, "Recipe production time: " +  production.getCurrentRecipe().getProductionTime() + " seconds("+ production.getCurrentRecipe().getProductionTime()*FactoryModPlugin.TICKS_PER_SECOND + " ticks)"));
								}
							}
						}
						else
						{
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use that block!" ));
						}
					}
				}
			}
		}
		else if(e.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if (clicked.getType() == Material.CHEST)
			{
				if (factoryMan.factoryExistsAt(clicked.getLocation()))
				{
					PlayerReinforcement reinforcment = (PlayerReinforcement) getReinforcement(clicked);
					if ((FactoryModPlugin.CITADEL_ENABLED && !isReinforced(clicked)) || !FactoryModPlugin.CITADEL_ENABLED || 
							(reinforcment.isAccessible(player)))
					{
						if (productionMan.factoryExistsAt(clicked.getLocation()))
						{
							Production production = (Production) productionMan.getFactory(clicked.getLocation());
							if (production.getActive() == true)
							{
								InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You can't access the chest while the factory is active! Turn it off first!" ));
								e.setCancelled(true);
							}
						}
					}
					else
					{
						InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You do not have permission to use that block!" ));
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
