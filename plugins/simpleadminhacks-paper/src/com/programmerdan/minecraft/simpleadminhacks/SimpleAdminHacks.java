package com.programmerdan.minecraft.simpleadminhacks;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class SimpleAdminHacks extends JavaPlugin {
	private static SimpleAdminHacks plugin;

	public SimpleAdminHacks() {
	}

	public void onEnable() {
		SimpleAdminHacks.plugin = this;
	}

	public void onDisable() {

	}

	public static SimpleAdminHacks instance() {
		return plugin;
	}
	public static Logger log() {
		return SimpleAdminHacks.plugin.getLogger();
	}
}
