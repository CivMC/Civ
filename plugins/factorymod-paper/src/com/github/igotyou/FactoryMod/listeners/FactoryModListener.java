package com.github.igotyou.FactoryMod.listeners;

import java.util.List;

import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PossibleFishingResult;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

import com.github.igotyou.FactoryMod.Factory;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.StringUtils;

public class FactoryModListener implements Listener {
	private FactoryModManager manager;
	private ReinforcementManager rm = Citadel.getReinforcementManager();

	public FactoryModListener(FactoryModManager manager) {
		this.manager = manager;
	}

	/**
	 * Called when a block is broken If the block that is destroyed is part of a
	 * factory, call the required methods.
	 */
	@EventHandler
	public void blockBreakEvent(BlockBreakEvent e) {
		Block block = e.getBlock();
		if (manager.isPossibleInteractionBlock(block.getType())
				&& ((FactoryModPlugin.CITADEL_ENABLED && !rm
						.isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)) {
			Factory c = manager.getFactoryAt(block);
			if (c != null) {
				c.getInteractionManager().blockBreak(e.getPlayer(), block);
			}
		}

	}

	/**
	 * Called when a entity explodes(creeper,tnt etc.) Nearly the same as
	 * blockBreakEvent
	 */
	@EventHandler
	public void explosionListener(EntityExplodeEvent e) {
		List<Block> blocks = e.blockList();
		for (Block block : blocks) {
			if (manager.isPossibleInteractionBlock(block.getType())
					&& ((FactoryModPlugin.CITADEL_ENABLED && !rm
							.isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)) {
				Factory c = manager.getFactoryAt(block);
				if (c != null) {
					c.getInteractionManager().blockBreak(null, block);
				}
			}
		}
	}

	/**
	 * Called when a block burns Nearly the same as blockBreakEvent
	 */
	@EventHandler
	public void burnListener(BlockBurnEvent e) {
		Block block = e.getBlock();
		if (manager.isPossibleInteractionBlock(block.getType())
				&& ((FactoryModPlugin.CITADEL_ENABLED && !rm
						.isReinforced(block)) || !FactoryModPlugin.CITADEL_ENABLED)) {
			Factory c = manager.getFactoryAt(block);
			if (c != null) {
				c.getInteractionManager().blockBreak(null, block);
			}
		}
	}


	public void playerInteract(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		Player player = e.getPlayer();
		if (manager.isPossibleInteractionBlock(block.getType())
				&& player.getItemInHand().getType() == FactoryModPlugin.FACTORY_INTERACTION_MATERIAL) {
			Factory c = manager.getFactoryAt(block);
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (c != null) {
					c.getInteractionManager().rightClick(player, block);
				}
			}
			if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
				if (c == null) {
					if (manager.isPossibleCenterBlock(block.getType())) {
						manager.attemptCreation(block, player);
					}
				} else {
					c.getInteractionManager().leftClick(player, block);
				}

			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void handlePortalTelportEvent(PlayerPortalEvent e) {
		if (e.isCancelled()) {
			return;
		}

		// Disable normal nether portal teleportation
		if (e.getCause() == TeleportCause.NETHER_PORTAL
				&& FactoryModPlugin.DISABLE_PORTALS) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerTeleportEvent(PlayerTeleportEvent e) {
		if (e.isCancelled() || e.getCause() != TeleportCause.NETHER_PORTAL) {
			return;
		}

		// Disable normal nether portal teleportation
		if (FactoryModPlugin.DISABLE_PORTALS) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void entityTeleportEvent(EntityPortalEvent event) {
		if (FactoryModPlugin.DISABLE_PORTALS) {
			event.setCancelled(true);
		}
	}

	/**
	 * Turns slabs with the lore "Smooth double slab" into smooth double slab
	 * blocks and logs with the lore "6-sided log" into logs with the log
	 * texture on all 6 sides
	 * 
	 * @param e
	 */
	@EventHandler
	public void onSpecialBlockUse(BlockPlaceEvent e) {
		org.bukkit.inventory.ItemStack is = e.getItemInHand();
		if (!is.hasItemMeta() || !is.getItemMeta().hasLore()) {
			return;
		}
		Material material = e.getBlock().getType();
		ItemMeta blockMeta = is.getItemMeta();
		switch (material) {
		case STEP:
			if (blockMeta.getLore().get(0).equals("Smooth double slab")) {
				byte type = (byte) (is.getDurability() + 8);
				e.getBlock().setTypeIdAndData(Material.DOUBLE_STEP.getId(),
						type, true);
			}
			break;
		case LOG:
		case LOG_2:
			if (blockMeta.getLore().get(0).equals("Sixsided log")) {
				byte type = (byte) ((is.getDurability() % 4) + 12);
				e.getBlock().setTypeIdAndData(material.getId(), type, true);
			}

			break;
		default:
			return;
		}

	}
}
