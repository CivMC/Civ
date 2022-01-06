package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

public class ListCurrentInvites extends BaseCommandMiddle {

	@CommandAlias("nllci|listinvites|invites")
	@Description("List your current invites.")
	public void execute(Player sender) {
		Player p = (Player) sender;
		p.sendMessage(PlayerListener.getNotificationsInStringForm(NameAPI.getUUID(p.getName())));
	}
}
