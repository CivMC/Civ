package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.mercury.MercuryAPI;

public class SayAll extends PlayerCommand {
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	
	public SayAll(String name) {
		super(name);
		setIdentifier("sayall");
		setDescription("This command is used to broadcast a message to all shards");
		setUsage("/sayall (message)");
		setArguments(1,100);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args){
		StringBuilder sb = new StringBuilder();
		for(String add: args) {
			sb.append(add);
			sb.append(" ");
		}
		String msg = sb.toString();
		plugin.getServer().broadcastMessage(ChatColor.GOLD+ msg);
		logger.info("Sending global message: "+msg);
		if (plugin.isMercuryEnabled()) { 
			MercuryAPI.sendGlobalMessage("bc|" + msg, "civchat2");
		}
		else {
			sender.sendMessage(ChatColor.RED+ "Mercury is not enabled, message could not be broadcasted");
		}
		return true;
		
		
	}
	
	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}	

}
