package com.github.igotyou.FactoryMod.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
		 Location centralLoctation = block.getLocation();
		 Material type = block.getType();
		 
		 if (type == FactoryModPlugin.CENTRAL_BLOCK_MATERIAL)
		 {
			 Location northLocation = northLoc(centralLoctation);
			 Location southLocation = southLoc(centralLoctation);
			 Location eastLocation = eastLoc(centralLoctation);
			 Location westLocation = westLoc(centralLoctation);
			 
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
		    		  InteractionResponse.messagePlayerResult(player, productionMan.createFactory(centralLoctation, southLocation, northLocation));
		    	  }
		      }
		        
		      if(westType.getId()== 61 || westType.getId() == 62)  
		      {  
		    	  if(eastType.getId()== 54) 
		    	  {
		    		  InteractionResponse.messagePlayerResult(player, productionMan.createFactory(centralLoctation, eastLocation, westLocation));
		    	  }
		      }
		            
		      if(southType.getId()== 61 || southType.getId()== 62)  
		      {
		    	  if(northType.getId()== 54) 
		    	  {
		    		  InteractionResponse.messagePlayerResult(player, productionMan.createFactory(centralLoctation, northLocation, southLocation));
		    	  }
		      }        
		        
		        if(eastType.getId()== 61 || eastType.getId()== 62)  
		        {
		        	if(westType.getId()== 54) 
		        	{
		        		InteractionResponse.messagePlayerResult(player, productionMan.createFactory(centralLoctation, westLocation, eastLocation));
		        	}
		        } 
		 }
	}
	
	@EventHandler
	public void blockBreakEvent(BlockBreakEvent e)
	{
		
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
