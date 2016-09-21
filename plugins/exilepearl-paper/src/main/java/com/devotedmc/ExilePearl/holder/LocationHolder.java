package com.devotedmc.ExilePearl.holder;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;

/**
 * A location holding an exile pearl
 * @author Gordon
 *
 */
public class LocationHolder implements PearlHolder {

	private final Location l;
	
	/**
	 * Creates a new LocationHolder instance
	 * @param l The location
	 */
	public LocationHolder(final Location l) {
		this.l = l;
	}

	@Override
	public String getName() {
		return "nobody";
	}

	@Override
	public Location getLocation() {
		return l;
	}

	@Override
	public HolderVerifyResult validate(ExilePearl pearl, StringBuilder feedback) {
		 // Location holder
		Chunk chunk = l.getChunk();
		for (Entity entity : chunk.getEntities()) {
			if (entity instanceof Item) {
				Item item = (Item)entity;
				ItemStack is = item.getItemStack();

				if (pearl.validateItemStack(is)) {
					feedback.append(String.format("Found on ground at (%d,%d,%d)",
							entity.getLocation().getBlockX(),
							entity.getLocation().getBlockY(),
							entity.getLocation().getBlockZ()));
					return HolderVerifyResult.ON_GROUND;
				}
			}
		}
		feedback.append("On ground not in chunk");
		return HolderVerifyResult.ENTITY_NOT_IN_CHUNK;
	}
}
