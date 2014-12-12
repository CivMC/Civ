package vg.civcraft.mc.citadel.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;

public class GroupsListener implements Listener {
	private ReinforcementManager rm = Citadel.getReinforcementManager();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void deleteGroupEvent(GroupDeleteEvent event){
		if (!event.hasFinished()) // deletion only just began
			return;
		rm.invalidateAllReinforcements();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void mergeGroupEvent(GroupMergeEvent event){
		if (!event.hasFinished()) // merge just began
			return;
		rm.invalidateAllReinforcements();
	}
}
