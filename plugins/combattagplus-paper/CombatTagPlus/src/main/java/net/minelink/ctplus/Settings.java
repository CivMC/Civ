package net.minelink.ctplus;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Settings {

    private final CombatTagPlus plugin;

    Settings(CombatTagPlus plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        Configuration defaults = plugin.getConfig().getDefaults();
        defaults.set("disabled-worlds", new ArrayList<>());
        defaults.set("disabled-commands", new ArrayList<>());
    }

    public void update() {
        // Initialize the new config cache
        List<Map<String, Object>> config = new ArrayList<>();

        // Default config path
        Path path = Paths.get(plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml");

        // Iterate through new config
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(plugin.getResource("config.yml"));
        for (String key : defaultConfig.getKeys(true)) {
            // Convert key to correct format for a single line
            String oneLineKey = StringUtils.repeat("  ", key.split(".").length) + key + ": ";

            // Generate new config section
            Map<String, Object> section = new HashMap<>();
            section.put("key", oneLineKey);

            // Attempt to save value from old configuration
            if (key.equals("config-version")) {
                section.put("value", defaultConfig.get(key));
            } else if (plugin.getConfig().get(key) != null) {
                section.put("value", plugin.getConfig().get(key));
            } else {
                section.put("value", defaultConfig.get(key));
            }

            // Save section to cache
            config.add(section);
        }

        // Attempt to open the default config
        try (BufferedReader br = new BufferedReader(new InputStreamReader(plugin.getResource("config.yml")))) {
            // Iterate through all lines in this config
            String current;
            String previous = null;
            List<String> comments = new ArrayList<>();
            while ((current = br.readLine()) != null) {
                // If previous line is a comment add it, else clear the comments
                if (previous != null && previous.matches("(| +)#.*")) {
                    comments.add(previous);
                } else {
                    comments.clear();
                }

                // Iterate through current config cache
                for (Map<String, Object> section : config) {
                    // Do nothing if key is not valid
                    if (section.get("key") == null) continue;

                    // Do nothing if there are no comments to assign
                    if (comments.isEmpty()) continue;

                    // Do nothing if current line doesn't start with this key
                    String key = section.get("key").toString();
                    if (!current.startsWith(key.substring(0, key.length() - 1))) continue;

                    // Add comment to config cache
                    section.put("comments", new ArrayList<>(comments));
                }

                // Set the previous line
                previous = current;
            }
        } catch (IOException e) {
            // Failed to read from default config within plugin jar
            plugin.getLogger().severe("**CONFIG ERROR**");
            plugin.getLogger().severe("Failed to read from default config within plugin jar.");

            // Leave a stack trace in console
            e.printStackTrace();
        }

        // Attempt to open the new config
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Iterate through new cached config
            for (int i = 0; i < config.size(); i++) {
                // Get section of the config at this index
                Map<String, Object> section = config.get(i);

                // Do nothing if key is invalid
                if (section.get("key") == null) continue;

                // Do nothing if value is invalid
                if (section.get("value") == null) continue;

                // Write the comments if they are valid
                Object comments = section.get("comments");
                if (comments != null && comments instanceof List) {
                    for (Object o : (List) comments) {
                        writer.write(o.toString());
                        writer.newLine();
                    }
                }

                // Write the key
                String key = section.get("key").toString();
                writer.write(key);

                // Write the value
                Object value = section.get("value");

                if (value instanceof String) {
                    writer.write("'" + value.toString() + "'");
                } else if (value instanceof List) {
                    List list = (List) value;
                    int indent = key.length() - key.replace(" ", "").length() - 1;
                    for (Object s : list) {
                        writer.newLine();
                        writer.write(StringUtils.repeat(" ", indent) + "  - '" + s.toString() + "'");
                    }
                } else {
                    writer.write(value.toString());
                }

                // Write a couple more lines for extra space ;-)
                if (config.size() > i + 1) {
                    writer.newLine();
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            // Failed to write new config file to the disk
            plugin.getLogger().severe("**CONFIG ERROR**");
            plugin.getLogger().severe("Failed to write an updated config to the disk.");

            // Leave a stack trace in console
            e.printStackTrace();
        }

        // Reload the updated configuration
        plugin.reloadConfig();
        load();
    }

    public int getConfigVersion() {
        return plugin.getConfig().getInt("config-version", 0);
    }

    public int getLatestConfigVersion() {
        return plugin.getConfig().getDefaults().getInt("config-version", 0);
    }

    public boolean isOutdated() {
        return getConfigVersion() < getLatestConfigVersion();
    }

    public int getTagDuration() {
        return plugin.getConfig().getInt("tag-duration", 15);
    }

    public String getTagMessage() {
        String message = plugin.getConfig().getString("tag-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getUntagMessage() {
        String message = plugin.getConfig().getString("untag-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean resetTagOnPearl() {
        return plugin.getConfig().getBoolean("reset-tag-on-pearl");
    }

    public boolean playEffect() {
        return plugin.getConfig().getBoolean("play-effect");
    }

    public boolean alwaysSpawn() {
        return plugin.getConfig().getBoolean("always-spawn");
    }

    public boolean mobTagging() {
        return plugin.getConfig().getBoolean("mob-tagging");
    }

    public int getLogoutWaitTime() {
        return plugin.getConfig().getInt("logout-wait-time", 10);
    }

    public String getLogoutCancelledMessage() {
        String message = plugin.getConfig().getString("logout-cancelled-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getLogoutSuccessMessage() {
        String message = plugin.getConfig().getString("logout-success-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getLogoutPendingMessage() {
        String message = plugin.getConfig().getString("logout-pending-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean instantlyKill() {
        return plugin.getConfig().getBoolean("instantly-kill");
    }

    public boolean untagOnKick() {
        return plugin.getConfig().getBoolean("untag-on-kick");
    }

    public boolean onlyTagAttacker() {
        return plugin.getConfig().getBoolean("only-tag-attacker");
    }

    public boolean disableSelfTagging() {
        return plugin.getConfig().getBoolean("disable-self-tagging");
    }

    public boolean disableBlockEdit() {
        return plugin.getConfig().getBoolean("disable-block-edit");
    }

    public String getDisableBlockEditMessage() {
        String message = plugin.getConfig().getString("disable-block-edit-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean disableCreativeTags() {
        return plugin.getConfig().getBoolean("disable-creative-tags");
    }

    public boolean disableEnderpearls() {
        return plugin.getConfig().getBoolean("disable-enderpearls");
    }

    public String getDisableEnderpearlsMessage() {
        String message = plugin.getConfig().getString("disable-enderpearls-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean disableFlying() {
        return plugin.getConfig().getBoolean("disable-flying");
    }

    public String getDisableFlyingMessage() {
        String message = plugin.getConfig().getString("disable-flying-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean disableTeleportation() {
        return plugin.getConfig().getBoolean("disable-teleportation");
    }

    public String getDisableTeleportationMessage() {
        String message = plugin.getConfig().getString("disable-teleportation-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean disableCrafting() {
        return plugin.getConfig().getBoolean("disable-crafting");
    }

    public String getDisableCraftingMessage() {
        String message = plugin.getConfig().getString("disable-crafting-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public int getNpcDespawnTime() {
        return plugin.getConfig().getInt("npc-despawn-time", 60);
    }

    public int getNpcDespawnMillis() {
        return getNpcDespawnTime() * 1000;
    }

    public boolean resetDespawnTimeOnHit() {
        return plugin.getConfig().getBoolean("reset-despawn-time-on-hit");
    }

    public boolean generateRandomName() {
        return plugin.getConfig().getBoolean("generate-random-name");
    }

    public String getRandomNamePrefix() {
        return plugin.getConfig().getString("random-name-prefix");
    }

    public String getKillMessage() {
        String message = plugin.getConfig().getString("kill-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getKillMessageItem() {
        String message = plugin.getConfig().getString("kill-message-item", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean useBarApi() {
        return plugin.getConfig().getBoolean("barapi");
    }

    public String getBarApiEndedMessage() {
        String message = plugin.getConfig().getString("barapi-ended-message", "&aYou are no longer in combat!");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getBarApiCountdownMessage() {
        String message = plugin.getConfig().getString("barapi-countdown-message", "&eCombatTag: &f{remaining}");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean denySafezone() {
        return plugin.getConfig().getBoolean("deny-safezone");
    }

    public boolean denySafezoneEnderpearl() {
        return plugin.getConfig().getBoolean("deny-safezone-enderpearl");
    }

    public boolean useForceFields() {
        return plugin.getConfig().getBoolean("force-fields");
    }

    public int getForceFieldRadius() {
        return plugin.getConfig().getInt("force-field-radius");
    }

    public String getForceFieldMaterial() {
        return plugin.getConfig().getString("force-field-material");
    }

    public byte getForceFieldMaterialDamage() {
        return (byte) plugin.getConfig().getInt("force-field-material-damage");
    }

    public boolean useFactions() {
        return plugin.getConfig().getBoolean("factions");
    }

    public boolean useTowny() {
        return plugin.getConfig().getBoolean("towny");
    }

    public boolean useWorldGuard() {
        return plugin.getConfig().getBoolean("worldguard");
    }

    public List<String> getDisabledWorlds() {
        return plugin.getConfig().getStringList("disabled-worlds");
    }

    public String getDisabledCommandMessage() {
        String message = plugin.getConfig().getString("disabled-command-message", "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public List<String> getDisabledCommands() {
        return plugin.getConfig().getStringList("disabled-commands");
    }

}
