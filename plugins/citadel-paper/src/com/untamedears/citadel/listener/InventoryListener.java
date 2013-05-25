package com.untamedears.citadel.listener;

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
import com.untamedears.citadel.ReinforcementManager;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

public class InventoryListener implements Listener {
  public Location getInvLoc(Inventory inv) {
    InventoryHolder holder = inv.getHolder();
    if (holder instanceof DoubleChest) {
      return ((DoubleChest)holder).getLocation();
    } else if (holder instanceof BlockState) {
      return ((BlockState)holder).getLocation();
    }
    return null;
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
    boolean cancel_event = false;
    Inventory initiator = event.getInitiator();
    InventoryHolder initiator_holder = initiator.getHolder();
    if (initiator_holder instanceof HumanEntity) {
      // Let it pass as we should never see a Player here
      Citadel.severe(String.format(
            "InventoryMoveItemEvent initiator == HumanEntity(%s), BUG",
            ((HumanEntity)initiator_holder).getName()));
          return;
    }
    Inventory src = event.getSource();
    Location src_loc = getInvLoc(src);
    if (src_loc == null) {
      // Likely it's a form of minecart, which can't be reinforced so allow
      return;
    }
    ReinforcementManager rm = Citadel.getReinforcementManager();
    IReinforcement src_rein = rm.getReinforcement(src_loc);
    if (src_rein == null || !(src_rein instanceof PlayerReinforcement)) {
      // No reinforcement on the source block, allow
      return;
    }
    Location initiator_loc = getInvLoc(initiator);
    if (initiator_loc == null) {
      // Likely it's a form of minecart so cancel the event
      event.setCancelled(true);
      return;
    }
    IReinforcement initiator_rein = rm.getReinforcement(initiator_loc);
    if (initiator_rein == null || !(initiator_rein instanceof PlayerReinforcement)) {
      // No reinforcement on the initiator block, deny
      event.setCancelled(true);
      return;
    }
    Faction src_owner = ((PlayerReinforcement)src_rein).getOwner();
    Faction initiator_owner = ((PlayerReinforcement)initiator_rein).getOwner();
    if (!src_owner.equals(initiator_owner)) {
      // Reinforcement owners don't match, deny
      event.setCancelled(true);
      return;
    }
  }
}
