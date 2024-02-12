package vg.civcraft.mc.civmodcore.inventory.items;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {

	private Material material;
	private ItemMeta meta;
	private int amount;

	private ItemBuilder() {
		this.amount = 1;
	}

	/**
	 * @param material The material to set for this item.
	 * @return Returns this builder.
	 *
	 * @throws IllegalArgumentException Throws an IAE if the given material fails am
	 *                                  {@link ItemUtils#isValidItemMaterial(Material)} test.
	 */
	public ItemBuilder material(@Nonnull final Material material) {
		if (!ItemUtils.isValidItemMaterial(material)) {
			throw new IllegalArgumentException("That is not a valid item material!");
		}
		this.material = material;
		return this;
	}

	/**
	 * @param <T> The type to cast the item meta to.
	 * @param handler The item meta handler.
	 * @return Returns this builder.
	 *
	 * @throws NullPointerException Throws an NPE if a new item meta needs to be created, but the new meta is null.
	 * @throws ClassCastException Throws an CCE if the item meta cannot be cast to the inferred type.
	 */
	@SuppressWarnings("unchecked")
	public <T> ItemBuilder meta(@Nonnull final Consumer<T> handler) {
		if (this.meta == null || !Bukkit.getItemFactory().isApplicable(this.meta, this.material)) {
			this.meta = Objects.requireNonNull(
					Bukkit.getItemFactory().getItemMeta(this.material),
					"Tried to create an item meta for [" + this.material + "] but it returned null!");
		}
		handler.accept((T) this.meta);
		return this;
	}

	/**
	 * @param amount The amount to set for this item.
	 * @return Returns this builder.
	 *
	 * @throws IllegalArgumentException Throws an IAE if the given amount is less than or equal to zero.
	 */
	public ItemBuilder amount(final int amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Item amount cannot be less than or equal to zero!");
		}
		this.amount = amount;
		return this;
	}

	/**
	 * @return Returns a new ItemStack based on the builder.
	 */
	public ItemStack build() {
		final var item = new ItemStack(this.material, this.amount);
		item.setItemMeta(this.meta);
		return item;
	}

	/**
	 * Creates a new builder with the given material.
	 *
	 * @param material The material to set for the builder.
	 * @return Returns a new builder.
	 */
	public static ItemBuilder builder(@Nonnull final Material material) {
		return new ItemBuilder().material(material);
	}

}
