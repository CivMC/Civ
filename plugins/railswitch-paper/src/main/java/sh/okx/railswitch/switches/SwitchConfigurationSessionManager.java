package sh.okx.railswitch.switches;

import com.google.common.base.Strings;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;
import sh.okx.railswitch.RailSwitchPlugin;
import sh.okx.railswitch.storage.RailSwitchKey;
import sh.okx.railswitch.storage.RailSwitchRecord;
import sh.okx.railswitch.storage.RailSwitchStorage;

/**
 * Manages chat-based destination editing sessions for detector rails.
 */
public final class SwitchConfigurationSessionManager implements Listener {

    private static final Duration SESSION_TIMEOUT = Duration.ofSeconds(60);

    private final RailSwitchPlugin plugin;
    private final Map<UUID, Session> sessions;

    public SwitchConfigurationSessionManager(RailSwitchPlugin plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupSessions();
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    public void shutdown() {
        for (UUID uuid : new ArrayList<>(sessions.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.YELLOW + "Rail switch editing session closed.");
            }
        }
        sessions.clear();
    }

    public void beginSession(Player player, Block detectorRail) {
        if (player == null || detectorRail == null) {
            return;
        }
        RailSwitchStorage storage = plugin.getRailSwitchStorage();
        if (storage == null) {
            player.sendMessage(ChatColor.RED + "Rail switch storage is not available.");
            return;
        }
        RailSwitchRecord record = storage.get(detectorRail).orElse(null);
        String header = record != null ? record.getHeader() : SwitchType.NORMAL.getTag();
        List<String> destinations = record != null ? new ArrayList<>(record.getLines()) : new ArrayList<>();
        Session session = new Session(RailSwitchKey.from(detectorRail), header, destinations);
        sessions.put(player.getUniqueId(), session);
        sendPrompt(player, destinations);
    }

