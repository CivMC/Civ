package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

@CommandAlias("nllci")
public class ListCurrentInvites extends BaseCommandMiddle {

	@Syntax("/nllci")
	@Description("List your current invites.")
	public void execute(CommandSender sender) {
		if (!(sender instanceof Player)){
			sender.sendMessage("back off console man");
			return;
		}
		Player p = (Player) sender;
		p.sendMessage(PlayerListener.getNotificationsInStringForm(NameAPI.getUUID(p.getName())));
	}
}
