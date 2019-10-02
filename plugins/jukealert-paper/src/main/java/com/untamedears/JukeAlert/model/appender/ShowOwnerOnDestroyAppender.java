package com.untamedears.JukeAlert.model.appender;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.SnitchAction;
import com.untamedears.JukeAlert.model.actions.impl.DestroySnitchAction;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class ShowOwnerOnDestroyAppender extends AbstractSnitchAppender {

	public static final String ID = "showowner";

	public ShowOwnerOnDestroyAppender(Snitch snitch) {
		super(snitch);
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return true;
	}

	@Override
	public void acceptAction(SnitchAction action) {
		if (!action.isLifeCycleEvent()) {
			return;
		}
		if (!(action instanceof DestroySnitchAction)) {
			return;
		}
		DestroySnitchAction dsa = ((DestroySnitchAction) action);
		UUID destroyerUUID = dsa.getPlayer();
		Player player = Bukkit.getPlayer(destroyerUUID);
		if (player == null) {
			return;
		}
		Group group = GroupManager.getGroup(snitch.getId());
		String groupName;
		String ownerName;
		if (group == null) {
			groupName = "unknown";
			ownerName = "unknown";
		} else {
			groupName = group.getName();
			ownerName = NameAPI.getCurrentName(group.getOwner());
		}
		player.sendMessage(
				String.format("%s was reinforced on %s owned by %s", snitch.getType().getName(), groupName, ownerName));
	}

}
