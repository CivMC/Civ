package vg.civcraft.mc.citadel.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.ChatColor;
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
        private Hologram hologram;
        private Reinforcement reinforcement;
        private long timeStamp;
        private boolean hasPermission;
        private Location cachedPlayerLocation;
        private double cachedHealth;
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
            if (hologram != null) {
                return;
            }
            List<String> lines = createHoloContent(); // Create with content to avoid entity spawn delay
            hologram = DHAPI.createHologram("citadel-hologram-" + this.hashCode(), getHoloLocation(reinforcement, player), lines);
            cachedPlayerLocation = player.getLocation();
            hologram.setDefaultVisibleState(false);
            hologram.setShowPlayer(player);
        }

        private List<String> createHoloContent() {
            List<String> lines = new ArrayList<>();
            lines.add(ModeListener.formatHealth(reinforcement));
            if (hasPermission) {
                lines.add(ChatColor.LIGHT_PURPLE + reinforcement.getGroup().getName());
                lines.add(ChatColor.AQUA + reinforcement.getType().getName());
                if (!reinforcement.isMature()) {
                    lines.add(ModeListener.formatProgress(reinforcement.getCreationTime(), reinforcement.getType().getMaturationTime(), ""));
                }
            }
            return lines;
        }

        boolean update() {
            if (System.currentTimeMillis() - timeStamp > cullDelay) {
                delete();
                return false;
            }
            if (reinforcement.isBroken()) {
                delete();
                return false;
            }
            updateLocation();
            DHAPI.setHologramLines(hologram, createHoloContent());
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
            hologram.setLocation(updated);
        }

        void refreshTimestamp() {
            this.timeStamp = System.currentTimeMillis();
        }

        void delete() {
            hologram.delete();
            hologram = null;
        }

    }

}
