package com.devotedmc.ExilePearl.holder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * A block holding an exile pearl
 * @author Gordon
 *
 */
public class BlockHolder implements PearlHolder {

	private final Block block;
	
	/**
	 * Creates a new BlockHolder instance
	 * @param b The block containing the pearl
	 */
	public BlockHolder(final Block block) {
		Guard.ArgumentNotNull(block, "block");
		
		this.block = block;
	}

	@Override
	public String getName() {
		switch (block.getType()) {
		case CHEST:
		case TRAPPED_CHEST:
		case ENDER_CHEST:
			return "a chest";
			
		case FURNACE:
			return "a furnace";
			
		case BREWING_STAND:
			return "a brewing stand";
			
		case DISPENSER:
			return "a dispenser";
			
		case ITEM_FRAME:
			return "a wall frame";
			
		case DROPPER:
			return "a dropper";
			
		case HOPPER:
			return "a hopper";
			
		case ENCHANTMENT_TABLE:
			return "an enchantment table";
			
		default:
			return "a block";
		}
	}

	@Override
	public Location getLocation() {
		return block.getLocation();
	}

	@Override
	public HolderVerifyResult validate(final ExilePearl pearl) {
		// Check the block state first
		BlockState bs = block.getState();
		if (bs == null) {
			return HolderVerifyResult.BLOCK_STATE_NULL;
		}
		
		// Is the block an inventory block?
		if (!(bs instanceof InventoryHolder)) {
			return HolderVerifyResult.NOT_BLOCK_INVENTORY;
		}
		
		// Is the item held?
		Inventory inv = ((InventoryHolder)bs).getInventory();
		for (HumanEntity viewer : inv.getViewers()) {
			ItemStack cursoritem = viewer.getItemOnCursor();
			if (pearl.validateItemStack(cursoritem)) {
			return HolderVerifyResult.IN_VIEWER_HAND;
			}
		}
		
		// In the container inventory?
		for (ItemStack item : inv.all(Material.ENDER_PEARL).values()) {
			if (pearl.validateItemStack(item)) {
				return HolderVerifyResult.IN_CHEST;
			}
		}
		
		// Nope, not found
		return HolderVerifyResult.DEFAULT;
	}
	
	@Override
	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlockHolder other = (BlockHolder) o;

		return block.equals(other.block);
	}

	@Override
	public boolean isBlock() {
		return true;
	}
}
