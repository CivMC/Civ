package net.civmc.heliodor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockProtector implements Listener {

    private final List<Predicate<Location>> predicates = new ArrayList<>();

    public void addPredicate(Predicate<Location> predicate) {
        this.predicates.add(predicate);
    }

    private boolean isProtected(Location location) {
        for (var predicate : predicates) {
            if (predicate.test(location)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isProtected(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isProtected(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> isProtected(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> isProtected(block.getLocation()));
    }
}
