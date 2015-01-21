package vg.civcraft.mc.namelayer.command.commands;

import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;

public class ToggleAutoAcceptInvites extends PlayerCommand{

	private GroupManagerDao db = NameLayerPlugin.getGroupManagerDao();
	public ToggleAutoAcceptInvites(String name) {
		super(name);
		setDescription("This command is used to toggle the acceptance of invites.");
		setUsage("/nltaai");
		setIdentifier("nltaai");
		setArguments(1,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("how would this even work");
			return true;
		}
		Player p = (Player) sender;
		UUID uuid = NameAPI.getUUID(p.getName());
		if (db.shouldAutoAcceptGroups(uuid)){
			db.removeAutoAcceptGroup(uuid);
			p.sendMessage(ChatColor.GREEN + "You will no longer automatically accept group requests.");
		}
		else {
			db.autoAcceptGroups(uuid);
			p.sendMessage(ChatColor.GREEN + "You will automatically accept group requests.");
		}
		return true;
	}

}
