package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CaseInsensitivity extends BasicHack {

    public CaseInsensitivity(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String[] splitMessage = event.getMessage().split(" ", 2);
        event.setMessage(splitMessage[0].toLowerCase() + " " + splitMessage[1]);
    }
}
