package me.Josvth.RandomSpawn;

import java.util.List;

import org.bukkit.command.CommandSender;


public abstract class RandomSpawnCommandExecutor {
	
	protected RandomSpawn plugin;
	protected String name;
	
    public void setPlugin(RandomSpawn instance) {
        this.plugin = instance;
    }
    
    public abstract boolean onCommand(CommandSender sender, List<String> args);
    
    public String getName() {
		return name;
	}

	public String getDescription() {
		try {
			return plugin.getCommand(name).getDescription(); 
		} catch (NullPointerException e){
			return null;
		}
	}

	public String getUsage() {
		try {
			return plugin.getCommand(name).getUsage();
		} catch (NullPointerException e){
			return null;
		}
	}

	public List<String> getAliases() {
		try {
			return plugin.getCommand(name).getAliases();
		} catch (NullPointerException e) {
			return null;
		}
	}
}
