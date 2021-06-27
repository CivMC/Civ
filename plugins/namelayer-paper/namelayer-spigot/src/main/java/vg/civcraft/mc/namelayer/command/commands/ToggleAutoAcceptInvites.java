package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.AutoAcceptHandler;

@CommandAlias("nltaai")
public class ToggleAutoAcceptInvites extends BaseCommandMiddle {

	private AutoAcceptHandler handler = NameLayerPlugin.getAutoAcceptHandler();

	@Syntax("/nltaai")
	@Description("Toggle the acceptance of invites.")
	public void execute(CommandSender sender) {
		if (!(sender instanceof Player)){
			sender.sendMessage("how would this even work");
			return;
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
	}
}
