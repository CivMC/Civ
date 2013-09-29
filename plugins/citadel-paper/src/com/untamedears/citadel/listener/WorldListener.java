package com.untamedears.citadel.listener;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.Citadel.VerboseMsg;
import com.untamedears.citadel.ReinforcementManager;

public class WorldListener implements Listener {
  @EventHandler(ignoreCancelled = true)
  public void onStructureGrow(StructureGrowEvent event) {
    ReinforcementManager rm = Citadel.getReinforcementManager();
    for (BlockState block_state : event.getBlocks()) {
      if (rm.getReinforcement(block_state.getLocation()) != null) {
        event.setCancelled(true);
        Citadel.verbose(
            VerboseMsg.ReinOvergrowth,
            block_state.getLocation().toString());
        return;
      }
    }
  }
}
