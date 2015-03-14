package net.minelink.ctplus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

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
        String message = plugin.getConfig().getString("tag-message", null);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public boolean playEffect() {
        return plugin.getConfig().getBoolean("play-effect");
    }

    public boolean alwaysSpawn() {
        return plugin.getConfig().getBoolean("always-spawn");
    }

    public int getLogoutWaitTime() {
        return plugin.getConfig().getInt("logout-wait-time", 10);
    }

    public boolean instantlyKill() {
        return plugin.getConfig().getBoolean("instantly-kill");
    }

    public boolean disableBlockEdit() {
        return plugin.getConfig().getBoolean("disable-block-edit");
    }

    public boolean disableCreativeTags() {
        return plugin.getConfig().getBoolean("disable-creative-tags");
    }

    public boolean disableEnderpearls() {
        return plugin.getConfig().getBoolean("disable-enderpearls");
    }

    public boolean disableFlying() {
        return plugin.getConfig().getBoolean("disable-flying");
    }

    public int getNpcDespawnTime() {
        return plugin.getConfig().getInt("npc-despawn-time", 60);
    }

    public int getNpcDespawnTicks() {
        return getNpcDespawnTime() * 20;
    }

    public boolean generateRandomName() {
        return plugin.getConfig().getBoolean("generate-random-name");
    }

    public String getRandomNamePrefix() {
        return plugin.getConfig().getString("random-name-prefix");
    }

    public String getKillMessage() {
        String message = plugin.getConfig().getString("kill-message", null);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public boolean useBarApi() {
        return plugin.getConfig().getBoolean("barapi");
    }

    public boolean useForceFields() {
        return plugin.getConfig().getBoolean("force-fields");
    }

    public int getForceFieldRadius() {
        return plugin.getConfig().getInt("force-field-radius");
    }

    public boolean useFactions() {
        return plugin.getConfig().getBoolean("factions");
    }

    public boolean useWorldGuard() {
        return plugin.getConfig().getBoolean("worldguard");
    }

    public List<String> getDisabledWorlds() {
        return plugin.getConfig().getStringList("disabled-worlds");
    }

    public List<String> getDisabledCommands() {
        return plugin.getConfig().getStringList("disabled-commands");
    }

}
