package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.util.Permission;

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
		String name = argAsString(0);
		
		ExilePearl pearl = plugin.getPearl(name);
		if (pearl == null) {
			msg("<i>No pearl was found with the name <c>%s", name);
			return;
		}
		
		PearlType type = PearlType.valueOf(argAsString(1));
		if (type == null) {
			type = PearlType.valueOf(argAsInt(1));
		}
		
		if (type == null) {
			msg("<i>Invalid type. Exile=0 or Prison=1");
			return;
		}
		
		pearl.setPearlType(type);
		msg("<g>You updated the pearl type of player %s to %s", pearl.getPlayerName(), type.toString());
	}
}
