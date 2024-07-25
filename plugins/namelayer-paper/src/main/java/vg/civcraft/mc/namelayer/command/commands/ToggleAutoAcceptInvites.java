package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.AutoAcceptHandler;

public class ToggleAutoAcceptInvites extends BaseCommandMiddle {

    private AutoAcceptHandler handler = NameLayerPlugin.getAutoAcceptHandler();

    @CommandAlias("nltaai|autoaccept")
    @Description("Toggle the acceptance of invites.")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        UUID uuid = NameAPI.getUUID(player.getName());
        if (handler.getAutoAccept(uuid)) {
            player.sendMessage(ChatColor.GREEN + "You will no longer automatically accept group requests.");
        } else {
            player.sendMessage(ChatColor.GREEN + "You will automatically accept group requests.");
        }
        handler.toggleAutoAccept(uuid, true);
    }
}
