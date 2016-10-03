package com.devotedmc.ExilePearl.holder;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * A location holding an exile pearl
 * @author Gordon
 *
 */
public class ItemHolder implements PearlHolder {

	private final Item item;
	
	/**
	 * Creates a new LocationHolder instance
	 * @param loc The location
	 */
	public ItemHolder(final Item item) {
		Guard.ArgumentNotNull(item, "item");
		
		this.item = item;
	}

	@Override
	public String getName() {
		return "nobody";
	}

	@Override
	public Location getLocation() {
		return item.getLocation();
	}

	@Override
	public HolderVerifyResult validate(ExilePearl pearl) {
		 // Location holder
		Chunk chunk = item.getLocation().getChunk();
		
		for (Entity entity : chunk.getEntities()) {
			if (entity.equals(item)) {
				if (pearl.validateItemStack(item.getItemStack())) {
					return HolderVerifyResult.ON_GROUND;
				}
				return HolderVerifyResult.ON_GROUND;
			}
		}
		return HolderVerifyResult.ENTITY_NOT_IN_CHUNK;
	}
	
	@Override
	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemHolder other = (ItemHolder) o;

		return item.equals(other.item);
	}
}
