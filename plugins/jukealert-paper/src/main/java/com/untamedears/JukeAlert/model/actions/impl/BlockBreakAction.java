package com.untamedears.JukeAlert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.JukeAlert.model.actions.BlockAction;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class BlockBreakAction extends BlockAction {

	public BlockBreakAction(long time, UUID player, Location location, Material material) {
		super(time, player, location, material);
	}

	@Override
	public String getIdentifier() {
		return "BLOCK_BREAK";
	}

	@Override
	public IClickable getGUIRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent getChatRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

}
