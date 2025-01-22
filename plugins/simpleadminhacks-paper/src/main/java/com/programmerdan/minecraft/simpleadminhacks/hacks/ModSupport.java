package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.google.common.io.ByteArrayDataOutput;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.ModSupportConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.BlockInventoryHolder;
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

    // ============================================================
    // Send World Info
    //
    // Sends the world name and uuid to the client. This will allow mods to better discriminate between worlds without
    // using dimension IDs, which may not be unique to each world.
    // ============================================================

    @EventHandler(
        ignoreCancelled = true,
        priority = EventPriority.MONITOR
    )
    private void sendWorldInfoOnLogin(
        final @NotNull PlayerJoinEvent event
    ) {
        if (this.config.sendWorldInfo) {
            INTERNAL_sendWorldInfo(
                event.getPlayer(),
                event.getPlayer().getWorld()
            );
        }
    }

    @EventHandler(
        ignoreCancelled = true,
        priority = EventPriority.MONITOR
    )
    private void sendWorldInfoOnRespawn(
        final @NotNull PlayerPostRespawnEvent event
    ) {
        if (this.config.sendWorldInfo) {
            INTERNAL_sendWorldInfo(
                event.getPlayer(),
                event.getRespawnedLocation().getWorld()
            );
        }
    }

    private void INTERNAL_sendWorldInfo(
        final @NotNull PluginMessageRecipient recipient,
        final @NotNull World world
    ) {
        final ByteArrayDataOutput out = ByteHelpers.newPacketWriter(128);
        out.writeUTF("WORLD_INFO");
        out.writeByte(1); // Packet schema id

        out.writeUTF(world.getName());
        out.writeUTF(world.getUID().toString());

        recipient.sendPluginMessage(plugin(), MOD_SUPPORT_CHANNEL, out.toByteArray());
    }

    // ============================================================
    // Send Inventory Location(s)
    //
    // Sends the block-location(s) of an opened inventory to the client. This will allow for QOL chest management mods
    // without violating block reading rules.
    // ============================================================

    @EventHandler(
        ignoreCancelled = true,
        priority = EventPriority.MONITOR
    )
    private void sendOpenedInventoryLocation(
        final @NotNull InventoryOpenEvent event
    ) {
        if (!this.config.sendInventoryLocation) {
            return;
        }
        if (!(event.getPlayer() instanceof final Player viewer)) {
            return;
        }

        final ByteArrayDataOutput out = ByteHelpers.newPacketWriter(256);
        out.writeUTF("INVENTORY_LOCATION");
        out.writeByte(1); // Packet schema id
        switch (event.getInventory().getHolder()) {
            case null -> out.writeByte(-1); // GUI
            case final BlockInventoryHolder blockHolder -> {
                out.writeByte(1); // Single block
                final Block block = blockHolder.getBlock();
                out.writeInt(block.getX());
                out.writeInt(block.getY());
                out.writeInt(block.getZ());
            }
            case final DoubleChest doubleChest -> {
                out.writeByte(2); // Two blocks
                final var lhsChest = (Chest) doubleChest.getLeftSide(); assert lhsChest != null;
                out.writeInt(lhsChest.getX());
                out.writeInt(lhsChest.getY());
                out.writeInt(lhsChest.getZ());
                final var rhsChest = (Chest) doubleChest.getRightSide(); assert rhsChest != null;
                out.writeInt(rhsChest.getX());
                out.writeInt(rhsChest.getY());
                out.writeInt(rhsChest.getZ());
            }
            default -> out.writeByte(0); // Not supported
        }
        viewer.sendPluginMessage(plugin(), MOD_SUPPORT_CHANNEL, out.toByteArray());
    }
}
