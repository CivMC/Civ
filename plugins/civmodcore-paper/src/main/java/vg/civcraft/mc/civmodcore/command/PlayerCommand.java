package vg.civcraft.mc.civmodcore.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

public abstract class PlayerCommand implements Command {

	private String name = "";
	private String description = "";
	private String usage = "";
	private String identifier = "";
	private int min = 0;
	private int max = 0;
	private boolean senderMustBeConsole = false;
	private CommandSender sender;

	public PlayerCommand(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getUsage() {
		return usage;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int getMinArguments() {
		return min;
	}

	@Override
	public int getMaxArguments() {
		return max;
	}

	@Override
	public boolean getSenderMustBeConsole() {
		return senderMustBeConsole;
	}
	
	@Override
	public void setSender(CommandSender sender) {
		this.sender = sender;
	}
	
	

	public void postSetup() {
		PluginCommand cmd = Bukkit.getPluginCommand(identifier);
		if (cmd != null) {
			cmd.setDescription(this.description);
			cmd.setUsage(this.usage);
		}
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
		postSetup();
	}

	public void setUsage(String usage) {
		this.usage = usage;
		postSetup();
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setArguments(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public boolean sendPlayerMessage(Player p, String m, boolean flag) {
		p.sendMessage(m);
		return flag;
	}
	
	public void setSenderMustBeConsole(boolean senderMustBeConsole) {
		this.senderMustBeConsole = senderMustBeConsole;
	}
	
	public void messageSender(String msg) {
		sender.sendMessage(msg);
	}
	
	public void messageSender(String msg, Object... args) {
		sender.sendMessage(String.format(msg, args));
	}
	
	public Player me() {
		return (Player)sender;
	}
}
