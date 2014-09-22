package vg.civcraft.mc.database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import vg.civcraft.mc.NameTrackerPlugin;
import vg.civcraft.mc.group.Group;

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
			
		}, config.getInt("groups.database.autoflush"));
	}
}
