package vg.civcraft.mc.citadel.model;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.listener.ModeListener;

public class HologramManager {

    // distance from center to diagonal corner is 0.5 * sqrt(2) and we add 10 % for
    // good measure
    private static final double HOLOOFFSET = 0.55 * Math.sqrt(2);

    private final Map<Location, Map<UUID, PlayerHolo>> holograms;
    private final Set<PlayerHolo> activeHolos;
    private final CitadelSettingManager settingMan;

    public HologramManager(CitadelSettingManager settingMan) {
        this.holograms = new HashMap<>();
        this.activeHolos = new HashSet<>();
        this.settingMan = settingMan;
        new BukkitRunnable() {

            @Override
            public void run() {
                updateHolograms();
            }

        }.runTaskTimer(Citadel.getInstance(), 2L, 2L);
    }

    public void showInfoHolo(Reinforcement rein, Player player) {
        Map<UUID, PlayerHolo> locationSpecificHolos = holograms.get(rein.getLocation());
        if (locationSpecificHolos == null) {
            locationSpecificHolos = new TreeMap<>();
            holograms.put(rein.getLocation(), locationSpecificHolos);
        }
        PlayerHolo holo = locationSpecificHolos.get(player.getUniqueId());
        if (holo == null) {
            holo = new PlayerHolo(player, rein);
            locationSpecificHolos.put(player.getUniqueId(), holo);
        }
        activeHolos.add(holo);
        holo.show();
    }

    public void deleteHolos(Player player) {
        for (Map.Entry<Location, Map<UUID, PlayerHolo>> entry : holograms.entrySet()) {
            Map<UUID, PlayerHolo> map = entry.getValue();
            PlayerHolo holo = map.get(player.getUniqueId());
            if (holo != null) {
                holo.delete();
                map.remove(player.getUniqueId());
            }
        }
    }

    private static Location getHoloLocation(Reinforcement rein, Player player) {
        Location baseLoc = rein.getBlockCenter();
        baseLoc = baseLoc.add(0, 0.5, 0);
        Vector vector = player.getEyeLocation().toVector();
        vector.subtract(baseLoc.toVector());
        // vector is now the offset from the reinforcement to the player and we now move
        // towards the player so the hologram isn't inside the block
        vector.normalize();
        // holoOffSet is a good distance to ensure we fully move the hologram out of the
        // block
        vector.multiply(HOLOOFFSET);
        baseLoc.add(vector);
        return baseLoc;
    }

    private void updateHolograms() {
        for (Iterator<PlayerHolo> iter = activeHolos.iterator(); iter.hasNext(); ) {
            PlayerHolo holo = iter.next();
            if (!holo.update()) {
                iter.remove();
            }
        }
    }

    private class PlayerHolo {

        private final Player player;
        private int hologramId;
        private final Reinforcement reinforcement;
        private long timeStamp;
        private final boolean hasPermission;
        private Location cachedPlayerLocation;
        private final long cullDelay;

        public PlayerHolo(Player player, Reinforcement reinforcement) {
            this.player = player;
            this.reinforcement = reinforcement;
            this.timeStamp = System.currentTimeMillis();
            this.cullDelay = settingMan.getHologramDuration(player.getUniqueId());
            // we intentionally cache permission to avoid having to look it up often
            // showing a bit too much information if the player gets kicked while a holo is
            // already visible does not matter
            this.hasPermission = reinforcement.hasPermission(player, CitadelPermissionHandler.getInfo());
        }

        void show() {
            refreshTimestamp();
            if (hologramId != 0) {
                return;
            }

            this.hologramId = Bukkit.getUnsafe().nextEntityId();

            Location location = getHoloLocation(reinforcement, player);
            Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());

            WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                hologramId,
                Optional.of(UUID.randomUUID()),
                EntityTypes.TEXT_DISPLAY,
                position,
                0f, // pitch
                0f, // yaw
                0f, // headYaw
                0,  // data
                Optional.of(Vector3d.zero())
            );

            PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);
            sendUpdatePacket();
            cachedPlayerLocation = player.getLocation();
        }

        private void sendUpdatePacket() {
            List<EntityData<?>> metadata = new ArrayList<>();
            // Index 10: Interpolation duration (INT) - for display entities
            metadata.add(new EntityData<>(10, EntityDataTypes.INT, 2));
            // Index 15: Billboard constraint (BYTE) - 3 = CENTER (always faces player)
            metadata.add(new EntityData<>(15, EntityDataTypes.BYTE, (byte) 3));
            // Index 23: Text content (ADV_COMPONENT) - the hologram text
            metadata.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, createHoloContent()));
            // Index 27: Display entity flags (BYTE) - 0x02 = see through
            metadata.add(new EntityData<>(27, EntityDataTypes.BYTE, (byte) 0x02));

            WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(hologramId, metadata);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);
        }

        private Component createHoloContent() {
            Component component = Component.empty();
            component = component.append(ModeListener.formatHealthRgb(reinforcement));
            if (hasPermission) {
                component = component.append(Component.newline());
                component = component.append(Component.text(reinforcement.getGroup().getName(), NamedTextColor.LIGHT_PURPLE)).append(Component.newline());
                component = component.append(Component.text(reinforcement.getType().getName(), NamedTextColor.AQUA));
                if (!reinforcement.isMature()) {
                    component = component.append(Component.newline()).append(Component.text(ModeListener.formatProgress(reinforcement.getCreationTime(), reinforcement.getType().getMaturationTime(), ""), NamedTextColor.GRAY));
                }
            }
            return component;
        }

        boolean update() {
            if (hologramId == 0) {
                return false;
            }
            if (System.currentTimeMillis() - timeStamp > cullDelay) {
                delete();
                return false;
            }
            if (reinforcement.isBroken()) {
                delete();
                return false;
            }
            updateLocation();
            sendUpdatePacket();
            return true;
        }

        void updateLocation() {
            Location current = player.getLocation();
            // Location.equals would also check pitch/yaw
            if (current.getBlockX() == cachedPlayerLocation.getBlockX() && current.getBlockY() == cachedPlayerLocation.getBlockY()
                && current.getBlockZ() == cachedPlayerLocation.getBlockZ()) {
                return;
            }
            cachedPlayerLocation = current;
            Location updated = getHoloLocation(reinforcement, player);
            Vector3d position = new Vector3d(updated.getX(), updated.getY(), updated.getZ());

            WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                hologramId,
                position,
                Vector3d.zero(),
                0f, // yaw
                0f, // pitch
                RelativeFlag.NONE,
                false
            );

            PacketEvents.getAPI().getPlayerManager().sendPacket(player, teleportPacket);
        }

        void refreshTimestamp() {
            this.timeStamp = System.currentTimeMillis();
        }

        void delete() {
            if (hologramId == 0) {
                return;
            }

            WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(hologramId);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyPacket);
            hologramId = 0;
        }

    }

}
