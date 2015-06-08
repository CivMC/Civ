package vg.civcraft.mc.namelayer.command;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.misc.Mercury;

public abstract class PlayerCommandMiddle extends PlayerCommand{

	public PlayerCommandMiddle(String name) {
		super(name);
	}

	protected GroupManager gm = NameAPI.getGroupManager();
	
	public void checkRecacheGroup(Group g){
		if (NameLayerPlugin.isMercuryEnabled()){
			String message = "recache " + g.getName();
			Mercury.invalidateGroup(message);
		}
	}
}
