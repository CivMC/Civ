package com.untamedears.JukeAlert.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;
import com.untamedears.JukeAlert.util.QTBox;
import com.untamedears.JukeAlert.util.SparseQuadTree;

public class SnitchManager {

    private JukeAlert plugin;
    private JukeAlertLogger logger;
    private Map<Integer, Snitch> snitchesById;
    private Map<World, SparseQuadTree> snitches;

    public SnitchManager() {
        plugin = JukeAlert.getInstance();
        logger = plugin.getJaLogger();
    }

    public void loadSnitches() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                snitchesById = new TreeMap<Integer, Snitch>();
                snitches = new HashMap<World, SparseQuadTree>();
                List<World> worlds = plugin.getServer().getWorlds();
                for (World world : worlds) {
                    SparseQuadTree worldSnitches = new SparseQuadTree();
                    Enumeration<Snitch> se = logger.getAllSnitches(world);
                    while (se.hasMoreElements()) {
                        Snitch snitch = se.nextElement();
                        snitchesById.put(snitch.getId(), snitch);
                        worldSnitches.add(snitch);
                    }
                    snitches.put(world, worldSnitches);
                }
            }
        });
    }

    public void saveSnitches() {
        this.logger.saveAllSnitches();
    }

    public Collection<Snitch> getAllSnitches() {
        return this.snitchesById.values();
    }

    public void setSnitches(Map<World, SparseQuadTree> snitches) {
        this.snitches = snitches;
    }

    public Snitch getSnitch(int snitch_id) {
        return this.snitchesById.get(snitch_id);
    }

    public Snitch getSnitch(World world, Location location) {
        Set<? extends QTBox> potentials = snitches.get(world).find(location.getBlockX(), location.getBlockZ());
        for (QTBox box : potentials) {
            Snitch sn = (Snitch)box;
            if (sn.at(location)) {
                return sn;
            }
        }
        return null;
    }

    public void addSnitch(Snitch snitch) {
        World world = snitch.getLoc().getWorld();
        if (snitches.get(world) == null) {
            SparseQuadTree map = new SparseQuadTree();
            map.add(snitch);
            snitches.put(world, map);
        } else {
            snitches.get(world).add(snitch);
        }
        snitchesById.put(snitch.getId(), snitch);
    }

    public void removeSnitch(Snitch snitch) {
        snitches.get(snitch.getLoc().getWorld()).remove(snitch);
        snitchesById.remove(snitch.getId());
    }

    public Set<Snitch> findSnitches(World world, Location location) {
        if (snitches.get(world) == null) {
            return new TreeSet<Snitch>();
        }
        int y = location.getBlockY();
        Set<Snitch> results = new TreeSet<Snitch>();
        Set<QTBox> found = snitches.get(world).find(location.getBlockX(), location.getBlockZ());
        for (QTBox box : found) {
            Snitch sn = (Snitch)box;
            if (sn.isWithinHeight(location.getBlockY())) {
                results.add(sn);
            }
        }
        return results;
    }
}
