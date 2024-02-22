package com.github.igotyou.FactoryMod.listeners;

import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class FactoryModListener implements Listener {
	private FactoryModManager manager;

	public FactoryModListener(FactoryModManager manager) {
		this.manager = manager;
	}

	/**
	 * Called when a block is broken If the block that is destroyed is part of a
	 * factory, call the required methods.
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void blockBreakEvent(BlockBreakEvent e) {
		Block block = e.getBlock();
		if (manager.isPossibleInteractionBlock(block.getType())) {
			Factory c = manager.getFactoryAt(block);
			if (c != null) {
				// let creative player interact without breaking it
				if (e.getPlayer().getGameMode() == GameMode.CREATIVE
						&&e.getPlayer().getInventory()
								.getItemInMainHand().getType() == manager.getFactoryInteractionMaterial()) {
					e.setCancelled(true);
					return;
				}
				c.getInteractionManager().blockBreak(e.getPlayer(), block);
			}
		}

	}

	@EventHandler
	public void redstoneChange(BlockRedstoneEvent evt) {
		if (evt.getOldCurrent() == evt.getNewCurrent()) {
			return;
		}
		Block powerSource = evt.getBlock();
		Material psType = powerSource.getType();
		if (psType == Material.REPEATER || psType == Material.COMPARATOR) {
			BlockData psData = powerSource.getState().getBlockData();
			BlockFace direction = ((Directional) psData).getFacing();
			// repeaters "face" their input apparently
			Block poweredBlock = powerSource.getRelative(direction.getOppositeFace());
			Factory f = manager.getFactoryAt(poweredBlock);
			if (f != null) {
				f.getInteractionManager().redStoneEvent(evt, poweredBlock);
			}
		} else {
			for (BlockFace direction : WorldUtils.ALL_SIDES) {
				Block poweredBlock = powerSource.getRelative(direction);
				Factory f = manager.getFactoryAt(poweredBlock);
				if (f != null) {
					f.getInteractionManager().redStoneEvent(evt, poweredBlock);
				}
			}
		}
	}

	/**
	 * Called when a entity explodes(creeper,tnt etc.) Nearly the same as
	 * blockBreakEvent
	 */
	@EventHandler(priority=EventPriority.HIGHEST)
	public void explosionListener(EntityExplodeEvent e) {
		List<Block> blocks = e.blockList();
		for (Block block : blocks) {
			if (manager.isPossibleInteractionBlock(block.getType())) {
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
	@EventHandler(priority=EventPriority.HIGHEST)
	public void burnListener(BlockBurnEvent e) {
		Block block = e.getBlock();
		if (manager.isPossibleInteractionBlock(block.getType())) {
			Factory c = manager.getFactoryAt(block);
			if (c != null) {
				c.getInteractionManager().blockBreak(null, block);
			}
		}
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent evt) {
		Block block = evt.getClickedBlock();
		Player player = evt.getPlayer();
		if (block != null && manager.isPossibleInteractionBlock(block.getType())) {
			BlockFace blockFace = evt.getBlockFace();
			Factory factory = manager.getFactoryAt(block);
			if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (factory != null) {
					factory.getInteractionManager().rightClick(player, block, blockFace);
				} else {
					// check if chest is other half of double chest
					if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
						for (Block b : MultiBlockStructure.searchForBlockOnSides(block, block.getType())) {
							Factory f = manager.getFactoryAt(b);
							if (f != null) {
								f.getInteractionManager().rightClick(player, b, blockFace);
							}
						}
					}
				}
			}
			if (evt.getAction() == Action.LEFT_CLICK_BLOCK) {
				if (factory == null) {
					if (manager.isPossibleCenterBlock(block.getType())) {
						if (player.getInventory().getItemInMainHand().getType() == manager
								.getFactoryInteractionMaterial()) {
							manager.attemptCreation(block, player);
						}
					} else {
						// check if chest is other half of double chest
						if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
							for (Block b : MultiBlockStructure.searchForBlockOnAllSides(block, block.getType())) {
								Factory f = manager.getFactoryAt(b);
								if (f != null) {
									f.getInteractionManager().leftClick(player, b, blockFace);
								}
							}
						}
					}
				} else {
					factory.getInteractionManager().leftClick(player, block, blockFace);
				}

			}
		}
	}

	/**
	 * Allow shift-clicking valid fuel into the smelting slot of a factories furnace
	 * @param event
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			boolean clickedTop = event.getRawSlot() < event.getView().getTopInventory().getSize();
			InventoryHolder holder = !clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();
			if (holder instanceof Furnace) {
				Block furnace = ((Furnace) holder).getBlock();
				if (manager.isPossibleInteractionBlock(furnace.getType())) {
					Factory factory = manager.getFactoryAt(furnace.getLocation());
					if (factory == null) {
						return;
					}
					ItemStack fuel = ((FurnacePowerManager) factory.getPowerManager()).getFuel();
					if (fuel == null) {
						return;
					}
					FurnaceInventory inv = (FurnaceInventory) holder.getInventory();
					Player p = (Player) event.getWhoClicked();
					ItemStack clicked = event.getCurrentItem();
					moveFuelToSmeltingSlot(inv, p, fuel, clicked);
				}
			}
		}
	}

	public void moveFuelToSmeltingSlot(FurnaceInventory inv, Player p, ItemStack fuel, ItemStack clicked) {
		if (clicked != null && clicked.getType() == fuel.getType()) {
			// Check (bottom) fuel slot is filled
			if (inv.getFuel() != null && inv.getFuel().getAmount() == inv.getFuel().getMaxStackSize()) {
				ItemStack smeltingSlot = inv.getSmelting();
				// Check (top) smelting slot has space
				if (smeltingSlot == null || (smeltingSlot.getType() == fuel.getType() && smeltingSlot.getAmount() < smeltingSlot.getMaxStackSize())) {
					int oldSlotAmount = smeltingSlot == null ? 0 : smeltingSlot.getAmount();
					int newSlotAmount = Math.min(oldSlotAmount + clicked.getAmount(), fuel.getMaxStackSize());
					inv.setSmelting(new ItemStack(fuel.getType(), newSlotAmount));
					clicked.setAmount(clicked.getAmount() + (oldSlotAmount - newSlotAmount));
					p.updateInventory();
				}
			}
		}
	}

	@EventHandler
	public void blockDispenser(BlockDispenseEvent e) {
		if (manager.getFactoryAt(e.getBlock()) != null) {
			e.setCancelled(true);
		}
	}
}
