package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.destroystokyo.paper.MaterialTags;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.PacketManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public final class AttrHider extends BasicHack {

    public static final String BYPASS_PERMISSION = "attrhider.bypass";

    private final PacketManager packets = new PacketManager();
    private Set<Object> customPackets = new HashSet<>();

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
            this.packets.addAdapter(new PacketAdapter(this.plugin, PacketType.Play.Server.ENTITY_EFFECT) {
                @Override
                public void onPacketSending(final PacketEvent event) {
                    final PacketContainer packet = event.getPacket();
                    final Player player = event.getPlayer();
                    if (player.hasPermission(BYPASS_PERMISSION)) {
                        return;
                    }
                    final PacketContainer cloned = packet.deepClone();
                    final StructureModifier<Integer> ints = cloned.getIntegers();
                    if (player.getEntityId() == ints.read(0)) {
                        return;
                    }
                    // set amplifier to 0
                    cloned.getBytes().write(1, (byte) 0);
                    // set duration to 0
                    ints.write(1, 0);
                    // The packet data is shared between events, but the event
                    // instance is exclusive to THIS sending of the packet
                    event.setPacket(cloned);
                }
            });
        }
        if (this.hideHealth) {
            this.packets.addAdapter(new PacketAdapter(this.plugin, PacketType.Play.Server.ENTITY_METADATA) {
                @Override
                public void onPacketSending(final PacketEvent event) {
                    final PacketContainer packet = event.getPacket();
                    final Player player = event.getPlayer();
                    if (player.hasPermission(BYPASS_PERMISSION)) {
                        return;
                    }
                    final Entity entity = packet.getEntityModifier(event).read(0);
                    if (!(entity instanceof LivingEntity)
                        || player.getEntityId() == entity.getEntityId()
                        || entity.getPassengers().contains(player)
                        || customPackets.remove(packet.getHandle())) {
                        return;
                    }
                    final PacketContainer cloned = packet.deepClone();
                    for (final WrappedDataValue object : cloned.getDataValueCollectionModifier().read(0)) {
                        // Read the 8th field as a float as that's the living entity's health
                        // https://wiki.vg/Entity_metadata#Living_Entity
                        if (object.getIndex() == 9) {
                            if ((float) object.getValue() > 0) {
                                object.setValue(1f); // Half a heart
                            }
                        }
                    }
                    // The packet data is shared between events, but the event
                    // instance is exclusive to THIS sending of the packet
                    event.setPacket(cloned);
                }
            });
        }
        if (this.roundPlayerListPing) {
            this.packets.addAdapter(new PacketAdapter(this.plugin, PacketType.Play.Server.PLAYER_INFO) {
                @Override
                public void onPacketSending(final PacketEvent event) {
                    final PacketContainer packet = event.getPacket();
                    final Player player = event.getPlayer();
                    if (player.hasPermission(BYPASS_PERMISSION)) {
                        return;
                    }
                    final PacketContainer cloned = packet.deepClone();
                    List<PlayerInfoData> newInfos = new ArrayList<>();
                    List<PlayerInfoData> oldInfos = cloned.getPlayerInfoDataLists().read(1);
                    for (PlayerInfoData oldInfo : oldInfos) {
                        if (oldInfo == null) continue;
                        int latency = oldInfo.getLatency();
                        // Limit player ping in the tablist to the same 6 values vanilla clients can discern visually
                        // this follows 1.16.5 PlayerTabOverlay#renderPingIcon()
                        if (latency < 0) latency = -1;
                        else if (latency < 150) latency = 75; // average of 0 and 150, arbitrary
                        else if (latency < 300) latency = 225;
                        else if (latency < 600) latency = 450;
                        else if (latency < 1000) latency = 800;
                        else latency = 1000;
                        newInfos.add(new PlayerInfoData(
                            oldInfo.getProfileId(),
                            latency,
                            oldInfo.isListed(),
                            oldInfo.getGameMode(),
                            oldInfo.getProfile(),
                            oldInfo.getDisplayName(),
                            oldInfo.getRemoteChatSessionData()
                        ));
					}
					cloned.getPlayerInfoDataLists().write(1, newInfos);
                    // The packet data is shared between events, but the event
                    // instance is exclusive to THIS sending of the packet
                    event.setPacket(cloned);
                }
            });
        }
    }

    @Override
    public void onDisable() {
        this.packets.removeAllAdapters();
        super.onDisable();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMountEntity(EntityMountEvent event) {
        if (this.hideHealth && event.getEntity() instanceof Player player && event.getMount() instanceof LivingEntity mountedEntity) {
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();

            watcher.setEntity(mountedEntity);
            watcher.setObject(9, WrappedDataWatcher.Registry.get(Float.class), (float) mountedEntity.getHealth());
            for (WrappedWatchableObject entry : watcher.getWatchableObjects()) {
                if (entry == null) continue;

                WrappedDataWatcher.WrappedDataWatcherObject watcherObject = entry.getWatcherObject();

                wrappedDataValueList.add(new WrappedDataValue(watcherObject.getIndex(), watcherObject.getSerializer(), entry.getRawValue()));
            }

            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, mountedEntity.getEntityId());
            packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            customPackets.add(packet.getHandle()); // Allow ignoring this packet when hiding health because EntityMountEvent fires *before* the player is actually a passenger
        }
    }

    private static boolean shouldBeObfuscated(final Material material) {
        return MaterialTags.HELMETS.isTagged(material)
            || MaterialTags.CHEST_EQUIPPABLE.isTagged(material)
            || MaterialTags.LEGGINGS.isTagged(material)
            || MaterialTags.BOOTS.isTagged(material)
            || MaterialTags.SWORDS.isTagged(material)
            || MaterialTags.AXES.isTagged(material)
            || MaterialTags.PICKAXES.isTagged(material)
            || MaterialTags.SHOVELS.isTagged(material)
            || MaterialTags.HOES.isTagged(material)
            || material == Material.FIREWORK_ROCKET
            || material == Material.WRITTEN_BOOK
            || material == Material.ENCHANTED_BOOK
            || material == Material.POTION
            || material == Material.LINGERING_POTION
            || material == Material.SPLASH_POTION;
    }
}
