package com.untamedears.jukealert.model.appender;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.actions.internal.DestroySnitchAction;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class ShowOwnerOnDestroyAppender extends AbstractSnitchAppender {

	public static final String ID = "showownerondestroy";

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
		Group group = snitch.getGroup();
		String groupName;
		String ownerName;
		if (group == null) {
			groupName = "unknown";
			ownerName = "unknown";
		} else {
			groupName = group.getName();
			ownerName = NameAPI.getCurrentName(group.getOwner());
		}
		player.sendMessage(String.format("%s%s %swas reinforced on %s%s%s owned by %s%s", ChatColor.GOLD,
				snitch.getType().getName(), ChatColor.YELLOW, ChatColor.GREEN, groupName, ChatColor.YELLOW,
				ChatColor.LIGHT_PURPLE, ownerName));
	}

}
