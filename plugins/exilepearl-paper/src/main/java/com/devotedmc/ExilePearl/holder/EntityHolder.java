package com.devotedmc.ExilePearl.holder;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import com.devotedmc.ExilePearl.ExilePearl;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * An entity holding an exile pearl
 */
public class EntityHolder implements PearlHolder {

	private final Entity entity;

	/**
	 * Creates a new EntityHolder instance
	 * @param entity The entity holding the pearl
	 */
	public EntityHolder(final Entity entity) {
		Guard.ArgumentNotNull(entity, "entity");
		this.entity = entity;
	}

	@Override
	public String getName() {
		switch (entity.getType()) {
			case ITEM_FRAME:
				return "an item frame";
			default:
				return "an entity";
		}
	}

	@Override
	public Location getLocation() {
		return entity.getLocation();
	}

	@Override
	public HolderVerifyResult validate(final ExilePearl pearl) {
		if (entity instanceof ItemFrame) {
			if (pearl.validateItemStack(((ItemFrame) entity).getItem())) {
				return HolderVerifyResult.IN_ITEM_FRAME;
			}
		}
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
		EntityHolder other = (EntityHolder) o;
		return entity.equals(other.entity);
	}

	@Override
	public boolean isBlock() {
		return true;
	}
}
