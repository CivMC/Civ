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

	public void postSetup() {
		PluginCommand cmd = Bukkit.getPluginCommand(identifier);
		if (cmd != null) {
			cmd.setDescription(this.description);
			cmd.setUsage(this.usage);
		}
	}

	
	public void setName(String name){
		this.name = name;
	}
	
	public void setDescription(String description){
		this.description = description;
		postSetup();
	}
	
	public void setUsage(String usage){
		this.usage = usage;
		postSetup();
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
