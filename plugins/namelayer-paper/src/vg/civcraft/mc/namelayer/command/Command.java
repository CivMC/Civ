package vg.civcraft.mc.namelayer.command;

import org.bukkit.command.CommandSender;

public interface Command {
	public boolean execute(CommandSender sender, String[] args);
	public String getName();
	public String getDescription();
	public String getUsage();
	public String getIdentifier();
	public int getMaxArguments();
	public int getMinArguments();
}
