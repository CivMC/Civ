package vg.civcraft.mc.database;

import org.bukkit.Bukkit;

import vg.civcraft.mc.ConfigManager;

public class SaveManager extends GroupManagerDao{
	
	public SaveManager(Database db) {
		super(db);
		autoFlushDataToDB();
	}
	
	public void flushDataToDB(){
		flushAddMember();
		flushRemoveMember();
		flushAddSubGroup();
		flushRemoveSubGroup();
		flushAddPermission();
		flushUpdatePermissions();
	}
	
	private void autoFlushDataToDB(){
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
				
				new Runnable(){
			
					public void run() {
						flushDataToDB();
					}
			
		}, ConfigManager.getAutoFlush());
	}
}
