package com.untamedears.realisticbiomes.replant;

import com.untamedears.realisticbiomes.RealisticBiomes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.untamedears.realisticbiomes.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

public class AutoReplantListener implements Listener {

	private final boolean rightClick;

	private BooleanSetting toggleAutoReplant;

	public AutoReplantListener(boolean rightClick) {
		this.rightClick = rightClick;
		initSettings();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (rightClick) {
			return;
		}

		Block block = event.getBlock();
		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();
		if (!getToggleAutoReplant(player.getUniqueId())) {
			return;
		}
		Material seed = getSeed(block.getType());
		if (seed == null) {
			return;
		}
		if (!isFullyGrown(block)) {
			return;
		}
		if (!playerHasSeeds(inventory, seed)) {
			return;
		}
		replantCrop(block, seed, inventory);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onRightClick(PlayerInteractEvent event) {
		if (!rightClick
				|| event.getHand() != EquipmentSlot.HAND) // Process only the main hand (no need to show 'no perms' twice)
		{
			return;
		}

		Block block = event.getClickedBlock();
		if (block == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		ItemStack item = event.getItem();
		if (item != null && item.getType() == Constants.Stick) // Do not auto-replant when the player just want to get growth info
			return;

		Player player = event.getPlayer();
		if (!getToggleAutoReplant(player.getUniqueId())) {
			return;
		}

		Material seed = getSeed(block.getType());
		if (seed == null) {
			return;
		}
		if (!isFullyGrown(block)) {
			return;
		}

		replantCropFromDrops(event, seed);
	}

	/**
	 * Takes a Material and checks if its a crop, returns seeds if it is, null if it isn't.
	 * @return Seed Material
	 */
	private Material getSeed(Material material) {
		return switch (material) {
			case WHEAT -> Material.WHEAT_SEEDS;
			case CARROTS -> Material.CARROT;
			case POTATOES -> Material.POTATO;
			case BEETROOTS -> Material.BEETROOT_SEEDS;
			case NETHER_WART -> Material.NETHER_WART;
			case COCOA -> Material.COCOA_BEANS;
			default -> null;
		};
	}

	/**
	 * Checks a players inventory for a given seed
	 * @param inventory PlayerInventory
	 * @param seeds Seed we are looking for
	 * @return true if found
	 */
	private boolean playerHasSeeds(PlayerInventory inventory, Material seeds) {
		ItemStack[] items = inventory.getContents();
		for (ItemStack item : items) {
			if (item == null) {
				continue;
			}
			if (item.getType() == seeds) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Runnable that replants the crop 5 ticks later, also updates PlantLogicManager to handle plant creation
	 */
	private void replantCrop(Block block, Material seed, PlayerInventory inventory) {
		Material plant = block.getType();
		BlockData data = block.getBlockData();
		Bukkit.getScheduler().runTaskLater(RealisticBiomes.getInstance(), () -> {
			if (!MaterialUtils.isAir(block.getType())) {
				return;
			} else if (!removeSeedFromPlayerInv(inventory, seed)){
				return;
			}
			resetCropState(block, data, plant);
			RealisticBiomes.getInstance().getPlantLogicManager().handlePlantCreation(block, new ItemStack(seed));
		},5L);
	}

	private void replantCropFromDrops(PlayerInteractEvent event, Material seed) {
		Block block = event.getClickedBlock();
		// Subtract one seed from the drops, to be used for replanting
		List<ItemStack> drops = new ArrayList<>(block.getDrops(event.getItem()));
		Iterator<ItemStack> it = drops.iterator();
		boolean hasRemovedSeed = false;
		while (it.hasNext()) {
			ItemStack drop = it.next();
			if (drop.getType() == seed) {
				int amount = drop.getAmount();
				if (amount >= 1) {
					hasRemovedSeed = true;
					if (amount == 1) {
						it.remove();
					} else {
						drop.setAmount(amount - 1);
					}
				}
			}
		}

		if (!hasRemovedSeed) {
			return;
		}

		// Citadel will pick this up and cancel if we don't have permission
		PlayerHarvestBlockEvent harvestBlockEvent = new PlayerHarvestBlockEvent(event.getPlayer(), block, drops);
		Bukkit.getPluginManager().callEvent(harvestBlockEvent);
		if (harvestBlockEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		resetCropState(block, block.getBlockData(), block.getType());

		for (ItemStack drop : drops) {
			block.getWorld().dropItemNaturally(block.getLocation(), drop);
		}
		RealisticBiomes.getInstance().getPlantLogicManager().handlePlantCreation(block, new ItemStack(seed));
		event.setUseItemInHand(Result.DENY); // Don't let player place a block on top of the crop
	}

	private void resetCropState(Block block, BlockData previousData, Material plant) {
		if (plant == Material.COCOA) {
			BlockFace previousFacing = ((Cocoa) previousData).getFacing();
			block.setType(plant);
			Cocoa cocoa = (Cocoa) block.getBlockData();
			cocoa.setFacing(previousFacing);
			block.setBlockData(cocoa);
		} else {
			block.setType(plant);
		}
	}

	private boolean removeSeedFromPlayerInv(PlayerInventory inventory, Material seed) {
		ItemStack[] items = inventory.getContents();
		for (ItemStack item : items) {
			if (item == null) {
				continue;
			}
			if (item.getType() == seed) {
				item.setAmount(item.getAmount() - 1);
				return true;
			}
		}
		return false;
	}

	private boolean isFullyGrown(Block block) {
		Ageable crop = (Ageable) block.getBlockData();
		return crop.getAge() == crop.getMaximumAge();
	}

	private void initSettings() {
		MenuSection rbMenu = PlayerSettingAPI.getMainMenu()
				.createMenuSection("RealisticBiomes", "Auto replant setting", new ItemStack(
						Material.WHEAT_SEEDS));
		toggleAutoReplant = new BooleanSetting(RealisticBiomes.getInstance(), true, "Use auto replant?", "autoReplant",
				rightClick ? "Will automatically harvest and replant a crop when right clicked" : "Will automatically take seeds from your inventory and replant crops");
		PlayerSettingAPI.registerSetting(toggleAutoReplant, rbMenu);
	}

	public boolean getToggleAutoReplant(UUID uuid) {
		return toggleAutoReplant.getValue(uuid);
	}
}
