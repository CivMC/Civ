package isaac.bastion.listeners;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.destroystokyo.paper.event.entity.SlimePathfindEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.BastionBlockManager;
import java.util.Set;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobListener implements Listener {

    private final BastionBlockManager blockManager;

    public MobListener(BastionBlockManager blockManager) {
        this.blockManager = blockManager;
    }

    // Fix for https://git.lumine.io/mythiccraft/MythicMobs/-/issues/2158, otherwise only one listener would be necessary
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void on(MythicMobSpawnEvent event) {
        if (!event.getMobType().getInternalName().equals("Bleeze")) {
            return;
        }
        Set<BastionBlock> preblocking = blockManager.getBlockingBastions(event.getLocation(), b -> b.getType().isBlockMobs() && b.isMature());
        if (preblocking.isEmpty()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void on(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BUILD_WITHER) {
            return;
        }
        if (!(event.getEntity() instanceof Enemy)) {
            return;
        }
        Set<BastionBlock> preblocking = blockManager.getBlockingBastions(event.getLocation(), b -> b.getType().isBlockMobs() && b.isMature());
        if (preblocking.isEmpty()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityPathfind(EntityPathfindEvent event) {
        if (!(event.getEntity() instanceof Enemy)) {
            return;
        }
        Set<BastionBlock> blocking = blockManager.getBlockingBastions(event.getLoc(), b -> b.getType().isBlockMobs() && b.isMature());
        if (blocking.isEmpty()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSlimePathfind(SlimePathfindEvent event) {
        Slime slime = event.getEntity();
        Set<BastionBlock> blocking = blockManager.getBlockingBastions(slime.getLocation(), b -> b.getType().isBlockMobs() && b.isMature());
        if (blocking.isEmpty()) {
            return;
        }
        event.setCancelled(true);
    }
}
