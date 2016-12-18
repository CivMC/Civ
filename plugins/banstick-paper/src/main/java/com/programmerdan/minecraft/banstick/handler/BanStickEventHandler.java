package com.programmerdan.minecraft.banstick.handler;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Base handler for setting up event captures. Like people logging in who are about to get BanSticked.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
 *
 */
public class BanStickEventHandler {
	public BanStickEventHandler(FileConfiguration config) {
		// setup.
		
		registerEvents();
	}
	
	public void registerEvents() {
		
	}
}
