package com.github.igotyou.FactoryMod.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.managers.FactoryModManager;
import com.github.igotyou.FactoryMod.managers.ProductionManager;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;

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
	
	@EventHandler
	public void blockPlaceEvent(BlockPlaceEvent e)
	{
		 Player player = e.getPlayer();
		 Block block = e.getBlock();
		 Location CentralLoctation = block.getLocation();
		 Material type = block.getType();
		 
		 if (type == FactoryModPlugin.CENTRAL_BLOCK_MATERIAL)
		 {
			 Location northLocation = northLoc(CentralLoctation);
			 Location southLocation = southLoc(CentralLoctation);
			 Location eastLocation = eastLoc(CentralLoctation);
			 Location westLocation = westLoc(CentralLoctation);
			 
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
		    	  player.sendMessage("Furnace detected North");
		    	  if(southType.getId()== 54) 
		    	  {
		    		  player.sendMessage("Correct placement, checking chest for factory foundations");
		    		  InteractionResponse.messagePlayerResult(player, productionMan.createFactory(CentralLoctation, southLocation, northLocation));
		    	  }
		      }
		        
		      if(westType.getId()== 61 || westType.getId() == 62)  
		      {  
		    	  player.sendMessage("Furnace detected West");
		    	  if(eastType.getId()== 54) 
		    	  {
		    		  player.sendMessage("Correct placement, checking chest for factory foundations");
		    		  InteractionResponse.messagePlayerResult(player, productionMan.createFactory(CentralLoctation, eastLocation, westLocation));
		    	  }
		      }
		            
		      if(southType.getId()== 61 || southType.getId()== 62)  
		      {
		    	  player.sendMessage("Furnace detected South");
		    	  if(northType.getId()== 54) 
		    	  {
		    		  player.sendMessage("Correct placement, checking chest for factory foundations");
		    		  InteractionResponse.messagePlayerResult(player, productionMan.createFactory(CentralLoctation, northLocation, southLocation));
		    	  }
		      }        
		        
		        if(eastType.getId()== 61 || eastType.getId()== 62)  
		        {
		        	player.sendMessage("Furnace detected East");
		        	if(westType.getId()== 54) 
		        	{
		        		player.sendMessage("Correct placement, checking chest for factory foundations");
		        		player.sendMessage("west location block type is:" + westLocation.getBlock().getType().toString() + " , and location is" + westLocation.toString());
		        		InteractionResponse.messagePlayerResult(player, productionMan.createFactory(CentralLoctation, westLocation, eastLocation));
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
}
