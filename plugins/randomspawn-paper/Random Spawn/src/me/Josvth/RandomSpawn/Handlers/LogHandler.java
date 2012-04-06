package me.Josvth.RandomSpawn.Handlers;

import java.util.logging.Logger;


import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class LogHandler {

	RandomSpawn plugin;
	Logger logger;
	
	public LogHandler(RandomSpawn instance){
		this.plugin = instance;
		this.logger = Logger.getLogger("Minecraft");
	}
	      
	public void info(String message){
		this.logger.info(this.buildString(message));
	}
	
	public void warn(String message){
		this.logger.warning(this.buildStringWarning(message));
	}
	
	public void debug(String message){
		this.logger.info(this.buildStringDebug(message));
	}
	
	public void playerInfo(Player player, String message){
    	player.sendMessage(ChatColor.AQUA + "[Random Spawn] " + ChatColor.WHITE + message);
    }
	
	private String buildString(String message){
		PluginDescriptionFile pdfile = plugin.getDescription();
		
		return "[" + pdfile.getName() + "]" + " : " + message;
	}
	
    private String buildStringDebug(String message){
        PluginDescriptionFile pdfile = plugin.getDescription(); 
        return "[" + pdfile.getName() + "]" + " (DEBUG): " + message;
    }
	
    private String buildStringWarning(String message){
		PluginDescriptionFile pdfile = plugin.getDescription();
		
		return "[" + pdfile.getName() + "]" + " (WARNING): " + message;
	}
}
