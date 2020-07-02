package com.untamedears.itemexchange.events;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import vg.civcraft.mc.civmodcore.api.BlockAPI;

/**
 * <p>Event that's emitted to show ItemExchange's interest in accessing a block's inventory.</p>
 *
 * <p>This event is used as part of {@code /iecreate} whereby a shop is created by looking at a shop compatible block,
 * but as compatible blocks are configurable they could be anything, even blocks that do not have standard inventories.
 * Additionally there may be third party security plugins that do not wish to give the creating player access to that
 * block's inventory, thus this event is used as a bridge.</p>
 */
public class BlockInventoryRequestEvent extends Event implements Cancellable {

	public enum Purpose {
		INSPECTION,
		ACCESS
	}

	private static final HandlerList handlers = new HandlerList();

	private final Block block;
	private final Player requester;
	private final Purpose purpose;
	private Inventory inventory;
	private boolean cancelled;

	private BlockInventoryRequestEvent(Block block, Player requester, Purpose purpose) {
		Preconditions.checkArgument(BlockAPI.isValidBlock(block), "Block must be valid!");
		Preconditions.checkArgument(purpose != null, "Access request must have a purpose!");
		Preconditions.checkArgument(!(purpose == Purpose.ACCESS && requester == null),
				"Access requests must have a requester!");
		this.block = block;
		this.requester = requester;
		this.purpose = purpose;
		if (this.block.getType() == Material.ENDER_CHEST) {
			if (requester != null) {
				this.inventory = requester.getEnderChest();
			}
		}
		else {
			BlockState state = block.getState();
			if (state instanceof BlockInventoryHolder) {
				this.inventory = ((BlockInventoryHolder) state).getInventory();
			}
		}
	}

	/**
	 * Retrieves the block that the inventory should be bound to.
	 *
	 * @return Returns the block the inventory should be bound to.
	 */
	public Block getBlock() {
		return this.block;
	}

	/**
	 * Retrieves the player requesting the inventory, if relevant. If the purpose is access, then a requester must be
	 * set.
	 *
	 * @return Returns the player requesting the inventory, which may be null.
	 */
	public Player getRequester() {
		return this.requester;
	}

	/**
	 * Retrieves the purpose of this inventory request.
	 *
	 * @return Returns the purpose of this inventory request.
	 */
	public Purpose getPurpose() {
		return this.purpose;
	}

	/**
	 * Retrieves the inventory bound to the block.
	 *
	 * @return Returns the inventory bound to the block.
	 */
	public Inventory getInventory() {
		return this.inventory;
	}

	/**
	 * <p>Fulfils the event's request for an inventory.</p>
	 *
	 * <p>Note: Fulfilment and assignment do not modify the block, nor store data. It holds the inventory in memory
	 * until it is retrieved after all handlers have been processed.</p>
	 *
	 * @param inventory The inventory to assign to the block. Set as null if you wish to deny access to the block's
	 *     inventory.
	 */
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * <p>Creates and emits a block inventory request.</p>
	 *
	 * <p>Note: The event automatically retrieves any vanilla block inventory.</p>
	 *
	 * @param block The block to retrieve the inventory from.
	 * @param requester The inventory requester.
	 * @param purpose The purpose of this inventory request.
	 * @return Returns the request event that was emitted and has finished processing.
	 */
	public static BlockInventoryRequestEvent emit(Block block, Player requester, Purpose purpose) {
		BlockInventoryRequestEvent event = new BlockInventoryRequestEvent(block, requester, purpose);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

}
