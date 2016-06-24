/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.aleksey.castlegates.CastleGates;

public class EventListener implements Listener {
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
    
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		CastleGates.getManager().handleBlockBreak(event);
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
    public void onBlockRedstone(BlockRedstoneEvent event) {
		CastleGates.getManager().handleBlockRedstone(event);
	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		CastleGates.getManager().handleBlockPhysics(event);
	}
}