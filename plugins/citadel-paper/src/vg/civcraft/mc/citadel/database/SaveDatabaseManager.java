package vg.civcraft.mc.citadel.database;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class SaveDatabaseManager extends CitadelReinforcementData {

	private List<Group> deleteGroups = new ArrayList<Group>();
	public SaveDatabaseManager(Database db) {
		super(db);
		deleteGroups = getDeleteGroups();
		Bukkit.getScheduler().runTaskTimerAsynchronously(Citadel.getInstance(),
				new Runnable(){

					@Override
					public void run() {
						delete();
					}
					
					public void delete(){
						if (deleteGroups.isEmpty())
							return;
						Group g = deleteGroups.get(0);
						boolean finished = deleteGroup(g.getName());
						if (finished) // still has to delete some counts
							return;
						removeDeleteGroup(g.getName());
						NameAPI.getGroupManager().deleteGroup(g.getName());
						deleteGroups.remove(g);
					}
			
		}, 20, 1);
	}
	/**
	 * Is used to check if a group is now ready to be deleted after being postponed.
	 * @param The group that was supposed to be deleted.
	 * @return Returns true if it should now be deleted.
	 */
	public boolean shouldSkipDelete(Group g){
		return deleteGroups.contains(g);
	}
	
	public void addDeleteGroup(Group g){
		insertDeleteGroup(g.getName());
		deleteGroups.add(g);
	}
}