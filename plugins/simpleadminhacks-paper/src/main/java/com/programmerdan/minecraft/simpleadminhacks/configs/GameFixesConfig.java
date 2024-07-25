package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public class GameFixesConfig extends SimpleHackConfig {

    private boolean blockElytraBreakBug;
    private double damageOnElytraBreakBug;
    private boolean canStorageTeleport;
    private boolean stopBedBombing;
    private boolean stopAnchorBombing;
    private boolean preventTreeWrap;
    private boolean maintainFlatBedrock;
    private boolean preventLongSigns;
    private int signLengthLimit;
    private boolean preventLongSignsAbsolute;
    private boolean cancelLongSignEvent;
    private boolean hardLimitBookPageSize;

    public GameFixesConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
        super(plugin, base);
    }

    protected void wireup(ConfigurationSection config) {
        blockElytraBreakBug = config.getBoolean("blockElytraBreakBug", true);
        damageOnElytraBreakBug = config.getDouble("damageOnElytraBreakBug", 0.0d);
        if (blockElytraBreakBug)
            plugin().log(Level.INFO, "  Block Elytra 1height break bug is enabled, doing {} damage to violators", damageOnElytraBreakBug);

        canStorageTeleport = config.getBoolean("canStorageTeleport");
        if (!canStorageTeleport) plugin().log("  Storage holder teleportation is disabled.");

        stopBedBombing = config.getBoolean("stopBedBombingInHellBiomes", true);
        if (stopBedBombing) plugin().log("  Stop Bed Bombing In Hell Biomes is enabled.");

        stopAnchorBombing = config.getBoolean("stopAnchorBombing", true);
        if (stopAnchorBombing) plugin().log("Stop Anchor bombing outside the Nether is enabled");

        preventTreeWrap = config.getBoolean("preventTreeWraparound", true);
        if (preventTreeWrap) plugin().log("  Stop tree wrapping into bedrock is enabled.");

        maintainFlatBedrock = config.getBoolean("maintainFlatBedrock", false);
        if (maintainFlatBedrock) plugin().log("  Maintaining bedrock flatness.");

        preventLongSigns = config.getBoolean("preventLongSigns", true);
        signLengthLimit = config.getInt("signLengthLimit", 100);
        preventLongSignsAbsolute = config.getBoolean("preventLongSignsAbsolute", true);
        cancelLongSignEvent = config.getBoolean("cancelLongSignEvent", false);
        hardLimitBookPageSize = config.getBoolean("hardLimitBookPageCount", true);
    }

    public boolean isBlockElytraBreakBug() {
        return blockElytraBreakBug;
    }

    public double getDamageOnElytraBreakBug() {
        return damageOnElytraBreakBug;
    }

    public boolean canStorageTeleport() {
        return canStorageTeleport;
    }

    public boolean stopBedBombing() {
        return this.stopBedBombing;
    }

    public boolean stopAnchorBombing() {
        return this.stopAnchorBombing;
    }

    public boolean stopTreeWraparound() {
        return preventTreeWrap;
    }

    public boolean hardLimitBookPageSize() {
        return hardLimitBookPageSize;
    }

    public boolean maintainFlatBedrock() {
        return maintainFlatBedrock;
    }

    public boolean isPreventLongSigns() {
        return preventLongSigns;
    }

    public int getSignLengthLimit() {
        return signLengthLimit;
    }

    public boolean isPreventLongSignsAbsolute() {
        return preventLongSignsAbsolute;
    }

    public boolean isCancelLongSignEvent() {
        return cancelLongSignEvent;
    }

}
