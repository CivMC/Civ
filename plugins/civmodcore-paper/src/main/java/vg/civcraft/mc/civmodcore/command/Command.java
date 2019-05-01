package vg.civcraft.mc.civmodcore.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civmodcore.ratelimiting.RateLimiter;

public interface Command {

	boolean execute(CommandSender sender, String[] args);

	List<String> tabComplete(CommandSender sender, String[] args);

	String getName();

	String getDescription();

	String getUsage();

	String getIdentifier();

	int getMaxArguments();

	int getMinArguments();

	void postSetup();

	boolean getSenderMustBePlayer();

	boolean getErrorOnTooManyArgs();

	void setSender(CommandSender sender);

	void setArgs(String[] args);
	
	RateLimiter getRateLimiter();
	
	RateLimiter getTabCompletionRateLimiter();

}
