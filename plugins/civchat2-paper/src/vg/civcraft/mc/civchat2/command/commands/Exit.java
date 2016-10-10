package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class Exit extends PlayerCommand {
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;

	public Exit(String name) {
		super(name);
		setIdentifier("exit");
		setDescription("This command is used to exit all chat channels.");
		setUsage("/exit");
		setArguments(0,0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args){
		chatMan = plugin.getCivChat2Manager();
		if(!(sender instanceof Player)){
			//console man sending chat... 
			sender.sendMessage(ChatStrings.chatMustBePlayer);
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName();

		chatMan.removeChannel(name);
		chatMan.removeGroupChat(name);
		sender.sendMessage(ChatStrings.chatMovedToGlobal);
		return true;	
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}	

}
