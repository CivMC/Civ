package com.untamedears.citadel.listener;

import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class InventoryListener implements Listener {
  private Set<String> priorMessages_ = new TreeSet<String>();

  public PlayerReinforcement getReinforcement(Inventory inv) {
    // Returns reinforcement of the inventory's holder or null if none exists
    final InventoryHolder holder = inv.getHolder();
    Location loc;
    if (holder instanceof DoubleChest) {
      loc = ((DoubleChest)holder).getLocation();
    } else if (holder instanceof BlockState) {
      loc = ((BlockState)holder).getLocation();
    } else {
      // Entity or Vehicle inventories
      return null;
    }
    final AccessDelegate delegate = AccessDelegate.getDelegate(loc.getBlock());
    final IReinforcement rein = delegate.getReinforcement();
    if (rein instanceof PlayerReinforcement) {
      return (PlayerReinforcement)rein;
    }
    return null;
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
    // Prevent hopper minecarts from extracting from reinforced containers or
    //  filling up reinforced containers.
    // Prevent misowned hoppers from stealing from reinforced containers.
    final Inventory src = event.getSource();
    final PlayerReinforcement srcRein = getReinforcement(src);
    // Calculate destination
    final Inventory dest = event.getDestination();
    final PlayerReinforcement destRein = getReinforcement(dest);
    if (srcRein == null) {
      if (destRein == null) {
        // No reinforcements, allow
        return;
      } else {
        // Reinforcement mismatch, deny
        event.setCancelled(true);
        return;
      }
    } else if (destRein == null) {  // srcRein != null
      // Reinforcement mismatch, deny
      event.setCancelled(true);
      return;
    }
    // srcRein != null && destRein != null
    final Faction srcOwner = srcRein.getOwner();
    final Faction destOwner = destRein.getOwner();
    if (srcOwner == null || destOwner == null) {
      String msg;
      if (srcOwner == null) {
        msg = String.format("Null group srcOwner(%s)", srcRein.getOwnerName());
      } else {  // destOwner == null
        msg = String.format("Null group destOwner(%s)", destRein.getOwnerName());
      }
      if (!priorMessages_.contains(msg)) {
        Citadel.info(msg);
        priorMessages_.add(msg);
      }
      // Unable to determine reinforcement owner match, deny
      event.setCancelled(true);
      return;
    }
    if (srcOwner != destOwner) {
      // Reinforcement owners don't match, deny
      event.setCancelled(true);
      return;
    }
    // Reinforcement owners match, allow
  }
}
