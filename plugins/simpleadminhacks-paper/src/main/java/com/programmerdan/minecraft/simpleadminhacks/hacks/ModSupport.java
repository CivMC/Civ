package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.google.common.io.ByteArrayDataOutput;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.ModSupportConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageRecipient;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.bytes.ByteHelpers;
import vg.civcraft.mc.civmodcore.bytes.Length;

public final class ModSupport extends SimpleHack<ModSupportConfig> implements Listener {
    private static final String MOD_SUPPORT_CHANNEL = "sah:mod_support";

    public ModSupport(
        final @NotNull SimpleAdminHacks plugin,
        final @NotNull ModSupportConfig config
    ) {
        super(plugin, config);
    }

    public static @NotNull ModSupportConfig generate(
        final @NotNull SimpleAdminHacks plugin,
        final @NotNull ConfigurationSection config
    ) {
        return new ModSupportConfig(plugin, config);
    }

    @Override
    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this.plugin, MOD_SUPPORT_CHANNEL);
        this.plugin.registerListener(this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        HandlerList.unregisterAll(this);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this.plugin, MOD_SUPPORT_CHANNEL);
    }

    // ============================================================
    // Send Tags
    //
    // These are bits of information that the server wants the client to know. These could be anything. For example,
    // servers could send a "no-elevation" tag to tell client mods to disable any elevation data. Or just a "CivMC" tag,
    // letting the client figure out what that means. Or neither. It's entirely optional.
    // ============================================================

    @EventHandler(
        ignoreCancelled = true,
        priority = EventPriority.LOWEST
    )
    private void sendTagsOnJoin(
        final @NotNull PlayerJoinEvent event
    ) {
        final PluginMessageRecipient recipient = event.getPlayer();
        if (this.config.sendServerTags) {
            final ByteArrayDataOutput out = ByteHelpers.newPacketWriter(128);
            out.writeUTF("TAGS");
            out.writeByte(1); // Packet schema id
            ByteHelpers.writeCollection(out, this.config.serverTags, Length.u8, ByteArrayDataOutput::writeUTF);
            recipient.sendPluginMessage(plugin(), MOD_SUPPORT_CHANNEL, out.toByteArray());
        }
    }
}
