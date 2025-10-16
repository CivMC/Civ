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
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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

    /**
     * Shuts down the session manager, closing all active sessions.
     */
    public void shutdown() {
        for (UUID uuid : new ArrayList<>(sessions.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(Component.text("Rail switch editing session closed.", NamedTextColor.YELLOW));
            }
        }
        sessions.clear();
    }

    /**
     * Begins a configuration session for the player to edit destinations on the detector rail.
     *
     * @param player The player starting the session
     * @param detectorRail The detector rail block to configure
     */
    public void beginSession(Player player, Block detectorRail) {
        if (player == null || detectorRail == null) {
            return;
        }
        RailSwitchStorage storage = plugin.getRailSwitchStorage();
        if (storage == null) {
            player.sendMessage(Component.text("Rail switch storage is not available.", NamedTextColor.RED));
            return;
        }
        RailSwitchRecord record = storage.get(detectorRail).orElse(null);
        String header = record != null ? record.getHeader() : SwitchType.NORMAL.getTag();
        List<String> destinations = record != null ? new ArrayList<>(record.getLines()) : new ArrayList<>();
        int maxDestinations = 0;
        if (plugin.getSwitchConfiguration() != null) {
            maxDestinations = plugin.getSwitchConfiguration().getMaxDestinationsPerSwitch();
        }
        Session session = new Session(RailSwitchKey.from(detectorRail), header, destinations, maxDestinations);
        sessions.put(player.getUniqueId(), session);
        sendPrompt(player, destinations);
    }

    /**
     * Checks if the player is currently in an editing session.
     *
     * @param player The player to check
     * @return True if the player is editing
     */
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

    /**
     * Cancels all editing sessions for the given rail switch key.
     *
     * @param key The rail switch key
     * @param reason The reason message to send to players, or null
     */
    public void cancelSessionsFor(RailSwitchKey key, String reason) {
        sessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (session.getKey().equals(key)) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline() && !Strings.isNullOrEmpty(reason)) {
                    player.sendMessage(Component.text(reason, NamedTextColor.YELLOW));
                }
                return true;
            }
            return false;
        });
    }

    private void handleInput(Player player, Session session, String message) {
        if (Strings.isNullOrEmpty(message)) {
            player.sendMessage(Component.text("No destinations provided; editing session closed.", NamedTextColor.RED));
            return;
        }
        String trimmed = message.trim();
        if (trimmed.equalsIgnoreCase("cancel") || trimmed.equalsIgnoreCase("exit")) {
            player.sendMessage(Component.text("Rail switch editing cancelled.", NamedTextColor.YELLOW));
            return;
        }
        String[] tokens = trimmed.split("\\s+");
        boolean modified = session.applyTokens(tokens);
        List<String> failed = session.getAndClearFailedDestinations();
        if (!failed.isEmpty()) {
            player.sendMessage(Component.text("The following stations could not be added due to the destination limit (" + plugin.getSwitchConfiguration().getMaxDestinationsPerSwitch() + " max): " + String.join(", ", failed), NamedTextColor.RED));
        }
        RailSwitchStorage storage = plugin.getRailSwitchStorage();
        if (storage != null && modified) {
            storage.upsert(session.getKey(), session.getHeader(), session.getDestinations());
        }
        sendFinalList(player, session.getDestinations(), modified);
    }

    private void sendPrompt(Player player, List<String> destinations) {
        player.sendMessage(Component.text("Editing rail destinations. Reply once with space-separated tokens.", NamedTextColor.AQUA));
        player.sendMessage(Component.text("Use 'name' to add, '!name' to toggle exclusion, '-name' to remove, or 'cancel' to exit.", NamedTextColor.AQUA));
        sendCurrentList(player, destinations);
    }

    private void sendCurrentList(Player player, List<String> destinations) {
        if (destinations.isEmpty()) {
            player.sendMessage(Component.text("No destinations configured.", NamedTextColor.GRAY));
        } else {
            player.sendMessage(Component.text("Current destinations: " + String.join(" ", destinations), NamedTextColor.GREEN));
        }
    }

    private void sendFinalList(Player player, List<String> destinations, boolean modified) {
        if (destinations.isEmpty()) {
            player.sendMessage(Component.text((modified ? "No destinations remain." : "No destinations configured."), NamedTextColor.GRAY));
        } else {
            String text = (modified ? "Updated destinations: " : "Current destinations: ") + String.join(" ", destinations);
            player.sendMessage(Component.text(text, NamedTextColor.GREEN));
        }
        player.sendMessage(Component.text("Editing session closed.", NamedTextColor.YELLOW));
    }

    private void cleanupSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                return true;
            }
            if (!isHoldingConfigurationTool(player)) {
                player.sendMessage(Component.text("Stopped editing; hold the configuration tool to edit again.", NamedTextColor.YELLOW));
                return true;
            }
            if (entry.getValue().isExpired(now)) {
                player.sendMessage(Component.text("Rail switch editing session timed out.", NamedTextColor.YELLOW));
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
                player.sendMessage(Component.text("Rail switch editing session closed; configuration tool no longer held.", NamedTextColor.YELLOW));
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
        private final int maxDestinations;
        private final List<String> failedDestinations;
        private Instant expiresAt;

        Session(RailSwitchKey key, String header, List<String> destinations, int maxDestinations) {
            this.key = key;
            this.header = header;
            this.destinations = destinations == null ? new ArrayList<>() : new ArrayList<>(destinations);
            this.maxDestinations = Math.max(0, maxDestinations);
            this.failedDestinations = new ArrayList<>();
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

        List<String> getAndClearFailedDestinations() {
            List<String> copy = new ArrayList<>(failedDestinations);
            failedDestinations.clear();
            return copy;
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
            if (!canAddAnother()) {
                failedDestinations.add(value);
                return false;
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
            if (!canAddAnother()) {
                failedDestinations.add("!" + value);
                return false;
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

        private boolean canAddAnother() {
            return maxDestinations <= 0 || destinations.size() < maxDestinations;
        }
    }
}
