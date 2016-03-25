package com.github.igotyou.FactoryMod.events;

import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.factories.Factory;

/**
 * Event called when any factory is moving around items. This will (hopefully)
 * make this plugin more compatible with other plugins which have listeners for
 * item move events
 *
 */
public class ItemTransferEvent extends InventoryMoveItemEvent {
	private Factory f;
	private Block fromBlock;
	private Block toBlock;

	public ItemTransferEvent(Factory f, Inventory fromInventory,
			Inventory toInventory, Block fromBlock, Block toBlock,
			ItemStack trans) {
		super(fromInventory, trans, toInventory, true);
		this.f = f;
		this.fromBlock = fromBlock;
		this.toBlock = toBlock;
	}

	/**
	 * @return The factory causing the transfer
	 */
	public Factory getFactory() {
		return f;
	}

	/**
	 * @return The source block from which the transfer is originating
	 */
	public Block getSourceBlock() {
		return fromBlock;
	}

	/**
	 * @return The target block to which the transfer is going
	 */
	public Block getTargetBlock() {
		return toBlock;
	}
}
