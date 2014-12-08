package vg.civcraft.mc.citadel.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class SaveDatabaseManager extends CitadelReinforcementData {

	private Map<DBAction, List<Reinforcement>> reins = new ConcurrentHashMap<DBAction, List<Reinforcement>>();

	private List<Group> deleteGroups = new ArrayList<Group>();
	public SaveDatabaseManager(Database db) {
		super(db);
		initializeSavesCaching();
		// Every 1 tick runs an update, insert, or delete
		Bukkit.getScheduler().runTaskTimerAsynchronously(Citadel.getInstance(),
				new Runnable() {

					@Override
					public void run() {
						for (DBAction action : reins.keySet()) {
							if (action == DBAction.INSERT)
								for (Reinforcement rein: reins.get(action))
									insertReinforcement(rein);
						}
					}

				}, 20, 1);
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

	private void initializeSavesCaching() {
		reins.put(DBAction.INSERT, new ArrayList<Reinforcement>());
		reins.put(DBAction.UPDATE, new ArrayList<Reinforcement>());
		reins.put(DBAction.DELETE, new ArrayList<Reinforcement>());
	}

	public void insertReinforcement(Reinforcement rein) {
		List<Reinforcement> list = reins.get(DBAction.INSERT);
		synchronized(list){
			list.add(rein);;
		}
	}

	public void saveReinforcement(Reinforcement rein) {
		// We don't have to worry if this executes before insert.
		// On insert it will add the most recent data regardless.
		List<Reinforcement> list = reins.get(DBAction.UPDATE);
		synchronized(list){
			list.add(rein);
		}
	}

	public void deleteReinforcement(Reinforcement rein) {
		// It shouldn't be inserted if it's about to be deleted.
		List<Reinforcement> list = reins.get(DBAction.INSERT);
		synchronized(list){
			if (list.contains(rein))
				list.remove(rein);
		}
		list = reins.get(DBAction.DELETE);
		synchronized(list){
			list.add(rein);
		}
	}
	
	public void flushAllReinforcements(){
		Citadel.Log("Flushing all stored reinforcements to the database.\n"
				+ "This can take a long time depending on the activity of "
				+ "your server. Please do not reload the server during this time.");
		for (DBAction db: reins.keySet()){
			List<Reinforcement> list = reins.get(db);
			int maxSize = list.size();
			long currentTime = System.currentTimeMillis() * 1000;
			Citadel.Log(String.format("Flushing %s with %d records.", db.name(),
					maxSize));
			int current = 0;
			if (db == DBAction.INSERT){
				for (Reinforcement rein: list)
					insertReinforcement(rein);
				current++;
			}
			else if (db == DBAction.UPDATE){
				for (Reinforcement rein: list)
					saveReinforcement(rein);
				current++;
			}
			else if (db == DBAction.DELETE){
				for (Reinforcement rein: list)
					deleteReinforcement(rein);
				current++;
			}
			int timeTook = (int) ((System.currentTimeMillis()*1000) /currentTime);
			Citadel.Log(String.format("That action took %d seconds.", timeTook));
		}
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