package vg.civcraft.mc.namelayer.gui;

import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.misc.Mercury;

/**
 * Abstract utility class, which provides some functionality needed for all guis
 *
 */
public abstract class GroupGUI {
	protected Group g;
	protected Player p;
	protected static GroupManager gm;
	
	public GroupGUI(Group g, Player p) {
		if (gm == null) {
			gm = NameAPI.getGroupManager();
		}
		this.g = g;
		this.p = p;
	}
	
	protected boolean validGroup() {
		if (!g.isValid()) {
			g = gm.getGroup(g.getName());
			if (g == null) {
				return false;
			}
		}
		return true;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public Group getGroup() {
		return g;
	}
	
	protected void checkRecacheGroup(){
		if (NameLayerPlugin.isMercuryEnabled()){
			String message = "recache " + g.getName();
			Mercury.invalidateGroup(message);
		}
	}

}
