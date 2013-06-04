package com.untamedears.JukeAlert.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Map<World, SparseQuadTree> snitches;

    public SnitchManager() {
        plugin = JukeAlert.getInstance();
        logger = plugin.getJaLogger();
    }

    public void loadSnitches() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                snitches = logger.getAllSnitches();
            }
        });
    }

    public void saveSnitches() {
        logger.saveAllSnitches();
    }

    public Map<World, SparseQuadTree> getAllSnitches() {
        return snitches;
    }

    public void setSnitches(Map<World, SparseQuadTree> snitches) {
        this.snitches = snitches;
    }

    public Snitch getSnitch(World world, Location location) {
        Set<? extends QTBox> potentials = snitches.get(world).find(location.getBlockX(), location.getBlockZ());
        for (QTBox box : potentials) {
            Snitch sn = (Snitch) box;
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
    }

    public void removeSnitch(Snitch snitch) {
        snitches.get(snitch.getLoc().getWorld()).remove(snitch);
    }

    public Set<Snitch> findSnitches(World world, Location location) {
        if (snitches.get(world) == null) {
            return new TreeSet<Snitch>();
        }
        int y = location.getBlockY();
        Set<Snitch> results = new TreeSet<Snitch>();
        Set<QTBox> found = snitches.get(world).find(location.getBlockX(), location.getBlockZ());
        for (QTBox box : found) {
            Snitch sn = (Snitch) box;
            if (sn.isWithinHeight(location.getBlockY())) {
                results.add(sn);
            }
        }
        return results;
    }
}
