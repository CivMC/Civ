package vg.civcraft.mc.namelayer.group;

import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;

public class DefaultGroupHandler {
	
	private GroupManagerDao dao;
	
	private Map <UUID, String> defaultGroups;
	
	public DefaultGroupHandler() {
		dao = NameLayerPlugin.getGroupManagerDao();
		defaultGroups = dao.getAllDefaultGroups();
	}
	
	public String getDefaultGroup(Player p) {
		return getDefaultGroup(p.getUniqueId());
	}
	
	public String getDefaultGroup(UUID uuid) {
		return defaultGroups.get(uuid);
	}
	
	public void setDefaultGroup(Player p, Group g) {
		setDefaultGroup(p.getUniqueId(), g, true);
	}
	
	public void setDefaultGroup(UUID uuid, Group g){
		setDefaultGroup(uuid,g,true);
	}
	
	public void setDefaultGroup(UUID uuid, Group g, boolean savetodb) {
		if (savetodb){
			String previous = defaultGroups.get(uuid);
			if (previous == null) {
				dao.setDefaultGroup(uuid, g.getName());
			}
			else {
				dao.changeDefaultGroup(uuid, g.getName());
			}
		}
		defaultGroups.put(uuid, g.getName());
	}
	
	public String recacheDefaultGroup(UUID uuid) {
		String gName = dao.getDefaultGroup(uuid);
		defaultGroups.put(uuid, gName);
		return gName;
	}
}
