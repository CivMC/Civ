package vg.civcraft.mc.civmodcore.inventory.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

/**
 * Class of static APIs for Items. Replaces ISUtils.
 */
@UtilityClass
public final class ItemUtils {

	/**
	 * @param item The item to get a translatable component for.
	 * @return Returns a translatable component of the given item.
	 */
	@Nonnull
	public static TranslatableComponent asTranslatable(@Nonnull final ItemStack item) {
		return Component.translatable(item.translationKey());
	}

	/**
	 * Gets the name of an item based off a material, e.g: POLISHED_GRANITE to Polished Granite
	 *
	 * @param material The material to get the name of.
	 * @return Returns the material name.
	 *
	 * @deprecated Use {@link MaterialUtils#asTranslatable(Material)} instead.
	 */
	@Deprecated
	@Nonnull
	public static String getItemName(@Nonnull final Material material) {
		return ChatUtils.stringify(MaterialUtils.asTranslatable(Objects.requireNonNull(material)));
	}

	/**
	 * Gets the name of an item either based off its material or its custom item tag.
	 *
	 * @param item The item to get the name of.
	 * @return Returns the item's name.
	 *
	 * @deprecated Use {@link #asTranslatable(ItemStack)} instead.
	 */
	@Deprecated
	@Nullable
	public static String getItemName(@Nullable final ItemStack item) {
		return item == null ? null : ChatUtils.stringify(asTranslatable(item));
	}

	/**
	 * Checks whether the given item can be interpreted as an empty slot.
	 *
	 * @param item The item to check.
	 * @return Returns true if the item can be interpreted as an empty slot.
	 */
	public static boolean isEmptyItem(final ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}

	/**
	 * Checks if an ItemStack is valid. An ItemStack is considered valid if when added to an inventory, it shows as an
	 * item with an amount within appropriate bounds. Therefore {@code new ItemStack(Material.AIR)} will not be
	 * considered valid, nor will {@code new ItemStack(Material.STONE, 80)}
	 *
	 * @param item The item to validate.
	 * @return Returns true if the item is valid.
	 */
	public static boolean isValidItem(@Nullable final ItemStack item) {
		return !isEmptyItem(item)
				&& isValidItemMaterial(item.getType())
				&& isValidItemAmount(item);
	}

	/**
	 * Checks if an ItemStack is valid instance in that it's non-null, has a valid item material, and has a positive
	 * item amount. This differs from {@link #isValidItem(ItemStack)} in that the maximum stack size isn't considered,
	 * so an item considered valid by this method may not be considered valid by {@link #isValidItem(ItemStack)}, thus
	 * may not appear correctly in inventories.
	 *
	 * @param item The item to validate.
	 * @return Returns true if the item is valid.
	 */
	public static boolean isValidItemIgnoringAmount(@Nullable final ItemStack item) {
		return item != null
				&& isValidItemMaterial(item.getType())
				&& item.getAmount() > 0;
	}

	/**
	 * Checks if an ItemStack has a valid amount.
	 *
	 * @param item The item to validate.
	 * @return Returns true if the item has a valid amount.
	 */
	public static boolean isValidItemAmount(@Nullable final ItemStack item) {
		return item != null
				&& item.getAmount() > 0
				&& item.getAmount() <= item.getMaxStackSize();
	}

	/**
	 * Checks whether a material would be considered a valid item.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material would be considered a valid item.
	 */
	public static boolean isValidItemMaterial(@Nullable final Material material) {
		return material != null
				/** Add any null-returns in {@link CraftItemFactory#getItemMeta(Material, org.bukkit.craftbukkit.v1_17_R1.inventory.CraftMetaItem)} */
				&& material != Material.AIR
				&& material.isItem();
	}

	/**
	 * Determines whether two item stacks are functionally identical.
	 *
	 * @param former The first item.
	 * @param latter The second item.
	 * @return Returns true if both items are functionally identical.
	 *
	 * @see ItemStack#isSimilar(ItemStack)
	 */
	public static boolean areItemsEqual(@Nullable final ItemStack former,
										@Nullable final ItemStack latter) {
		if (former == latter) {
			return true;
		}
		return (former != null && latter != null)
				&& former.getAmount() == latter.getAmount()
				&& areItemsSimilar(former, latter);
	}

