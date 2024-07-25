package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

public class ListCurrentInvites extends BaseCommandMiddle {

    @CommandAlias("nllci|listinvites|invites")
    @Description("List your current invites.")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        player.sendMessage(PlayerListener.getNotificationsInStringForm(NameAPI.getUUID(player.getName())));
    }
}
