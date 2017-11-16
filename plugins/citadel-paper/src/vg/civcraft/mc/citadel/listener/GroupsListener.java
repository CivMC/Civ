package vg.civcraft.mc.citadel.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;

public class GroupsListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void deleteGroupEvent(GroupDeleteEvent event){
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void mergeGroupEvent(GroupMergeEvent event){
	}
}
