package vg.civcraft.mc.namelayer.gui;

import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

/**
 * Abstract utility class, which provides some functionality needed for all guis
 *
 */
public abstract class AbstractGroupGUI {
	protected Group g;
	protected Player p;
	protected static GroupManager gm;
	
	public AbstractGroupGUI(Group g, Player p) {
		if (gm == null) {
			gm = NameAPI.getGroupManager();
		}
		this.g = g;
		this.p = p;
	}
	
	protected boolean validGroup() {
		if (!g.isValid()) {
			g = GroupManager.getGroup(g.getName());
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
	

}
