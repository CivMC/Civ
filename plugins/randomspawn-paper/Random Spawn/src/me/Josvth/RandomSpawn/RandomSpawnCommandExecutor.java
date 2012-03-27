package me.Josvth.RandomSpawn;

import java.util.List;

import org.bukkit.command.CommandSender;

public abstract class RandomSpawnCommandExecutor {
	
	protected RandomSpawn plugin;
	protected String name;
	
    public void setPlugin(RandomSpawn instance) {
        plugin = instance;
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
