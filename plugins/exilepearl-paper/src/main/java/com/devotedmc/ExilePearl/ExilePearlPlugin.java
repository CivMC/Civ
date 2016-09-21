package com.devotedmc.ExilePearl;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.ACivMod;

/**
 * An offshoot of Civcraft's PrisonPearl plugin
 * @author GordonFreemanQ, aleks
 *
 */
public class ExilePearlPlugin extends ACivMod {
	
	
	public ExilePearlPlugin() {
		
	}

	/**
	 * Spigot enable method
	 */
	@Override
	public void onEnable() {
		super.onEnable();
	}

	/**
	 * Spigot disable method
	 */
	@Override
	public void onDisable() {
		super.onDisable();
	}
	
	public Player getPlayerById(UUID uniqueId) {
		return null;// TODO Namelayer
	}

	/**
	 * Gets the plugin name
	 */
	@Override
	protected final String getPluginName() {
		return "ExilePearl";
	}
	
	public void log(Level level, String msg, Object... args) {
		getLogger().log(level, String.format(msg, args));
	}
	
	public void log(String msg, Object... args) {
		log(Level.INFO, msg, args);
	}

}
