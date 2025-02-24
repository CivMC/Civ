package com.github.maxopoly.essenceglue;

import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.IntegerSetting;
import java.util.UUID;

public class VirtualEssenceManager {

    private final IntegerSetting playerEssence;
    private final int virtualCap;

    public VirtualEssenceManager(JavaPlugin plugin, int virtualCap) {
        playerEssence = new IntegerSetting(plugin, 0, "Player Essence", "playerEssence");
        this.virtualCap = virtualCap;
        PlayerSettingAPI.registerSetting(playerEssence, null);
    }

    public int getEssence(UUID playerId) {
        Integer value = playerEssence.getValue(playerId);
        return value == null ? 0 : value;
    }

    public void setEssence(UUID playerId, int essence) {
        playerEssence.setValue(playerId, Math.min(essence, this.virtualCap));
    }

    public boolean isAtEssenceCapacity(UUID playerId) {
        Integer value = playerEssence.getValue(playerId);
        if (value == null) {
            return false;
        }
        return value >= this.virtualCap;
    }
}
