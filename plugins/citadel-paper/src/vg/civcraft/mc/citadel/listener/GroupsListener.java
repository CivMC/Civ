package vg.civcraft.mc.citadel.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.database.SaveDatabaseManager;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;
import vg.civcraft.mc.namelayer.group.Group;

public class GroupsListener implements Listener{

	private SaveDatabaseManager db = Citadel.getCitadelDatabase();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void groupDeleteEvent(GroupDeleteEvent event){
		Group group = event.getGroup();
		if (db.shouldSkipDelete(group))
			return;
		db.addDeleteGroup(group);
		group.setDisciplined(true);
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void groupMergeEvent(final GroupMergeEvent event){
		Bukkit.getScheduler().runTaskLater(Citadel.getInstance(), new Runnable(){

			@Override
			public void run() {
				Group groupSurviving = event.getMergingInto();
				Group groupMerging = event.getToBeMerged();
				groupMerging = groupSurviving; // Update all the reinforcements with the new group.
			}
			
		}, 20); // Need to wait for the caching to merge over
	}
}
