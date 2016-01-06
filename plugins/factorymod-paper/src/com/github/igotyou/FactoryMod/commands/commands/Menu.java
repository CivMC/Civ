package com.github.igotyou.FactoryMod.commands.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.utility.MenuBuilder;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class Menu extends PlayerCommand {

	public Menu(String name) {
		super(name);
		setIdentifier("fm");
		setDescription("Opens up the factory brower");
		setUsage("/fm");
		setArguments(0, 1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Fuck off console man");
			return true;
		}
		MenuBuilder mb = FactoryMod.getMenuBuilder();
		Player p = (Player) sender;
		if (args.length == 0) {
			mb.openFactoryBrowser(p, FactoryMod.getManager().getAllEggs().values().iterator()
					.next().getName());
		} else {
			mb.openFactoryBrowser(p, args[0]);
		}
		return true;
	}
	
	@Override
	public List <String> tabComplete(CommandSender arg0, String [] arg1) {
		return new LinkedList<String>();
	}
}
