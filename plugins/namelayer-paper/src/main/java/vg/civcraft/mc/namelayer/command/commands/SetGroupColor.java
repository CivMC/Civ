package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;
import java.util.UUID;

public class SetGroupColor extends BaseCommandMiddle {

    @CommandAlias("nlsgc|setgroupcolor")
    @Syntax("<group> <color>")
    @Description("Set the groups color")
    @CommandCompletion("@NL_Groups @ADV_Colors")
    public void execute(Player player, String groupName, String targetColor) {
        UUID uuid = NameAPI.getUUID(player.getName());
        Group group = gm.getGroup(groupName);
        if (groupIsNull(player, groupName, group)) {
            return;
        }

        if (targetColor == null || targetColor.isEmpty()) {
            player.sendRichMessage("<red>You need to enter a value</red>");
            return;
        }

        TextColor color = NamedTextColor.NAMES.value(targetColor);
        if (color == null) {
            color = TextColor.fromHexString(targetColor);
        }
        if (color == null) {
            player.sendRichMessage("<red>The value you entered was not a valid hex value or color.</red>");
            return;
        }
        GroupManager.PlayerType playerType = group.getPlayerType(uuid);
        if (playerType == null) {
            player.sendRichMessage("<red>You do not have access to that group.</red>");
            return;
        }
        if (!gm.hasAccess(group, uuid, PermissionType.getPermission("EDIT_COLOR"))) {
            player.sendRichMessage("<red>You do not have permission to modify " + group.getName() + " </red>");
            return;
        }
        group.setGroupColor(color);
        player.sendMessage(Component.text("You have changed the group color to ", NamedTextColor.GREEN)
            .append(Component.text(group.getName()).color(group.getGroupColor())));
    }

}
