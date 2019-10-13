package com.untamedears.JukeAlert.model.actions.impl;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.abstr.LoggableBlockAction;
import com.untamedears.JukeAlert.util.JAUtility;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

public class BlockBreakAction extends LoggableBlockAction {
	
	public static final String ID = "BLOCK_BREAK";

	public BlockBreakAction(long time, Snitch snitch, UUID player, Location location, String materialString) {
		this(time, snitch, player, location, JAUtility.parseMaterial(materialString));
	}
	
	public BlockBreakAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player, location, material);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public IClickable getGUIRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return new TextComponent(String.format("%sBreak  %s%s  %s%s %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()),ChatColor.AQUA, material.toString(), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
	}

}
