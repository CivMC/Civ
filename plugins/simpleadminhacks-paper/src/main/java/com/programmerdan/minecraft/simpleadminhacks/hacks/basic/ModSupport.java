package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.Objects;
import net.minecraft.core.Vec3i;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.plugin.messaging.PluginMessageRecipient;
import org.jetbrains.annotations.NotNull;

public final class ModSupport extends BasicHack {
    private static final String MOD_SUPPORT_CHANNEL = "sah:mod_support";

    public ModSupport(
        final @NotNull SimpleAdminHacks plugin,
        final @NotNull BasicHackConfig config
    ) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin(), MOD_SUPPORT_CHANNEL);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin(), MOD_SUPPORT_CHANNEL);
    }

    // ============================================================
    // Send World Name
    //
    // Sends the world name and uuid to the client. This will allow mods to better discriminate between worlds without
    // using dimension IDs, which may be shared between worlds.
    // ============================================================

    @AutoLoad
    private boolean sendWorldName;

    @EventHandler(
        ignoreCancelled = true,
        priority = EventPriority.MONITOR
    )
    private void sendWorldNameOnLogin(
        final @NotNull PlayerJoinEvent event
    ) {
        if (!this.sendWorldName) {
            return;
        }
        INTERNAL_sendWorldName(
            event.getPlayer(),
            event.getPlayer().getWorld()
        );
    }

    @EventHandler(
        ignoreCancelled = true,
        priority = EventPriority.MONITOR
    )
    private void sendWorldNameOnRespawn(
        final @NotNull PlayerPostRespawnEvent event
    ) {
        if (!this.sendWorldName) {
            return;
        }
        INTERNAL_sendWorldName(
            event.getPlayer(),
            event.getRespawnedLocation().getWorld()
        );
    }

    private void INTERNAL_sendWorldName(
        final @NotNull PluginMessageRecipient recipient,
        final @NotNull World world
    ) {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput(128);
        out.writeUTF("WORLD_NAME");
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

    private static final Vec3i[] NO_BLOCKS = new Vec3i[0];

    @AutoLoad
    private boolean sendInventoryLocations;

    @EventHandler(
        ignoreCancelled = true,
        priority = EventPriority.MONITOR
    )
    private void sendOpenedInventoryLocation(
        final @NotNull InventoryOpenEvent event
    ) {
        if (!this.sendInventoryLocations) {
            return;
        }
        if (!(event.getPlayer() instanceof final Player viewer)) {
            return;
        }
        final Vec3i[] blocks = switch (event.getInventory().getHolder()) {
            case final BlockInventoryHolder blockHolder -> {
                final Block block = blockHolder.getBlock();
                yield new Vec3i[] {
                    new Vec3i(
                        block.getX(),
                        block.getY(),
                        block.getZ()
                    )
                };
            }
            case final DoubleChest doubleChest -> {
                final var lhsChest = Objects.requireNonNull((Chest) doubleChest.getLeftSide());
                final var rhsChest = Objects.requireNonNull((Chest) doubleChest.getRightSide());
                yield new Vec3i[] {
                    new Vec3i(
                        lhsChest.getX(),
                        lhsChest.getY(),
                        lhsChest.getZ()
                    ),
                    new Vec3i(
                        rhsChest.getX(),
                        rhsChest.getY(),
                        rhsChest.getZ()
                    )
                };
            }
            case null, default -> NO_BLOCKS;
        };

        final ByteArrayDataOutput out = ByteStreams.newDataOutput(256);
        out.writeUTF("INVENTORY_LOCATION");
        out.writeByte(1); // Packet schema id

        if (ArrayUtils.isEmpty(blocks)) {
            // Either it's a GUI, or is an unsupported inventory like a horse
            out.writeBoolean(false); // hasBlocks
        }
        else {
            out.writeBoolean(true); // hasBlocks
            for (final var iter = IteratorUtils.arrayIterator(blocks); iter.hasNext();) {
                final Vec3i block = iter.next();
                out.write(block.getX());
                out.write(block.getY());
                out.write(block.getZ());
                out.writeBoolean(iter.hasNext()); // hasAnotherBlock
            }
        }

        viewer.sendPluginMessage(plugin(), MOD_SUPPORT_CHANNEL, out.toByteArray());
    }
}
