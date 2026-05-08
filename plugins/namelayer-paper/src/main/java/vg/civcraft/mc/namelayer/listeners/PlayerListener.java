package vg.civcraft.mc.namelayer.listeners;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.cache.NameLayerGroupCache;
import vg.civcraft.mc.namelayer.group.Group;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        if (!p.hasPlayedBefore()) {
            handleFirstJoin(p);
        }

        boolean shouldAutoAccept = NameLayerPlugin.getAutoAcceptHandler().getAutoAccept(uuid);
        if (shouldAutoAccept) {
            return;
        }

        String x = "You have been invited to the following groups while you were away. You can accept each invitation by using the command: /nlag [groupname].  ";

        for (Group g : getNotifications(uuid)) {
            x += g.getName() + ", ";
        }
        x = x.substring(0, x.length() - 2);
        x += ".";
        p.sendMessage(ChatColor.YELLOW + x);
    }

    public static List<Group> getNotifications(UUID uuid) {
        final NameLayerGroupCache cache = NameLayerPlugin.getGroupCache();
        if (uuid == null || cache == null) {
            return Collections.emptyList();
        }
        return cache.snapshotGroups().stream()
            .filter(group -> group.getInvite(uuid) != null)
            .sorted((first, second) -> String.CASE_INSENSITIVE_ORDER.compare(first.getName(), second.getName()))
            .toList();
    }

    public static String getNotificationsInStringForm(UUID u) {
        String groups = "";
        for (Group g : getNotifications(u))
            groups += g.getName() + ", ";
        if (groups.length() == 0)
            return ChatColor.GREEN + "You have no notifications.";
        groups = groups.substring(0, groups.length() - 2);
        groups = ChatColor.GREEN + "Your current groups are: " + groups + ".";
        return groups;
    }

    private void handleFirstJoin(Player p) {
        if (!NameLayerPlugin.createGroupOnFirstJoin()) {
            return;
        }
        if (NameLayerPlugin.getDefaultGroupHandler().getDefaultGroup(p) != null) {
            //assume something went wrong, feel free to chose a random civcraft dev to blame
            return;
        }
        final UUID uuid = p.getUniqueId();
        final String name = p.getName();

        NameLayerAPI.getGroupManager().ensureNewfriendGroupAsync(uuid, name, result -> {
            if (!result.success() || result.group() == null) {
                NameLayerPlugin.log(Level.WARNING,
                    "Newfriend automatic group ensure failed for " + name + " " + uuid + ": " + result.message());
                return;
            }
            final Group group = result.group();
            NameLayerPlugin.getDefaultGroupHandler().cacheDefaultGroup(uuid, group);
            NameLayerPlugin.log(Level.WARNING,
                "Newfriend automatic group ensured for " + group.getName() + " " + uuid);
        });
    }
}
