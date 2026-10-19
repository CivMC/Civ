package com.untamedears.jukealert.broadcaster;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.util.JASettingsManager;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.group.Group;

public final class RemoteSnitchAlertDispatcher {

    private RemoteSnitchAlertDispatcher() {
    }

    public static void send(final RemoteSnitchAlert alert) {
        final Group group = GroupManager.getGroup(alert.groupName());
        if (group == null) {
            return;
        }

        final JASettingsManager settings = JukeAlert.getInstance().getSettingsManager();
        for (final UUID uuid : group.getAllMembers()) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }
            if (settings.doesIgnoreAllAlerts(uuid)) {
                continue;
            }
            if (settings.doesIgnoreAlert(alert.groupName(), uuid)) {
                continue;
            }
            if (!groupHasAlertPermission(group, uuid)) {
                continue;
            }

            final Component component = createMessage(alert);
            if (settings.monocolorAlerts(uuid)) {
                player.sendMessage(Component.text(PlainTextComponentSerializer.plainText().serialize(component), NamedTextColor.AQUA));
            } else {
                player.sendMessage(component);
            }
        }
    }

    private static boolean groupHasAlertPermission(final Group group, final UUID uuid) {
        return NameLayerAPI.getGroupManager()
            .hasAccess(group, uuid, JukeAlertPermissionHandler.getSnitchAlerts());
    }

    private static Component createMessage(final RemoteSnitchAlert alert) {
        return getActionName(alert.actionIdentifier())
            .append(Component.text("  "))
            .append(Component.text(alert.playerName(), NamedTextColor.GREEN))
            .append(Component.text("  "))
            .append(createSnitchComponent(alert))
            .append(Component.text("  "))
            .append(Component.text(String.format("[%s %d %d %d]", alert.worldName(), alert.x(), alert.y(), alert.z()),
                NamedTextColor.YELLOW));
    }

    private static Component getActionName(final String actionIdentifier) {
        return switch (actionIdentifier) {
            case "LOGIN" -> Component.text("Login", NamedTextColor.GOLD, TextDecoration.BOLD);
            case "LOGOUT" -> Component.text("Logout", NamedTextColor.GOLD, TextDecoration.BOLD);
            default -> Component.text("Enter", NamedTextColor.GOLD);
        };
    }

    private static Component createSnitchComponent(final RemoteSnitchAlert alert) {
        final String displayName = alert.snitchName().isEmpty() ? alert.snitchTypeName() : alert.snitchName();
        return Component.text(displayName, NamedTextColor.AQUA)
            .hoverEvent(createSnitchHover(alert));
    }

    private static Component createSnitchHover(final RemoteSnitchAlert alert) {
        Component hover = Component.text("Location: ", NamedTextColor.GOLD)
            .append(Component.text(String.format("(%s) [%d %d %d]", alert.worldName(), alert.x(), alert.y(), alert.z()),
                NamedTextColor.AQUA));
        if (!alert.snitchName().isEmpty()) {
            hover = hover.append(Component.newline())
                .append(Component.text("Name: ", NamedTextColor.GOLD))
                .append(Component.text(alert.snitchName(), NamedTextColor.AQUA));
        }
        return hover.append(Component.newline())
            .append(Component.text("Group: ", NamedTextColor.GOLD))
            .append(Component.text(alert.groupName(), NamedTextColor.AQUA));
    }
}
