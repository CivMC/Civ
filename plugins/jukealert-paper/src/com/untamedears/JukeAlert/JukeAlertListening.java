package com.untamedears.JukeAlert;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;

public class JukeAlertListening {
	  public void placeSnitchBlock(BlockPlaceEvent bpe) {
		  
		 Block block= bpe.getBlock();
		
		 if (block.getType()== Material.JUKEBOX){
			 
		 }
	  }
}
