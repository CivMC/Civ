package com.devotedmc.ExilePearl.command;

import java.util.UUID;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.Permission;

public class CmdAdminSetHealth extends PearlCommand {

	public CmdAdminSetHealth(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("sethealth");

		this.setHelpShort("Sets the health % value of a pearl.");
		
		this.commandArgs.add(requiredPearlPlayer());
		this.commandArgs.add(required("health %", autoTab("", "Enter the desired health percent")));
		
		this.permission = Permission.SET_HEALTH.node;
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
		
		Integer arg = argAsInt(1);
		if (arg == null) {
			msg("<b>Pearl health must be an integer between 0 and 100.");
			return;
		}
		
		int percent = Math.min(100, Math.max(1, arg));
		
		// calculate the actual value
		int healthValue = (int)(plugin.getPearlConfig().getPearlHealthMaxValue() * ((double)percent / 100));
		pearl.setHealth(healthValue);
		msg("<g>You updated the pearl health of player %s to %d%%", pearl.getPlayerName(), percent);
	}
}