	/**
	 * Determines whether two item stacks are similar.
	 *
	 * @param former The first item.
	 * @param latter The second item.
	 * @return Returns true if both items are similar.
	 *
	 * @see ItemStack#isSimilar(ItemStack)
	 */
	public static boolean areItemsSimilar(@Nullable final ItemStack former,
										  @Nullable final ItemStack latter) {
		if (former == latter) {
			return true;
		}
		if ((former == null || latter == null)
				|| former.getType() != latter.getType()
				|| former.hasItemMeta() != latter.hasItemMeta()) {
			return false;
		}
		return MetaUtils.areMetasEqual(former.getItemMeta(), latter.getItemMeta());
	}

	/**
	 * Returns the NMS version of a given item, preferring the item's craft handle but will fall back upon creating an
	 * NMS copy.
	 *
	 * @param item The item to get the NMS version of.
	 * @return The NMS version, either handle or copy.
	 */
	@Contract("!null -> !null")
	@Nullable
	public static net.minecraft.world.item.ItemStack getNMSItemStack(@Nullable final ItemStack item) {
		if (item == null) {
			return null;
		}
		if (item instanceof CraftItemStack craftItem) {
			if (craftItem.handle != null) {
				return craftItem.handle;
			}
		}
		return CraftItemStack.asNMSCopy(item);
	}

	/**
	 * Decrements an item's amount, or returns null if the amount reaches zero.
	 *
	 * @param item The item to decrement in amount.
	 * @return Returns the given item with a decremented amount, or null.
	 */
	@Nullable
	public static ItemStack decrementItem(final ItemStack item) {
		return decrementItem(item, 1);
	}

	/**
	 * Decrements an item's amount, or returns null if the amount reaches zero.
	 *
	 * @param item The item to decrement in amount. Will return the item unchanged if the amount is zero of less.
	 * @return Returns the given item with a decremented amount, or null.
	 */
	@Nullable
	public static ItemStack decrementItem(final ItemStack item,
										  final int amount) {
		return item == null ? null : amount < 1 ? item : item.subtract(amount).getAmount() < 1 ? null : item;
	}

	/**
	 * Normalizes an item.
	 *
	 * @param item The item to normalize.
	 * @return The normalized item.
	 */
	@Contract("!null -> !null")
	@Nullable
	public static ItemStack normalizeItem(@Nullable final ItemStack item) {
		return item == null ? null : item.clone().asOne();
	}

	/**
	 * Retrieves the ItemMeta from an item.
	 *
	 * @param item The item to retrieve meta from.
	 * @return Returns the item meta.
	 */
	@Nullable
	public static ItemMeta getItemMeta(@Nullable final ItemStack item) {
		return item == null ? null : item.getItemMeta();
	}

	/**
	 * Determines whether an item has a display name.
	 *
	 * @param item The item to check the display name of.
	 * @return Returns true if the item has a display name.
	 */
	public static boolean hasDisplayName(@Nullable final ItemStack item) {
		final var meta = getItemMeta(item);
		return meta != null && meta.hasDisplayName();
	}

	/**
	 * Retrieves the display name from an item.
	 *
	 * @param item The item to retrieve the display name from.
	 * @return Returns the display name of an item.
	 */
	@Nullable
	public static Component getComponentDisplayName(@Nullable final ItemStack item) {
		final var meta = getItemMeta(item);
		return meta == null ? null : meta.displayName();
	}

	/**
	 * Sets a display name to an item. A null or empty name will remove the display name from the item.
	 *
	 * @param item The item to set the display name to.
	 * @param name The display name to set on the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void setComponentDisplayName(@Nonnull final ItemStack item,
											   @Nullable final Component name) {
		final var meta = Objects.requireNonNull(getItemMeta(item),
				"Cannot set that display name: item has no meta.");
		meta.displayName(name);
		item.setItemMeta(meta);
	}

	/**
	 * Retrieves the lore from an item.
	 *
	 * @param item The item to retrieve the lore from.
	 * @return Returns the lore, which is never null.
	 */
	@Nonnull
	public static List<Component> getComponentLore(@Nullable final ItemStack item) {
		final var meta = getItemMeta(item);
		return meta == null ? new ArrayList<>(0) : MetaUtils.getComponentLore(meta);
	}

