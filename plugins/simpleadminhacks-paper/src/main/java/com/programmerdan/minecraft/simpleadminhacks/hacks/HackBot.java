package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.bots.Bot;
import com.programmerdan.minecraft.simpleadminhacks.configs.HackBotConfig;

/**
 * Wrapping some NPC entity classes for maximum admin fun
 * 
 * @author ProgrammerDan
 *
 */
public class HackBot extends SimpleHack<HackBotConfig> implements Listener, CommandExecutor {

	private HashMap<String, Bot> bots;
	
	public static final String NAME = "HackBot";
	
	public HackBot(SimpleAdminHacks plugin, HackBotConfig config) {
		super(plugin, config);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 2) { 
			return false;
		}
		
		String cmd = args[0];
		if ("all".equals(cmd)) {
			String subcmd = args[1];
			if ("save".equals(subcmd)) {
				for (Bot npc : bots.values()) {
					npc.flushToConfig();
				}
				plugin().saveConfig();
			} else if ("spawn".equals(subcmd)) {
				for (Bot npc : bots.values()) {
					npc.spawn();
				}
			} else if ("despawn".equals(subcmd)) {
				for (Bot npc : bots.values()) {
					npc.despawn();
				}
			} else {
				return false;
			}
		} else if ("generate".equals(cmd)) {
			String npcname = args[1];
			
			
		} else {
			String npcname = args[1];
			Bot npc = bots.get(npcname);
			if (npc == null) {
				sender.sendMessage("Unable to find NPC by name");
				return false;
			}
			if ("save".equals(cmd)) {
				npc.flushToConfig();
				plugin().saveConfig();
			} else if ("despawn".equals(cmd)) {
				npc.despawn();
			} else if ("destroy".equals(cmd)) {
				npc.despawn();
				plugin().getConfig().set(npc.config().getCurrentPath(), null);
				npc.invalidate();
				bots.remove(npcname);
				plugin().saveConfig();
			} else if ("spawn".equals(cmd)) {
				npc.spawn();
			} else {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void registerListeners() {
		plugin().registerListener(this);
	}

	@Override
	public void registerCommands() {
		plugin().registerCommand("bot", this);
	}

	@Override
	public void dataBootstrap() {
		this.bots = new HashMap<String, Bot>();
	}

	@Override
	public void unregisterListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterCommands() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataCleanup() {
		this.bots.clear();
		this.bots = null;
	}

	@Override
	public String status() {
		// TODO Auto-generated method stub
		return null;
	}

}
