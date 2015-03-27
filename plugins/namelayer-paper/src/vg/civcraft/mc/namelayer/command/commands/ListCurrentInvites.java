package vg.civcraft.mc.namelayer.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

import java.util.List;

public class ListCurrentInvites extends PlayerCommand{

	public ListCurrentInvites(String name) {
		super(name);
		setIdentifier("nllci");
		setDescription("List your current invites.");
		setUsage("/nllci");
		setArguments(0,0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("back off console man");
			return true;
		}
		Player p = (Player) sender;
		p.sendMessage(PlayerListener.getNotificationsInStringForm(NameAPI.getUUID(p.getName())));
		return true;
	}
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
