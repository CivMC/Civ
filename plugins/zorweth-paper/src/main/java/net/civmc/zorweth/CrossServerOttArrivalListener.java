package net.civmc.zorweth;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.civmc.zorweth.database.RocketTransferDao.CrossServerOttArrival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

public final class CrossServerOttArrivalListener implements Listener {

    private final ZorwethPlugin plugin;
    private final CrossServerOttManager manager;
    private final Map<UUID, CrossServerOttArrival> cachedArrivals = new ConcurrentHashMap<>();

    public CrossServerOttArrivalListener(final ZorwethPlugin plugin, final CrossServerOttManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
        final CrossServerOttArrival arrival;
        try {
            arrival = this.manager.getArrival(event.getUniqueId());
        } catch (final SQLException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to look up cross-server OTT arrival", exception);
            event.kickMessage(Component.text("Unable to process OTT arrival at this time, please try again later"));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }

        if (arrival != null) {
            this.cachedArrivals.put(event.getUniqueId(), arrival);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player requester = event.getPlayer();
        final CrossServerOttArrival arrival = this.cachedArrivals.remove(requester.getUniqueId());
        if (arrival == null) {
            return;
        }

        requester.getPersistentDataContainer().set(RocketTransferKeys.OTT_JOIN, PersistentDataType.BOOLEAN, true);
        Bukkit.getScheduler().runTask(this.plugin, () -> completeArrival(requester.getUniqueId(), arrival));
    }

    private void completeArrival(final UUID requesterId, final CrossServerOttArrival arrival) {
        final Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null) {
            return;
        }

        final String arrivalMarker = getArrivalMarker(arrival);
        if (arrivalMarker.equals(requester.getPersistentDataContainer()
            .get(RocketTransferKeys.OTT_APPLIED_ARRIVAL, PersistentDataType.STRING))) {
            clearArrival(requesterId);
            return;
        }

        final World world = Bukkit.getWorld(arrival.targetWorld());
        if (world == null) {
            requester.sendMessage(Component.text("Your OTT target location is no longer available.", NamedTextColor.RED));
            return;
        }

        requester.getPersistentDataContainer().set(RocketTransferKeys.OTT_APPLIED_ARRIVAL,
            PersistentDataType.STRING, arrivalMarker);
        if (!requester.teleport(new Location(world, arrival.targetX(), arrival.targetY(), arrival.targetZ(),
            arrival.targetYaw(), arrival.targetPitch()))) {
            requester.getPersistentDataContainer().remove(RocketTransferKeys.OTT_APPLIED_ARRIVAL);
            requester.sendMessage(Component.text("Unable to complete OTT arrival. Please contact an admin.",
                NamedTextColor.RED));
            return;
        }
        clearArrival(requesterId);
        final Player target = Bukkit.getPlayer(arrival.targetUuid());
        requester.sendMessage(Component.text("Your cross-server OTT has completed.", NamedTextColor.GREEN));
        if (target != null) {
            target.sendMessage(Component.text(requester.getName() + " has been teleported to your accepted location!",
            NamedTextColor.GREEN));
        }
    }

    private void clearArrival(final UUID requesterId) {
        this.manager.clearArrivalAsync(requesterId);
    }

    private String getArrivalMarker(final CrossServerOttArrival arrival) {
        return arrival.targetUuid() + ":" + arrival.expiresAtMillis();
    }
}
