package net.civmc.heliodor.farmbeacon;

import net.civmc.heliodor.BlockProtector;
import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.backpack.Backpack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class FarmBeaconListener implements Listener {

    public static final NamespacedKey FARM_BEACON_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "farm_beacon");

    public void protect(BlockProtector protector) {
        protector.addPredicate(l -> l.getBlock().getState(false) instanceof Beacon beacon
            && beacon.getPersistentDataContainer().has(FARM_BEACON_KEY));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!FarmBeacon.isFarmBeacon(item)) {
            return;
        }

        Block block = event.getBlock();
        Beacon type = (Beacon) block.getState(false);

        type.getPersistentDataContainer().set(FARM_BEACON_KEY, PersistentDataType.LONG, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.BEACON) {
            return;
        }

        Beacon state = (Beacon) block.getState(false);

        if (!state.getPersistentDataContainer().has(FARM_BEACON_KEY)) {
            return;
        }

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), FarmBeacon.createFarmBeacon());
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BEACON || action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        boolean flag = event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR || event.getPlayer().getInventory().getItemInOffHand().getType() != Material.AIR;
        boolean flag1 = event.getPlayer().isSneaking() && flag;
        if (flag1) {
            return;
        }

        Beacon state = (Beacon) block.getState(false);

        if (state.getPersistentDataContainer().has(FARM_BEACON_KEY)) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }
}
