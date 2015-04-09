package net.minelink.ctplus.listener;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.Tag;
import net.minelink.ctplus.event.PlayerCombatTagEvent;
import net.minelink.ctplus.task.BarUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.UUID;

import static org.bukkit.ChatColor.*;

public final class PlayerListener implements Listener {

    private final CombatTagPlus plugin;

    public PlayerListener(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void addPlayer(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Send a Player List of all the NPCs for client visibility reasons
        plugin.getNpcPlayerHelper().createPlayerList(player);

        // Add player to cache
        plugin.addPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void removePlayer(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove all NPCs from the Player List for player
        plugin.getNpcPlayerHelper().removePlayerList(player);

        // Remove player from cache
        plugin.removePlayer(player);
    }

    @EventHandler
    public void showBar(PlayerJoinEvent event) {
        BarUpdateTask.run(plugin, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void broadcastKill(PlayerDeathEvent event) {
        // Do nothing with kill message is blank
        String message = plugin.getSettings().getKillMessage();
        if (message == null || message.isEmpty()) return;

        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();

        // Player is NPC, determine actual identity
        if (plugin.getNpcPlayerHelper().isNpc(player)) {
            playerId = plugin.getNpcPlayerHelper().getIdentity(player).getId();
        }

        // Do nothing if player isn't tagged
        Tag tag = plugin.getTagManager().getTag(playerId);
        if (tag == null) return;

        String victim = tag.getVictimName();
        String attacker = tag.getAttackerName();

        // Do nothing if there is one player missing
        if (victim == null || attacker == null) return;

        // Sometimes the victim tags the attacker and then dies by another cause like fire
        // In these cases, the tag manager sees the attacker as the victim
        // This should fix it by swapping victim/attacker when victim doesn't match dead player
        if (!tag.getVictimId().equals(playerId)) {
            victim = tag.getAttackerName();
            attacker = tag.getVictimName();
        }

        // Broadcast kill message
        message = message.replace("{victim}", victim).replace("{attacker}", attacker);
        Bukkit.broadcast(message, "ctplus.notify.kill");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void disableCommand(PlayerCommandPreprocessEvent event) {
        // Do nothing if player has bypass permission
        Player player = event.getPlayer();
        if (player.hasPermission("ctplus.bypass.command")) return;

        // Do nothing if player isn't even combat tagged
        if (!plugin.getTagManager().isTagged(player.getUniqueId())) return;

        String message = event.getMessage().toLowerCase();

        // Is player using a denied command?
        for (String command : plugin.getSettings().getDisabledCommands()) {
            String c = "/" + command.toLowerCase();
            if (!message.equals(c) && !message.startsWith(c + " ")) continue;

            // Cancel command
            event.setCancelled(true);
            player.sendMessage(AQUA + c + RED + " is disabled in combat.");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void disableBlockEdit(BlockBreakEvent event) {
        // Do nothing if block edits are allowed in combat
        if (!plugin.getSettings().disableBlockEdit()) return;

        // Do nothing if player has bypass permission
        Player player = event.getPlayer();
        if (player.hasPermission("ctplus.bypass.blockedit")) return;

        // Do nothing if player isn't even combat tagged
        if (!plugin.getTagManager().isTagged(player.getUniqueId())) return;

        // Cancel block edit
        event.setCancelled(true);
        player.sendMessage(AQUA + "Block editing " + RED + " is disabled in combat.");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void disableBlockEdit(BlockPlaceEvent event) {
        // Do nothing if block edits are allowed in combat
        if (!plugin.getSettings().disableBlockEdit()) return;

        // Do nothing if player has bypass permission
        Player player = event.getPlayer();
        if (player.hasPermission("ctplus.bypass.blockedit")) return;

        // Do nothing if player isn't even combat tagged
        if (!plugin.getTagManager().isTagged(player.getUniqueId())) return;

        // Cancel block edit
        event.setCancelled(true);
        player.sendMessage(AQUA + "Block editing " + RED + " is disabled in combat.");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void disableEnderpearls(PlayerInteractEvent event) {
        // Do nothing if enderpearls are allowed in combat
        if (!plugin.getSettings().disableEnderpearls()) return;

        // Do nothing if player has bypass permission
        Player player = event.getPlayer();
        if (player.hasPermission("ctplus.bypass.enderpearl")) return;

        // Do nothing if player isn't even combat tagged
        if (!plugin.getTagManager().isTagged(player.getUniqueId())) return;

        // Do nothing if player is not right clicking
        Action action = event.getAction();
        if (!(action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR))) return;

        // Do nothing if player is not holding an enderpearl
        if (!player.getItemInHand().getType().equals(Material.ENDER_PEARL)) return;

        // Cancel enderpearl throw
        event.setCancelled(true);
        player.sendMessage(AQUA + "Enderpearls " + RED + " are disabled in combat.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void disableFlying(PlayerCombatTagEvent event) {
        // Do nothing if flying is allowed in combat
        if (!plugin.getSettings().disableFlying()) return;

        Player p;

        // Disable flying for victim
        p = event.getVictim();
        if (p != null && p.isFlying() && !p.hasPermission("ctplus.bypass.flying")) {
            p.setAllowFlight(false);
            p.sendMessage(AQUA + "Flying " + RED + "is disabled in combat.");
        }

        // Disable flying for attacker
        p = event.getAttacker();
        if (p != null && p.isFlying() && !p.hasPermission("ctplus.bypass.flying")) {
            p.setAllowFlight(false);
            p.sendMessage(AQUA + "Flying " + RED + "is disabled in combat.");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void disableFlying(PlayerToggleFlightEvent event) {
        // Do nothing if flying is allowed in combat
        if (!plugin.getSettings().disableFlying()) return;

        // Do nothing if player is flying
        final Player player = event.getPlayer();
        if (player.isFlying()) return;

        // Do nothing if player isn't even combat tagged
        if (!plugin.getTagManager().isTagged(player.getUniqueId())) return;

        // Do nothing if player has bypass permission
        if (player.hasPermission("ctplus.bypass.flying")) return;

        // Cancel player's flight
        player.setAllowFlight(false);

        // Cancel the event and inform the player
        event.setCancelled(true);
        player.sendMessage(AQUA + "Flying " + RED + "is disabled in combat.");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void disableTeleportation(PlayerTeleportEvent event) {
        // Do nothing if teleportation is allowed in combat
        if (!plugin.getSettings().disableTeleportation()) return;

        // Do nothing if teleportation caused by enderpearl
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        // Do nothing if player isn't even combat tagged
        final Player player = event.getPlayer();
        if (!plugin.getTagManager().isTagged(player.getUniqueId())) return;

        // Do nothing if player has bypass permission
        if (player.hasPermission("ctplus.bypass.teleport")) return;

        // Cancel the event and inform the player
        event.setCancelled(true);
        player.sendMessage(AQUA + "Teleportation " + RED + "is disabled in combat.");
    }

}
