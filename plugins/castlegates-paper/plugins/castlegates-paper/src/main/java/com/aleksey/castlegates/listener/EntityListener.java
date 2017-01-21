/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.aleksey.castlegates.CastleGates;

public class EntityListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
    	CastleGates.getManager().handlePlayerJoin(event);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
    	CastleGates.getManager().handlePlayerQuit(event);
    }
	
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if(event.getClickedBlock() != null
    			&& event.getAction() == Action.LEFT_CLICK_BLOCK
    			)
    	{
    		CastleGates.getManager().handleBlockClicked(event);
    	}    	
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		CastleGates.getManager().handleEntityExplode(event);
	}
	
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		CastleGates.getManager().handleEntityChangeBlock(event);
	}
}