	/**
	 * Sets the lore for an item, replacing any lore that may have already been set.
	 *
	 * @param item The item to set the lore to.
	 * @param lines The lore to set to the item.
	 *
	 * @see ItemUtils#clearLore(ItemStack)
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void setComponentLore(@Nonnull final ItemStack item,
										@Nullable final Component... lines) {
		setComponentLore(item, lines == null ? null : Arrays.asList(lines));
	}

	/**
	 * Sets the lore for an item, replacing any lore that may have already been set.
	 *
	 * @param item The item to set the lore to.
	 * @param lines The lore to set to the item.
	 *
	 * @see ItemUtils#clearLore(ItemStack)
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void setComponentLore(@Nonnull final ItemStack item,
										@Nullable final List<Component> lines) {
		final var meta = Objects.requireNonNull(getItemMeta(item),
				"Cannot set that lore: item has no meta.");
		MetaUtils.setComponentLore(meta, lines);
		item.setItemMeta(meta);
	}

	/**
	 * Clears the lore from an item.
	 *
	 * @param item The item to clear lore of.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void clearLore(@Nonnull final ItemStack item) {
		setComponentLore(item, (List<Component>) null);
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param item The item to append the lore to.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void addComponentLore(@Nonnull final ItemStack item,
										@Nullable final Component... lines) {
		addComponentLore(item, false, lines);
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param item The item to append the lore to.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void addComponentLore(@Nonnull final ItemStack item,
										@Nullable final List<Component> lines) {
		addComponentLore(item, false, lines);
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void addComponentLore(@Nonnull final ItemStack item,
										final boolean prepend,
										@Nullable final Component... lines) {
		addComponentLore(item, prepend, lines == null ? null : Arrays.asList(lines));
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void addComponentLore(@Nonnull final ItemStack item,
										final boolean prepend,
										@Nullable final List<Component> lines) {
		final var meta = Objects.requireNonNull(getItemMeta(item),
				"Cannot add that lore: item has no meta.");
		MetaUtils.addComponentLore(meta, prepend, lines);
		item.setItemMeta(meta);
	}

	/**
	 * Retrieves the Damageable ItemMeta only if it's relevant to the item. This is necessary because [almost?] every
	 * ItemMeta implements Damageable... for some reason. And so this will only return a Damageable instance if the
	 * item material actually has a maximum durability above zero.
	 *
	 * @param item The item to get the Damageable meta from.
	 * @return Returns an instance of Damageable, or null.
	 */
	@Nullable
	public static Damageable getDamageable(@Nullable final ItemStack item) {
		if (item == null) {
			return null;
		}
		final Material material = item.getType();
		if (isValidItemMaterial(material)
				&& material.getMaxDurability() > 0
				&& getItemMeta(item) instanceof Damageable damageable) {
			return damageable;
		}
		return null;
	}

	/**
	 * Makes an item glow by adding an enchantment and the flag for hiding enchantments, so it has the enchantment glow
	 * without an enchantment being visible. Note that this does actually apply an enchantment to an item.
	 *
	 * @param item Item to apply glow to.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void addGlow(@Nullable final ItemStack item) {
		handleItemMeta(item, (ItemMeta meta) -> {
			MetaUtils.addGlow(meta);
			return true;
		});
	}

	/**
	 * Handles an item's metadata.
	 *
	 * @param <T> The item meta type, which might not extend ItemMeta (Damageable for example)
	 * @param item The item to handle the metadata of.
	 * @param handler The item metadata handler, which should return true if modifications were made.
	 * @return Returns true if the metadata was successfully handled.
	 *
	 * @see ItemStack#getItemMeta()
	 */
	@Contract("null, _ -> false; _, null -> false")
	@SuppressWarnings("unchecked")
	public static <T> boolean handleItemMeta(@Nullable final ItemStack item,
											 @Nullable final Predicate<T> handler) {
		if (item == null || handler == null) {
			return false;
		}
		try {
			final T meta = (T) item.getItemMeta();
			if (meta == null) {
				return false;
			}
			if (handler.test(meta)) {
				return item.setItemMeta((ItemMeta) meta);
			}
		}
		catch (ClassCastException ignored) { }
		return false;
	}

