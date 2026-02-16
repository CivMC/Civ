package isaac.bastion.listeners;

import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.BastionBlockManager;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobListener implements Listener {

    private final BastionBlockManager blockManager;

    public MobListener(BastionBlockManager blockManager) {
        this.blockManager = blockManager;
    }

    // Fix for https://git.lumine.io/mythiccraft/MythicMobs/-/issues/2140, otherwise only one listener would be necessary
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void on(MythicMobSpawnEvent event) {
        if (!event.getMobType().getInternalName().equals("Bleeze")) {
            return;
        }
        Set<BastionBlock> preblocking = blockManager.getBlockingBastions(event.getLocation(), b -> b.getType().isBlockMobs());
        if (preblocking.isEmpty()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void on(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }
        Class<? extends Entity> clazz = event.getEntityType().getEntityClass();
        if (clazz == null || !Monster.class.isAssignableFrom(clazz)) {
            return;
        }
        Set<BastionBlock> preblocking = blockManager.getBlockingBastions(event.getLocation(), b -> b.getType().isBlockMobs());
        if (preblocking.isEmpty()) {
            return;
        }

        event.setCancelled(true);
    }
}
