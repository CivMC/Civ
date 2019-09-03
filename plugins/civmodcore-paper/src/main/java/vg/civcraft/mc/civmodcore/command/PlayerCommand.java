package vg.civcraft.mc.civmodcore.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.ratelimiting.RateLimiter;
import vg.civcraft.mc.civmodcore.ratelimiting.RateLimiting;
import vg.civcraft.mc.civmodcore.util.TextUtil;

@Deprecated
public abstract class PlayerCommand implements Command {

	protected String name = "";
	protected String description = "";
	private String usage = "";
	protected String identifier = "";
	protected int min = 0;
	protected int max = 0;
	protected boolean senderMustBePlayer = false;
	protected boolean errorOnTooManyArgs = true;
	protected CommandSender sender;
	protected String[] args;
	protected RateLimiter rateLimiter;
	protected RateLimiter tabCompletionRateLimiter;

	public PlayerCommand(String name) {
		this.name = name;
	}

	@Override
	@Deprecated
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
	@Deprecated
	public void setArgs(String[] args) {
		this.args = args;
	}

	@Deprecated
	public String[] getArgs() {
		return args;
	}

	@Override
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

	public void setSenderMustBePlayer(boolean senderMustBePlayer) {
		this.senderMustBePlayer = senderMustBePlayer;
	}

	public void setErrorOnTooManyArgs(boolean errorOnTooManyArgs) {
		this.errorOnTooManyArgs = errorOnTooManyArgs;
	}
	
	public void setRateLimitingBehavior(int limit, int refillAmount, int refillIntervallInSeconds) {
		this.rateLimiter = RateLimiting.createRateLimiter("COMMAND_" + identifier, 
				limit, limit, refillAmount, ((long) refillIntervallInSeconds) * 1000);
	}
	
	public void setTabCompletionRateLimitingBehavior(int limit, int refillAmount, int refillIntervallInSeconds) {
		this.tabCompletionRateLimiter = RateLimiting.createRateLimiter("COMMAND_" + identifier, 
				limit, limit, refillAmount, ((long) refillIntervallInSeconds) * 1000);
	}
	
	public RateLimiter getRateLimiter() {
		return rateLimiter;
	}
	
	public RateLimiter getTabCompletionRateLimiter() {
		return tabCompletionRateLimiter;
	}

	public Player player() {
		return (Player) sender;
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
