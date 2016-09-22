package com.programmerdan.minecraft.simpleadminhacks.hacks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.BadBoyWatchConfig;

/**
 * Watches for bad boys by tracking block breaks.
 * 
 * @author ProgrammerDan
 *
 */
public class BadBoyWatch extends SimpleHack<BadBoyWatchConfig> implements Listener {

	public BadBoyWatch(SimpleAdminHacks plugin, BadBoyWatchConfig config) {
		super(plugin, config);
	}
	
	@EventHandler(prority = EventPriority.MONITOR, ignoreCancelled=true) {
		
	}

	@Override
	public void registerListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataBootstrap() {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public String status() {
		return null;
	}

	public static BadBoyWatchConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BadBoyWatchConfig(plugin, config);
	}
	
}