	// ------------------------------------------------------------
	// Deprecated Functions
	// ------------------------------------------------------------

	/**
	 * Retrieves the display name from an item.
	 *
	 * @param item The item to retrieve the display name from.
	 * @return Returns the display name of an item.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use
	 *             {@link #getComponentDisplayName(ItemStack)} instead.
	 */
	@Nullable
	@Deprecated
	public static String getDisplayName(@Nullable final ItemStack item) {
		final var meta = getItemMeta(item);
		return meta == null ? null : meta.getDisplayName();
	}

	/**
	 * Sets a display name to an item.
	 *
	 * @param item The item to set the display name to.
	 * @param name The display name to set on the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use
	 *             {@link #setComponentDisplayName(ItemStack, Component)} instead.
	 */
	@Deprecated
	public static void setDisplayName(@Nonnull final ItemStack item,
									  @Nullable final String name) {
		final var meta = Objects.requireNonNull(getItemMeta(item),
				"Cannot set that display name: item has no meta.");
		meta.setDisplayName(name);
		item.setItemMeta(meta);
	}

	/**
	 * Retrieves the lore from an item.
	 *
	 * @param item The item to retrieve the lore from.
	 * @return Returns the lore, which is never null.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use
	 *             {@link #getComponentLore(ItemStack)} instead.
	 */
	@Nonnull
	@Deprecated
	public static List<String> getLore(@Nullable final ItemStack item) {
		final var meta = getItemMeta(item);
		return meta == null ? new ArrayList<>(0) : MetaUtils.getLore(meta);
	}

	/**
	 * Sets the lore for an item, replacing any lore that may have already been set.
	 *
	 * @param item The item to set the lore to.
	 * @param lines The lore to set to the item.
	 *
	 * @see ItemUtils#clearLore(ItemStack)
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use
	 *             {@link #setComponentLore(ItemStack, Component...)} instead.
	 */
	@Deprecated
	public static void setLore(@Nonnull final ItemStack item,
							   @Nullable final String... lines) {
		setLore(item, lines == null ? null : Arrays.asList(lines));
	}

	/**
	 * Sets the lore for an item, replacing any lore that may have already been set.
	 *
	 * @param item The item to set the lore to.
	 * @param lines The lore to set to the item.
	 *
	 * @see ItemUtils#clearLore(ItemStack)
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use
	 *             {@link #setComponentLore(ItemStack, List)} instead.
	 */
	@Deprecated
	public static void setLore(@Nonnull final ItemStack item,
							   @Nullable final List<String> lines) {
		final var meta = Objects.requireNonNull(getItemMeta(item),
				"Cannot set that lore: item has no meta.");
		meta.setLore(lines);
		item.setItemMeta(meta);
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param item The item to append the lore to.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use
	 *             {@link #addComponentLore(ItemStack, Component...)} instead.
	 */
	@Deprecated
	public static void addLore(@Nonnull final ItemStack item,
							   @Nullable final String... lines) {
		addLore(item, false, lines);
	}

	/**
	 * Appends lore to an item.
	 *
	 * @param item The item to append the lore to.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use
	 *             {@link #addComponentLore(ItemStack, List)} instead.
	 */
	@Deprecated
	public static void addLore(@Nonnull final ItemStack item,
							   @Nullable final List<String> lines) {
		addLore(item, false, lines);
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use
	 *             {@link #addComponentLore(ItemStack, boolean, Component...)} instead.
	 */
	@Deprecated
	public static void addLore(@Nonnull final ItemStack item,
							   final boolean prepend,
							   @Nullable final String... lines) {
		addLore(item, prepend, lines == null ? null : Arrays.asList(lines));
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param item The item to append the lore to.
	 * @param prepend If set to true, the lore will be prepended instead of appended.
	 * @param lines The lore to append to the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use
	 *             {@link #addComponentLore(ItemStack, boolean, List)} instead.
	 */
	@Deprecated
	public static void addLore(@Nonnull final ItemStack item,
							   final boolean prepend,
							   @Nullable final List<String> lines) {
		final var meta = Objects.requireNonNull(getItemMeta(item),
				"Cannot add that lore: item has no meta.");
		MetaUtils.addLore(meta, prepend, lines);
		item.setItemMeta(meta);
	}

}
