package com.untamedears.JukeAlert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.abstr.LoggablePlayerVictimAction;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class KillPlayerAction extends LoggablePlayerVictimAction {
	
	public static final String ID = "KILL_PLAYER";

	public KillPlayerAction(long time, Snitch snitch, UUID player, Location location, UUID victim) {
		super(time, snitch, player, location, victim.toString());
	}
	
	public String getVictimName() {
		return NameAPI.getCurrentName(getVictimUUID());
	}
	
	public UUID getVictimUUID() {
		return UUID.fromString(victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
