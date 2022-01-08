package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.util.Permission;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CmdAdminSetDate extends PearlCommand {

	public CmdAdminSetDate(ExilePearlApi pearlApi) {
		super(pearlApi);

		this.aliases.add("setdate");

		this.setHelpShort("Sets the creation date of a pearl.");

		this.commandArgs.add(requiredPearlPlayer());
		this.commandArgs.add(required("date yy/mm/dd", autoTab("", "date yy/mm/dd")));

		this.permission = Permission.SET_DATE.node;
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
		Date date;
		try {
			 date = new SimpleDateFormat("yy/MM/dd").parse(argAsString(1));
		} catch (ParseException e) {
			msg("<b>Date must be a valid date");
			return;

		}

		pearl.setPearledOn(date);
		msg("<g>Date set!");

	}

}
