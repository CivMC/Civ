package com.untamedears.realisticbiomes.listener;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BonemealListener implements Listener {

	private Set<Material> preventedMaterials;

	public BonemealListener(Collection<Material> preventedMaterials) {
		this.preventedMaterials = EnumSet.copyOf(preventedMaterials);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
			return;
		}
		if (event.getItem() == null || event.getItem().getType() != Material.BONE_MEAL) {
			return;
		}
		if (preventedMaterials.contains(event.getClickedBlock().getType())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDispenser(BlockDispenseEvent event) {
		if (event.getItem() == null || event.getItem().getType() != Material.BONE_MEAL) {
			return;
		}
		Dispenser dispenser = (Dispenser) event.getBlock().getBlockData();
		Block dispensedInto = event.getBlock().getRelative(dispenser.getFacing());
		if (preventedMaterials.contains(dispensedInto.getType())) {
			event.setCancelled(true);
		}
	}
}
