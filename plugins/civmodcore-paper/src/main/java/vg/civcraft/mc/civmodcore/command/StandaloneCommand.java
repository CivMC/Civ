package vg.civcraft.mc.civmodcore.command;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.ratelimiting.RateLimiter;

public abstract class StandaloneCommand {
	
	protected int minArgs = 0;
	protected int maxArgs = Integer.MAX_VALUE;
	protected boolean mustBePlayer = false;
	protected boolean mustBeConsole = false;
	protected RateLimiter rateLimiter;
	protected RateLimiter tabCompletionRateLimiter;

	public abstract boolean execute(CommandSender sender, String[] args);

	public abstract List<String> tabComplete(CommandSender sender, String[] args);
	
	public String getIdentifier() {
		Class<? extends StandaloneCommand> pluginClass = this.getClass();
		CivCommand annot = pluginClass.getAnnotation(CivCommand.class);
		if (annot == null) {
			return null;
		}
		return annot.id();
	}
	
	boolean isRateLimitedToExecute(Player p) {
		if (rateLimiter == null) {
			return false;
		}
		return rateLimiter.pullToken(p);
	}
	
	boolean isRateLimitedToTabComplete(Player p) {
		if (tabCompletionRateLimiter == null) {
			return false;
		}
		return tabCompletionRateLimiter.pullToken(p);
	}
	
	boolean hasTooManyArgs(int argLength) {
		return argLength > maxArgs;
	}
	
	boolean hasTooFewArgs(int argLength) {
		return argLength < minArgs;
	}
	
	void setMaxArgs(int maxArgs) {
		this.maxArgs = maxArgs;
	}
	
	void setMinArgs(int minArgs) {
		this.minArgs = minArgs;
	}
	
	void setRateLimiter(RateLimiter limiter) {
		this.rateLimiter = limiter;
	}
	
	void setTabCompletionRateLimiter(RateLimiter limiter) {
		this.tabCompletionRateLimiter = limiter;
	}
	
	void setSenderMustBePlayer(boolean mustBePlayer) {
		this.mustBePlayer = mustBePlayer;
	}
	
	void setSenderMustBeConsole(boolean mustBeConsole) {
		this.mustBeConsole = mustBeConsole;
	}
	
	boolean canBeRunByPlayers() {
		return !mustBeConsole;
	}
	
	boolean canBeRunByConsole() {
		return !mustBePlayer;
	}
	
	protected static List<String> doTabComplete(String arg, Collection<String> candidates, boolean caseSensitive) {
		return doTabComplete(arg, candidates, s -> s ,caseSensitive);
	}
	
	protected static <T> List<String> doTabComplete(String arg, Collection<T> suppliers, Function<T, String> function, boolean caseSensitive) {
		List<String> result = new LinkedList<>();
		if (!caseSensitive) {
			arg = arg.toLowerCase();
		}
		for(T supplier : suppliers) {
			String candidate = function.apply(supplier);
			boolean matches;
			if (caseSensitive) {
				matches = candidate.startsWith(arg);
			}
			else {
				matches = candidate.toLowerCase().startsWith(arg);
			}
			if (matches) {
				result.add(candidate);
			}
		}
		return result;
	}
}
