package com.github.igotyou.FactoryMod.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.commands.commands.Create;
import com.github.igotyou.FactoryMod.commands.commands.Menu;

import vg.civcraft.mc.civmodcore.command.CommandHandler;

public class FactoryModCommandHandler extends CommandHandler{
	public void registerCommands() {
		addCommands(new Menu("fm"));
		addCommands(new Create("fmc"));
	}
	
	public static List <String> tabCompleteFactory(CommandSender arg0, String [] arg1) {
		List <String> fac = new LinkedList<String>();
		String entered = getFactoryName(arg1);
		entered = entered.toLowerCase();
		for(String name:FactoryMod.getManager().getAllEggs().keySet()) {
			if (name.toLowerCase().startsWith(entered)) {
				fac.add(name);
			}
		}
		if (fac.size() == 0) {
			return fac;
		}
		if (fac.size() > 1) {
			List <String> res = new LinkedList<String>();
			for(String s : fac) {
				String toAdd = s.split(" ")[arg1.length - 1];
				if (!res.contains(toAdd)) {
					res.add(toAdd);
				}
			}
			return res;
		}
		StringBuilder sb = new StringBuilder();
		for(int i = arg1.length - 1; i < fac.get(0).split(" ").length; i++) {
			sb.append(fac.get(0).split(" ") [i]);
			sb.append(" ");
		}
		fac.clear();
		fac.add(sb.toString().substring(0, sb.length() - 1).toLowerCase());
		return fac;
	}

	public static String getFactoryName(String[] args) {
		if (args.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			sb.append(arg);
			sb.append(" ");
		}
		return sb.toString().substring(0, sb.length() - 1);
	}
	

}
