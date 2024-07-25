package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class SetPassword extends BaseCommandMiddle {

    @CommandAlias("nlsp|setpassword")
    @Syntax("<group> <password>")
    @Description("Set a password on a group.")
    @CommandCompletion("@NL_Groups")
    public void execute(CommandSender sender, String groupName, String userPassword) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        UUID uuid = NameAPI.getUUID(player.getName());
        Group g = gm.getGroup(groupName);
        if (groupIsNull(sender, groupName, g)) {
            return;
        }

        PlayerType pType = g.getPlayerType(uuid);
        if (pType == null) {
            player.sendMessage(ChatColor.RED + "You do not have access to that group.");
            return;
        }

        if (!gm.hasAccess(g, uuid, PermissionType.getPermission("PASSWORD"))) {
            player.sendMessage(ChatColor.RED + "You do not have permission to modify that group.");
            return;
        }

        String password = null;
        if (userPassword != null)
            password = userPassword;
        g.setPassword(password);
        player.sendMessage(ChatColor.GREEN + "Password has been successfully set to: " + g.getPassword());
    }
}
