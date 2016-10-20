package com.github.igotyou.FactoryMod.commands.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.commands.FactoryModCommandHandler;
import com.github.igotyou.FactoryMod.utility.MenuBuilder;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class Menu extends PlayerCommand {

	public Menu(String name) {
		super(name);
		setIdentifier("fm");
		setDescription("Opens up the factory brower");
		setUsage("/fm");
		setArguments(0, 10);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC + "Fuck off console man");
			return true;
		}
		MenuBuilder mb = FactoryMod.getMenuBuilder();
		Player p = (Player) sender;
		if (p.isInsideVehicle()) {
			p.sendMessage(ChatColor.RED + "You can't use this command in vehicles");
			return true;
		}
		if (args.length == 0) {
			mb.openFactoryBrowser(p, null);
		} else {
			mb.openFactoryBrowser(p, FactoryModCommandHandler.getFactoryName(args));
		}
		return true;
	}

	@Override
	public List <String> tabComplete(CommandSender arg0, String [] arg1) {
		return FactoryModCommandHandler.tabCompleteFactory(arg0, arg1);
	}
}
