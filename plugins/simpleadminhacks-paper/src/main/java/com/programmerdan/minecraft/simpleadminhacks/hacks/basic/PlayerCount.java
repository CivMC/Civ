package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class PlayerCount extends BasicHack implements PluginMessageListener {

    public static final String CHANNEL = "civproxy:player_count";
    private final Map<String, Integer> pings = new HashMap<>();

    public PlayerCount(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(SimpleAdminHacks.instance(), CHANNEL, this);
        new PlayerCountPlaceholders(this.pings).register();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!CHANNEL.equals(channel)) {
            return;
        }
        ByteArrayDataInput data = ByteStreams.newDataInput(message);
        int servers = data.readInt();
        for (int i = 0; i < servers; i++) {
            pings.put(data.readUTF().toLowerCase(), data.readInt());
        }
    }
}
