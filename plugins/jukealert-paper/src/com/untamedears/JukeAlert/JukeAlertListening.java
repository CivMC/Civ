package com.untamedears.JukeAlert;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JukeAlertListening extends JavaPlugin implements Listener{

	  public void placeSnitchBlock(BlockPlaceEvent bpe) {
		  
		 
		
		 if (block.getType()== Material.JUKEBOX){
			 		
		}
		public block(block BlockPlacedEvent()){
			return block;
		}


			 
		 
		 
	  }
	  private void registerEvents() {
		    getServer().getPluginManager().registerEvents(this, this);
		  }
}
