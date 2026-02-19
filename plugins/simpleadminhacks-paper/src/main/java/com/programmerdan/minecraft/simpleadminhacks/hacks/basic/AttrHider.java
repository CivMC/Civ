package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.Action;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.PlayerInfo;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityMountEvent;

public final class AttrHider extends BasicHack {

    public static final String BYPASS_PERMISSION = "attrhider.bypass";

    private final List<PacketListenerCommon> listeners = new ArrayList<>();

    @AutoLoad
    private boolean hideEffects;

    @AutoLoad
    private boolean hideHealth;

    @AutoLoad
    private boolean roundPlayerListPing;

    public AttrHider(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (this.hideEffects) {
            registerListener(new EffectHiderListener());
        }
        if (this.hideHealth) {
            registerListener(new HealthHiderListener());
        }
        if (this.roundPlayerListPing) {
            registerListener(new PingRounderListener());
        }
    }

    @Override
    public void onDisable() {
        for (final PacketListenerCommon listener : this.listeners) {
            PacketEvents.getAPI().getEventManager().unregisterListener(listener);
        }
        this.listeners.clear();
        super.onDisable();
    }

    private void registerListener(final PacketListener listener) {
        final PacketListenerCommon registered = PacketEvents.getAPI().getEventManager()
            .registerListener(listener, PacketListenerPriority.NORMAL);
        this.listeners.add(registered);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMountEntity(final EntityMountEvent event) {
        if (this.hideHealth
            && event.getEntity() instanceof Player player
            && event.getMount() instanceof LivingEntity mountedEntity) {
            final List<EntityData<?>> metadata = new ArrayList<>();
            metadata.add(new EntityData<>(9, EntityDataTypes.FLOAT, (float) mountedEntity.getHealth()));
            final WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                mountedEntity.getEntityId(), metadata);
            // Send silently so our own health-hiding listener does not intercept this packet.
            // EntityMountEvent fires *before* the player is actually a passenger,
            // so the listener would otherwise hide the real health.
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        }
    }

    private static final class EffectHiderListener implements PacketListener {

        @Override
        public void onPacketSend(final PacketSendEvent event) {
            if (event.getPacketType() != PacketType.Play.Server.ENTITY_EFFECT) {
                return;
            }
            final Player player = event.getPlayer();
            if (player == null || player.hasPermission(BYPASS_PERMISSION)) {
                return;
            }
            final WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect(event);
            if (player.getEntityId() == packet.getEntityId()) {
                return;
            }
            // Set amplifier to 0
            packet.setEffectAmplifier(0);
            // Set duration to 0
            packet.setEffectDurationTicks(0);
            event.markForReEncode(true);
        }
    }

    private static final class HealthHiderListener implements PacketListener {

        @Override
        public void onPacketSend(final PacketSendEvent event) {
            if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA) {
                return;
            }
            final Player player = event.getPlayer();
            if (player == null || player.hasPermission(BYPASS_PERMISSION)) {
                return;
            }
            final WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
            final int entityId = packet.getEntityId();
            if (player.getEntityId() == entityId) {
                return;
            }
            final Entity entity = SpigotConversionUtil.getEntityById(player.getWorld(), entityId);
            if (!(entity instanceof LivingEntity) || entity.getPassengers().contains(player)) {
                return;
            }
            for (final EntityData<?> data : packet.getEntityMetadata()) {
                // Index 9 is the LivingEntity health field
                // https://wiki.vg/Entity_metadata#Living_Entity
                if (data.getIndex() == 9 && data.getType() == EntityDataTypes.FLOAT) {
                    if ((float) data.getValue() > 0) {
                        @SuppressWarnings("unchecked")
                        final EntityData<Float> healthData = (EntityData<Float>) data;
                        healthData.setValue(1f); // Half a heart
                    }
                }
            }
            event.markForReEncode(true);
        }
    }

    private static final class PingRounderListener implements PacketListener {

        @Override
        public void onPacketSend(final PacketSendEvent event) {
            if (event.getPacketType() != PacketType.Play.Server.PLAYER_INFO_UPDATE) {
                return;
            }
            final Player player = event.getPlayer();
            if (player == null || player.hasPermission(BYPASS_PERMISSION)) {
                return;
            }
            final WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(event);
            if (!packet.getActions().contains(Action.UPDATE_LATENCY)) {
                return;
            }
            for (final PlayerInfo entry : packet.getEntries()) {
                int latency = entry.getLatency();
                // Limit player ping in the tablist to the same 6 values vanilla clients can discern visually
                // this follows 1.16.5 PlayerTabOverlay#renderPingIcon()
                if (latency < 0) {
                    latency = -1;
                } else if (latency < 150) {
                    latency = 75; // average of 0 and 150, arbitrary
                } else if (latency < 300) {
                    latency = 225;
                } else if (latency < 600) {
                    latency = 450;
                } else if (latency < 1000) {
                    latency = 800;
                } else {
                    latency = 1000;
                }
                entry.setLatency(latency);
            }
            event.markForReEncode(true);
        }
    }
}
