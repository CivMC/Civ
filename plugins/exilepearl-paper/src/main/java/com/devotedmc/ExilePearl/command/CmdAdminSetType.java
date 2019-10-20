package com.devotedmc.ExilePearl.command;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.util.Permission;
import com.devotedmc.ExilePearl.util.SpawnUtil;

public class CmdAdminSetType extends PearlCommand {

	public CmdAdminSetType(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("settype");

		this.setHelpShort("Sets the type of a pearl.");

		this.commandArgs.add(requiredPearlPlayer());
		this.commandArgs.add(required("type", autoTab("", "Enter the pearl type")));

		this.permission = Permission.SET_TYPE.node;
		this.visibility = CommandVisibility.SECRET;
	}

	@Override
	public void perform() {
		UUID playerId = argAsPlayerOrUUID(0);
		if (playerId == null) {
			msg("<i>No player was found matching <c>%s", argAsString(0));
			return;
		}

		ExilePearl pearl = plugin.getPearl(playerId);
		if (pearl == null) {
			msg("<i>No pearl was found matching <c>%s", argAsString(0));
			return;
		}

		PearlType type = PearlType.fromString(argAsString(1));
		if (type == null) {
			type = PearlType.valueOf(argAsInt(1));
		}

		if (type == null) {
			msg("<i>Invalid type. Exile=0 or Prison=1");
			return;
		}

		pearl.setPearlType(type);
		Player player = pearl.getPlayer();
		if(player != null && player.isOnline()) {
			SpawnUtil.spawnPlayer(player, pearl.getPearlType() == PearlType.PRISON ? ExilePearlPlugin.getApi().getPearlConfig().getPrisonWorld() : ExilePearlPlugin.getApi().getPearlConfig().getMainWorld());
		}
		msg("<g>You updated the pearl type of player %s to %s", pearl.getPlayerName(), type.toString());
	}
}
