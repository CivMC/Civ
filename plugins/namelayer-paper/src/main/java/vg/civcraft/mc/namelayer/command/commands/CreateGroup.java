package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class CreateGroup extends BaseCommandMiddle {

    @CommandAlias("nlcg|nlcreategroup|creategroup|create|ctcreate|creategroup")
    @Syntax("<group> [password]")
    @Description("Create a group (Public or Private). Password is optional.")
    public void execute(Player sender, String groupName, @Optional String userPassword) {
        String name = groupName;
        final boolean adminOverride = sender.isOp() || sender.hasPermission("namelayer.admin");
        final int groupLimit = NameLayerPlugin.getInstance().getGroupLimit();

        //enforce regulations on the name
        if (name.length() > 32) {
            sender.sendMessage(ChatColor.RED + "The group name is not allowed to contain more than 32 characters");
            return;
        }
        Charset latin1 = StandardCharsets.ISO_8859_1;
        boolean invalidChars = !latin1.newEncoder().canEncode(name);
        //cant allow them to hurt mercury :(
        if (name.contains("|")) {
            invalidChars = true;
        }

        for (char c : name.toCharArray()) {
            if (Character.isISOControl(c)) {
                invalidChars = true;
                break;
            }
        }

        if (invalidChars) {
            sender.sendMessage(ChatColor.RED + "You used characters, which are not allowed");
            return;
        }

        if (GroupManager.getGroup(name) != null) {
            sender.sendMessage(ChatColor.RED + "That group is already taken. Try another unique group name.");
            return;
        }
        final UUID uuid = NameLayerAPI.getUUID(sender.getName());
        Group g = new Group(name, uuid, false, userPassword, -1, System.currentTimeMillis(), "GRAY");
        gm.createGroupAsync(g, createdGroup -> {
            if (createdGroup != null) {
                NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group {0} creation complete resulting in group id: {1}",
                    new Object[]{createdGroup.getName(), createdGroup.getGroupId()});
            }

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return;
            }
            if (createdGroup == null) {
                player.sendMessage(ChatColor.RED + "That group is already taken, you have reached the group limit, or creation failed.");
            } else {
                player.sendMessage(ChatColor.GREEN + "The group " + createdGroup.getName() + " was successfully created.");
            }
        }, false, groupLimit, adminOverride);
        sender.sendMessage(ChatColor.GREEN + "Group creation request is in process.");
    }
}
