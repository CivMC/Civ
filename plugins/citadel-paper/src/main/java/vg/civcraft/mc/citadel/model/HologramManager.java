package vg.civcraft.mc.citadel.model;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.papermc.paper.adventure.AdventureComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
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

    private Map<Location, Map<UUID, PlayerHolo>> holograms;
    private Set<PlayerHolo> activeHolos;
    private CitadelSettingManager settingMan;

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

        private Player player;
        private int hologramId;
        private Reinforcement reinforcement;
        private long timeStamp;
        private boolean hasPermission;
        private Location cachedPlayerLocation;
        private long cullDelay;

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

            ProtocolManager protocolLib = ProtocolLibrary.getProtocolManager();
            PacketContainer fakeEntity = protocolLib.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            Location location = getHoloLocation(reinforcement, player);
            fakeEntity.getIntegers()
                .write(0, hologramId)
                .write(1, 0)
                .write(2, 0)
                .write(3, 0)
                .write(4, 0);

            fakeEntity.getUUIDs().write(0, UUID.randomUUID());

            fakeEntity.getEntityTypeModifier()
                .write(0, EntityType.TEXT_DISPLAY);

            fakeEntity.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

            fakeEntity.getBytes()
                .write(0, (byte) 0)
                .write(1, (byte) 0)
                .write(2, (byte) 0);

            protocolLib.sendServerPacket(player, fakeEntity);
            sendUpdatePacket();
            cachedPlayerLocation = player.getLocation();
        }

        private void sendUpdatePacket() {
            ProtocolManager protocolLib = ProtocolLibrary.getProtocolManager();

            PacketContainer fakeMetadata = protocolLib.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            fakeMetadata.getIntegers().write(0, hologramId);

            List<WrappedDataValue> values = new ArrayList<>();
            values.add(new WrappedDataValue(10, WrappedDataWatcher.Registry.get(Integer.class), 2));
            values.add(new WrappedDataValue(15, WrappedDataWatcher.Registry.get(Byte.class), (byte) 3));
            values.add(new WrappedDataValue(23, WrappedDataWatcher.Registry.getChatComponentSerializer(),
                new AdventureComponent(createHoloContent())));
//            values.add(new WrappedDataValue(25, WrappedDataWatcher.Registry.get(Integer.class), 0));
            // Add shadow when MC-260529 is fixed
            values.add(new WrappedDataValue(27, WrappedDataWatcher.Registry.get(Byte.class), (byte) (0x02)));
            fakeMetadata.getDataValueCollectionModifier().write(0, values);

            protocolLib.sendServerPacket(player, fakeMetadata);
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
            Location updated = getHoloLocation(reinforcement, player);
            ProtocolManager protocolLib = ProtocolLibrary.getProtocolManager();

            PacketContainer fakeTeleport = protocolLib.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
            fakeTeleport.getIntegers().write(0, hologramId);

            fakeTeleport.getModifier().withType(PositionMoveRotation.class)
                .write(0,
                    new PositionMoveRotation(
                        new Vec3(updated.getX(), updated.getY(), updated.getZ()),
                        new Vec3(0, 0, 0),
                        0,
                        0));

            fakeTeleport.getModifier().withType(Set.class)
                    .write(0, Relative.DELTA);

            fakeTeleport.getBooleans()
                .write(0, false);

            protocolLib.sendServerPacket(player, fakeTeleport);
        }

        void refreshTimestamp() {
            this.timeStamp = System.currentTimeMillis();
        }

        void delete() {
            if (hologramId == 0) {
                return;
            }
            ProtocolManager protocolLib = ProtocolLibrary.getProtocolManager();

            PacketContainer fakeDestroy = protocolLib.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            fakeDestroy.getIntLists().write(0, List.of(hologramId));

            protocolLib.sendServerPacket(player, fakeDestroy);
            hologramId = 0;
        }

    }

}