    public boolean isEditing(Player player) {
        return player != null && sessions.containsKey(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Session session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        event.setCancelled(true);
        sessions.remove(player.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> handleInput(player, session, event.getMessage()));
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event) {
        validateToolLater(event.getPlayer());
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        validateToolLater(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
    }

    public void cancelSessionsFor(RailSwitchKey key, String reason) {
        sessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (session.getKey().equals(key)) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline() && !Strings.isNullOrEmpty(reason)) {
                    player.sendMessage(ChatColor.YELLOW + reason);
                }
                return true;
            }
            return false;
        });
    }

    private void handleInput(Player player, Session session, String message) {
        if (Strings.isNullOrEmpty(message)) {
            player.sendMessage(ChatColor.RED + "No destinations provided; editing session closed.");
            return;
        }
        String trimmed = message.trim();
        if (trimmed.equalsIgnoreCase("cancel") || trimmed.equalsIgnoreCase("exit")) {
            player.sendMessage(ChatColor.YELLOW + "Rail switch editing cancelled.");
            return;
        }
        String[] tokens = trimmed.split("\\s+");
        boolean modified = session.applyTokens(tokens);
        RailSwitchStorage storage = plugin.getRailSwitchStorage();
        if (storage != null && modified) {
            storage.upsert(session.getKey(), session.getHeader(), session.getDestinations());
        }
        sendFinalList(player, session.getDestinations(), modified);
    }

    private void sendPrompt(Player player, List<String> destinations) {
        player.sendMessage(ChatColor.AQUA + "Editing rail destinations. Reply once with space-separated tokens.");
        player.sendMessage(ChatColor.AQUA + "Use 'name' to add, '!name' to toggle exclusion, '-name' to remove, or 'cancel' to exit.");
        sendCurrentList(player, destinations);
    }

    private void sendCurrentList(Player player, List<String> destinations) {
        if (destinations.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No destinations configured.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Current destinations: " + String.join(" ", destinations));
        }
    }

    private void sendFinalList(Player player, List<String> destinations, boolean modified) {
        if (destinations.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + (modified ? "No destinations remain." : "No destinations configured."));
        } else {
            String prefix = modified ? ChatColor.GREEN + "Updated destinations: " : ChatColor.GREEN + "Current destinations: ";
            player.sendMessage(prefix + String.join(" ", destinations));
        }
        player.sendMessage(ChatColor.YELLOW + "Editing session closed.");
    }

    private void cleanupSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                return true;
            }
            if (!isHoldingConfigurationTool(player)) {
                player.sendMessage(ChatColor.YELLOW + "Stopped editing; hold the configuration tool to edit again.");
                return true;
            }
            if (entry.getValue().isExpired(now)) {
                player.sendMessage(ChatColor.YELLOW + "Rail switch editing session timed out.");
                return true;
            }
            return false;
        });
    }

    private void validateToolLater(Player player) {
        if (player == null) {
            return;
        }
        if (!sessions.containsKey(player.getUniqueId())) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!isHoldingConfigurationTool(player)) {
                sessions.remove(player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW + "Rail switch editing session closed; configuration tool no longer held.");
            }
        });
    }

    private boolean isHoldingConfigurationTool(Player player) {
        if (player == null) {
            return false;
        }
        Material tool = plugin.getSwitchConfiguration() != null ? plugin.getSwitchConfiguration().getToolMaterial() : null;
        if (tool == null) {
            return false;
        }
        return (player.getInventory().getItemInMainHand() != null
            && player.getInventory().getItemInMainHand().getType() == tool)
            || (player.getInventory().getItemInOffHand() != null
            && player.getInventory().getItemInOffHand().getType() == tool);
    }

    private static final class Session {

        private final RailSwitchKey key;
        private final String header;
        private final List<String> destinations;
        private Instant expiresAt;

        Session(RailSwitchKey key, String header, List<String> destinations) {
            this.key = key;
            this.header = header;
            this.destinations = destinations == null ? new ArrayList<>() : new ArrayList<>(destinations);
            refresh();
        }

        RailSwitchKey getKey() {
            return key;
        }

        String getHeader() {
            return header;
        }

        List<String> getDestinations() {
            return Collections.unmodifiableList(destinations);
        }

        void refresh() {
            expiresAt = Instant.now().plus(SESSION_TIMEOUT);
        }

        boolean isExpired(Instant now) {
            return now.isAfter(expiresAt);
        }

        boolean applyTokens(String[] tokens) {
            boolean modified = false;
            for (String rawToken : tokens) {
                if (Strings.isNullOrEmpty(rawToken)) {
                    continue;
                }
                String token = rawToken.trim();
                if (token.isEmpty()) {
                    continue;
                }
                if (token.equalsIgnoreCase("cancel") || token.equalsIgnoreCase("exit")) {
                    continue;
                }
                if (token.startsWith("-") && token.length() > 1) {
                    if (removeDestination(token.substring(1))) {
                        modified = true;
                    }
                    continue;
                }
                if (token.startsWith("!") && token.length() > 1) {
                    if (toggleDestination(token.substring(1))) {
                        modified = true;
                    }
                    continue;
                }
                if (addDestination(token)) {
                    modified = true;
                }
            }
            return modified;
        }

        private boolean addDestination(String destination) {
            String value = sanitize(destination);
            if (value.isEmpty()) {
                return false;
            }
            int positiveIndex = findIndex(value);
            if (positiveIndex >= 0) {
                String existing = destinations.get(positiveIndex);
                if (existing.startsWith("!")) {
                    destinations.set(positiveIndex, existing.substring(1));
                    return true;
                }
                return false;
            }
            int negativeIndex = findIndex("!" + value);
            if (negativeIndex >= 0) {
                destinations.set(negativeIndex, value);
                return true;
            }
            destinations.add(value);
            return true;
        }

        private boolean removeDestination(String destination) {
            String value = sanitize(destination);
            if (value.isEmpty()) {
                return false;
            }
            int positiveIndex = findIndex(value);
            if (positiveIndex >= 0) {
                destinations.remove(positiveIndex);
                return true;
            }
            int negativeIndex = findIndex("!" + value);
            if (negativeIndex >= 0) {
                destinations.remove(negativeIndex);
                return true;
            }
            return false;
        }

        private boolean toggleDestination(String destination) {
            String value = sanitize(destination);
            if (value.isEmpty()) {
                return false;
            }
            int positiveIndex = findIndex(value);
            if (positiveIndex >= 0) {
                String existing = destinations.get(positiveIndex);
                if (existing.startsWith("!")) {
                    destinations.set(positiveIndex, existing.substring(1));
                } else {
                    destinations.set(positiveIndex, "!" + existing);
                }
                return true;
            }
            int negativeIndex = findIndex("!" + value);
            if (negativeIndex >= 0) {
                String existing = destinations.get(negativeIndex);
                destinations.set(negativeIndex, existing.substring(1));
                return true;
            }
            destinations.add("!" + value);
            return true;
        }

        private int findIndex(String value) {
            for (int i = 0; i < destinations.size(); i++) {
                if (destinations.get(i).equalsIgnoreCase(value)) {
                    return i;
                }
            }
            return -1;
        }

        private String sanitize(String destination) {
            return destination == null ? "" : destination.trim();
        }
    }
}
