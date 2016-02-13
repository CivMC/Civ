package com.github.igotyou.FactoryMod.commands.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
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
		if (args.length == 0) {
			mb.openFactoryBrowser(p, null);
		} else {
			mb.openFactoryBrowser(p, getFactoryName(args));
		}
		return true;
	}

	@Override
	public List <String> tabComplete(CommandSender arg0, String [] arg1) {
		List <String> fac = new LinkedList<String>();
		String entered = getFactoryName(arg1);
		for(String name:FactoryMod.getManager().getAllEggs().keySet()) {
			if (name.toLowerCase().startsWith(entered)) {
				fac.add(name);
			}
		}
		return fac;
	}

	private String getFactoryName(String[] args) {
		if (args.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			sb.append(arg);
			sb.append(" ");
		}
		return sb.toString().substring(0, sb.length() - 1).toLowerCase();
	}
}
