package vg.civcraft.mc.civduties.configuration;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command {
	private String syntax;
	private Timing timing;
	private Executor executor;
	
	public enum Timing{
		ENABLE,
		DISABLE,
		LOGIN, 
		LOGOUT;
	}
	
	public enum Executor{
		PLAYER,
		CONSOLE;
	}
	
	public Command(String syntax, Timing timing, Executor executor) {
		this.syntax = syntax;
		this.timing = timing;
		this.executor = executor;
	}
	
	public String getSyntax() {
		return syntax;
	}

	public void setSyntax(String syntax) {
		this.syntax = syntax;
	}

	public Timing getTiming() {
		return timing;
	}

	public void setTiming(Timing timing) {
		this.timing = timing;
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void execute(Player player){
		String parsedCommand = (syntax.charAt(0) == '/' ? syntax.substring(1) : syntax)
				.replaceAll("%PLAYER_NAME%", player.getName())
				.replaceAll("%PLAYER_GAMEMODE%", player.getGameMode().toString())
				.replaceAll("%PLAYER_SERVER%", Bukkit.getServer().getName());
		CommandSender sender = player;
		if(executor == Executor.CONSOLE){
			sender = Bukkit.getConsoleSender();
		}
		Bukkit.dispatchCommand(sender, parsedCommand);
	}
}
