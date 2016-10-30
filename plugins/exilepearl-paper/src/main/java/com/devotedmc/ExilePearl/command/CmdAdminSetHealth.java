package com.devotedmc.ExilePearl.command;

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
		String name = argAsString(0);
		Integer arg = argAsInt(1);
		if (arg == null) {
			msg("<b>Pearl health must be an integer between 0 and 100.", name);
			return;
		}
		
		int percent = Math.min(100, Math.max(1, arg));
		
		ExilePearl pearl = plugin.getPearl(name);
		if (pearl == null) {
			msg("<i>No pearl was found with the name <c>%s", name);
			return;
		}
		
		// calculate the actual value
		int healthValue = (int)(plugin.getPearlConfig().getPearlHealthMaxValue() * ((double)percent / 100));
		pearl.setHealth(healthValue);
		msg("<g>You updated the pearl health of player %s to %d%%", pearl.getPlayerName(), percent);
	}
}
