package vg.civcraft.mc.civmodcore.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class PlayerCommand implements Command{

	private String name = "";
	private String description = "";
	private String usage = "";
	private String identifier = "";
	private int min = 0;
	private int max = 0;
	
	public PlayerCommand(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getUsage() {
		return usage;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getMinArguments() {
		return min;
	}

	public int getMaxArguments() {
		return max;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setDescription(String description){
		Bukkit.getPluginCommand(identifier).setDescription(description);
		this.description = description;
	}
	
	public void setUsage(String usage){
		Bukkit.getPluginCommand(identifier).setUsage(usage);
		this.usage = usage;
	}
	
	public void setIdentifier(String identifier){
		this.identifier = identifier;
	}
	
	public void setArguments(int min, int max){
		this.min = min;
		this.max = max;
	}
	
	public boolean sendPlayerMessage(Player p, String m, boolean flag) {
		p.sendMessage(m);
		return flag;
	}
}