package vg.civcraft.mc.civmodcore.players;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class PlayerNames implements Listener {

    private static final Set<String> names = new HashSet<>();

    public PlayerNames(Plugin plugin) {
        names.clear();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer[] players = Bukkit.getOfflinePlayers();
            List<String> namesList = Stream.of(players)
                .map(OfflinePlayer::getName)
                .filter(StringUtils::isNotBlank)
                .toList();
            Bukkit.getScheduler().runTask(plugin, () -> {
                names.addAll(namesList);
            });
        });
    }

    @EventHandler(
        priority = EventPriority.MONITOR, // Make sure it happens after NameLayer's AssociationListener
        ignoreCancelled = true
    )
    private void onLogin(
        final @NotNull PlayerLoginEvent event
    ) {
        names.add(event.getPlayer().getName());
    }

    public static @NotNull Collection<String> getPlayerNames() {
        return Collections.unmodifiableSet(names);
    }
}
