package vg.civcraft.mc.civchat2.command;

import org.bukkit.command.CommandSender;

/*
 * @author jjj5311
 * Interface for commands
 */
public interface CivChat2Command {
	public boolean execute(CommandSender sender, String[] args);
	public String getName();
	public String getDescription();
	public String getUsage();
	public String getIdentifier();
	public int getMaxArguments();
	public int getMinArguments();
}
