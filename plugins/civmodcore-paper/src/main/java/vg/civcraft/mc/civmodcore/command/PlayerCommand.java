package vg.civcraft.mc.civmodcore.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.util.TextUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

public abstract class PlayerCommand implements Command {

	private String name = "";
	private String description = "";
	private String usage = "";
	private String identifier = "";
	private int min = 0;
	private int max = 0;
	private boolean senderMustBePlayer = false;
	private boolean errorOnTooManyArgs = true;
	private CommandSender sender;
	private String[] args;

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
	public boolean getSenderMustBePlayer() {
		return senderMustBePlayer;
	}
	
	@Override
	public boolean getErrorOnTooManyArgs() {
		return this.errorOnTooManyArgs;
	}
	
	@Override
	public void setSender(CommandSender sender) {
		this.sender = sender;
	}

	@Override
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	public String[] getArgs() {
		return args;
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
	
	public void setSenderMustBePlayer(boolean senderMustBeConsole) {
		this.senderMustBePlayer = senderMustBeConsole;
	}
	
	public void setErrorOnTooManyArgs(boolean errorOnTooManyArgs) {
		this.errorOnTooManyArgs = errorOnTooManyArgs;
	}
	
	public Player player() {
		return (Player)sender;
	}
	
	public void msg(String msg) {
		sender.sendMessage(parse(msg));
	}
	
	public void msg(String msg, Object... args) {
		sender.sendMessage(parse(msg, args));
	}
	
	public String parse(String text) {
		return TextUtil.parse(text);
	}
	
	public String parse(String text, Object... args) {
		return TextUtil.parse(text, args);
	}
}
