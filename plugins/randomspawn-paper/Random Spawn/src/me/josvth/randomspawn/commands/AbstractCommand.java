package me.josvth.randomspawn.commands;

import java.util.List;

import me.josvth.randomspawn.RandomSpawn;

import org.bukkit.command.CommandSender;

public abstract class AbstractCommand {
	
	protected final RandomSpawn plugin;
	protected final String name;
	
	public AbstractCommand(RandomSpawn instance, String commandName){
		plugin = instance;
		name = commandName;
	}
	    
    public abstract boolean onCommand(CommandSender sender, List<String> args);
    
    public boolean onConsoleCommand(CommandSender sender, List<String> args){
    	return onCommand(sender,args);
    }
    
    public String getName() {
		return name;
	}
   
	public String getDescription() {
		try {
			return plugin.getCommand("rs " + name).getDescription(); 
		} catch (NullPointerException e){
			return null;
		}
	}

	public String getUsage() {
		try {
			return plugin.getCommand("rs " + name).getUsage();
		} catch (NullPointerException e){
			return null;
		}
	}
	
    public String getPermission() {
		try {
			return plugin.getCommand("rs " + name).getPermission(); 
		} catch (NullPointerException e){
			return null;
		}
	}
    
	public List<String> getAliases() {
		try {
			return plugin.getCommand("rs " + name).getAliases();
		} catch (NullPointerException e) {
			return null;
		}
	}
}
