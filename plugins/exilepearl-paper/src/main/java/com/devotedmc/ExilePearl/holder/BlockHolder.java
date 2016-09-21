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

/**
 * A block holding an exile pearl
 * @author Gordon
 *
 */
public class BlockHolder implements PearlHolder {

	private final Block b;
	private final String name;
	
	/**
	 * Creates a new BlockHolder instance
	 * @param b The block containing the pearl
	 */
	public BlockHolder(final Block b) {
		this.b = b;
		
		switch (b.getType()) {
		case CHEST:
		case TRAPPED_CHEST:
			this.name = "a chest";
			break;
			
		case FURNACE:
			this.name =  "a furnace";
			break;
			
		case BREWING_STAND:
			this.name =  "a brewing stand";
			break;
			
		case DISPENSER:
			this.name =  "a dispenser";
			break;
			
		case ITEM_FRAME:
			this.name =  "a wall frame";
			break;
			
		case DROPPER:
			this.name =  "a dropper";
			break;
			
		case HOPPER:
			this.name =  "a hopper";
			break;
			
		case ENDER_CHEST:
			this.name =  "a chest";
			break;
			
		default:
			this.name = "a block";
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Location getLocation() {
		return b.getLocation();
	}

	@Override
	public HolderVerifyResult validate(ExilePearl pearl, StringBuilder feedback) {
		// Check the block state first
		Location bl = b.getLocation();
		BlockState bs = bl.getBlock().getState();
		if (bs == null) {
			feedback.append("BlockState is null");
			return HolderVerifyResult.BLOCK_STATE_NULL;
		}
		
		// Is the block an inventory block?
		Location bsLoc = bs.getLocation();
		if (!(bs instanceof InventoryHolder)) {
			feedback.append(String.format(
					"%s not inventory at (%d,%d,%d)", bs.getType().toString(),
					bsLoc.getBlockX(), bsLoc.getBlockY(), bsLoc.getBlockZ()));
			return HolderVerifyResult.NOT_BLOCK_INVENTORY;
		}
		
		// Is the item held?
		Inventory inv = ((InventoryHolder)bs).getInventory();
		for (HumanEntity viewer : inv.getViewers()) {
			ItemStack cursoritem = viewer.getItemOnCursor();
			if (pearl.validateItemStack(cursoritem)) {
				feedback.append(String.format("In hand of %s viewing chest at (%d,%d,%d)",
						viewer.getName(),
						b.getLocation().getBlockX(),
						b.getLocation().getBlockY(),
						b.getLocation().getBlockZ()));
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
}
