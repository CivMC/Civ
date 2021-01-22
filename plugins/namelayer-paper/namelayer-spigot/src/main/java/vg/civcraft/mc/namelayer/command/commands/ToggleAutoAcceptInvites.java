package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.group.AutoAcceptHandler;

public class ToggleAutoAcceptInvites extends PlayerCommandMiddle{

	private AutoAcceptHandler handler = NameLayerPlugin.getAutoAcceptHandler();
	public ToggleAutoAcceptInvites(String name) {
		super(name);
		setIdentifier("nltaai");
		setDescription("Toggle the acceptance of invites.");
		setUsage("/nltaai");
		setArguments(0,0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("how would this even work");
			return true;
		}
		Player p = (Player) sender;
		UUID uuid = NameAPI.getUUID(p.getName());
		if (handler.getAutoAccept(uuid)){
			p.sendMessage(ChatColor.GREEN + "You will no longer automatically accept group requests.");
		}
		else {
			p.sendMessage(ChatColor.GREEN + "You will automatically accept group requests.");
		}
		handler.toggleAutoAccept(uuid, true);
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
