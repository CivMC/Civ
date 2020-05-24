package com.untamedears.itemexchange.events;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.chain;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.api.BlockAPI;

/**
 *
 */
public class BlockInventoryRequestEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Block block;
	private final Player requester;
	private Inventory inventory;

	public BlockInventoryRequestEvent(Block block, Player requester) {
		Preconditions.checkArgument(BlockAPI.isValidBlock(block));
		this.block = block;
		this.requester = requester;
		if (this.block.getType() == Material.ENDER_CHEST) {
			if (requester != null) {
				this.inventory = requester.getEnderChest();
			}
		}
		else {
			BlockInventoryHolder holder = chain(() -> (BlockInventoryHolder) block.getBlockData());
			if (holder != null) {
				this.inventory = holder.getInventory();
			}
		}
	}

	public Block getBlock() {
		return this.block;
	}

	public Player getRequester() {
		return this.requester;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public static BlockInventoryRequestEvent emit(Block block, Player requester) {
		BlockInventoryRequestEvent event = new BlockInventoryRequestEvent(block, requester);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

}
