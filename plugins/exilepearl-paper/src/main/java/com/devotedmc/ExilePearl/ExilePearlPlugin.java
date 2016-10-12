package com.devotedmc.ExilePearl;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.devotedmc.ExilePearl.core.CorePluginFactory;

import vg.civcraft.mc.civmodcore.ACivMod;

/**
 * The plugin class for ExilePearl
 * <p>
 * The meat of the plugin is located in ExilePearlCore.java
 * 
 * @author GordonFreemanQ
 *
 */
public final class ExilePearlPlugin extends ACivMod {
	
	private static ExilePearlApi core;
	
	public static ExilePearlApi getApi() {
		return core;
	}
	
	public ExilePearlPlugin() {
		core = CorePluginFactory.createCore(this);
	}
	
	@Override
	public void onLoad() { 
		core.onLoad();
	}

	@Override
	public void onEnable() {
		core.onEnable();
	}

	@Override
	public void onDisable() {
		core.onDisable();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		return core.onCommand(sender, cmd, alias, args);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		return core.onTabComplete(sender, cmd, alias, args);
	}

	@Override
	public final String getPluginName() {
		return "ExilePearl";
	}
}
