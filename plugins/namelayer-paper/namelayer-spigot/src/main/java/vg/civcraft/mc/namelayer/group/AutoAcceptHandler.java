package vg.civcraft.mc.namelayer.group;

import java.util.Set;
import java.util.UUID;

import vg.civcraft.mc.namelayer.NameLayerPlugin;

public class AutoAcceptHandler {

	private Set<UUID> autoAccepts;

	public AutoAcceptHandler(Set<UUID> autoAccepts) {
		this.autoAccepts = autoAccepts;
	}

	public void setAutoAccept(UUID player, boolean accept, boolean persistToDb) {
		if (accept && !autoAccepts.contains(player)) {
			autoAccepts.add(player);
			if (persistToDb) {
				NameLayerPlugin.getGroupManagerDao().autoAcceptGroupsAsync(player);
			}
		} else {
			if (autoAccepts.contains(player)) {
				autoAccepts.remove(player);
				if (persistToDb) {
					NameLayerPlugin.getGroupManagerDao().removeAutoAcceptGroupAsync(player);
				}
			}
		}

	}

	public void toggleAutoAccept(UUID player, boolean persistToDb) {
		setAutoAccept(player, !getAutoAccept(player), persistToDb);
	}

	public boolean getAutoAccept(UUID uuid) {
		return autoAccepts.contains(uuid);
	}
}
