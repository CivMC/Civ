package vg.civcraft.mc.civmodcore.inventory.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.Translatable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreCollectionUtils;

/**
 * Class of static APIs for Items. Replaces ISUtils.
 */
public final class ItemUtils {
    /**
     * Gets the name of an item based off a material, e.g: POLISHED_GRANITE to Polished Granite
     *
     * @param material The material to get the name of.
     * @return Returns the material name.
     * @deprecated Use {@link Component#translatable(Translatable)} instead.
     */
    @Deprecated
    public static @NotNull String getItemName(
        final @NotNull Material material
    ) {
        return ChatUtils.stringify(Component.translatable(material));
    }

    /**
     * Gets the name of an item either based off its material or its custom item tag.
     *
     * @param item The item to get the name of.
     * @return Returns the item's name.
     * @deprecated Use {@link Component#translatable(Translatable)} instead.
     */
    @Deprecated
    public static @Nullable String getItemName(
        final ItemStack item
    ) {
        return item == null ? null : ChatUtils.stringify(Component.translatable(item));
    }

    /**
     * Checks whether the given item can be interpreted as an empty slot.
     *
     * @param item The item to check.
     * @return Returns true if the item can be interpreted as an empty slot.
     */
    @Contract("null -> true")
    public static boolean isEmptyItem(
        final ItemStack item
    ) {
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
    @Contract("null -> false")
    public static boolean isValidItem(
        final ItemStack item
    ) {
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
    @Contract("null -> false")
    public static boolean isValidItemIgnoringAmount(
        final ItemStack item
    ) {
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
    @Contract("null -> false")
    public static boolean isValidItemAmount(
        final ItemStack item
    ) {
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
    @Contract("null -> false")
    public static boolean isValidItemMaterial(
        final Material material
    ) {
        return material != null
            /** Add any null-returns in {@link org.bukkit.craftbukkit.inventory.CraftItemFactory#getItemMeta(Material, org.bukkit.craftbukkit.inventory.CraftMetaItem)} */
            && material != Material.AIR
            && material.isItem();
    }

    /**
     * Determines whether two item stacks are similar.
     *
     * @param lhs The first item.
     * @param rhs The second item.
     * @return Returns true if both items are similar.
     * @see ItemStack#isSimilar(ItemStack)
     */
    @SuppressWarnings("UnstableApiUsage")
    @Contract("null, null -> true; !null, null -> false; null, !null -> false")
    public static boolean areItemsSimilar(
        final ItemStack lhs,
        final ItemStack rhs
    ) {
        if (lhs == rhs) {
            return true;
        }
        if (isEmptyItem(lhs) ^ isEmptyItem(rhs)) {
            return false;
        }
        if (!lhs.matchesWithoutData(rhs, Set.of(DataComponentTypes.CUSTOM_NAME, DataComponentTypes.LORE), true)) {
            return false;
        }
        if (!ChatUtils.areComponentsEqual(
            lhs.getData(DataComponentTypes.CUSTOM_NAME),
            rhs.getData(DataComponentTypes.CUSTOM_NAME)
        )) {
            return false;
        }
        if (!MoreCollectionUtils.areListsEqual(
            lhs.getData(DataComponentTypes.LORE) instanceof final ItemLore lhsLore ? lhsLore.lines() : null,
            rhs.getData(DataComponentTypes.LORE) instanceof final ItemLore rhsLore ? rhsLore.lines() : null,
            ChatUtils::areComponentsEqual
        )) {
            return false;
        }
        return true;
    }

    /**
     * Unwraps a given Bukkit item to retrieve the internal NMS item.
     *
     * @return Returns the internal NMS item, or null. Will return null if the given item is "empty" (as determined by
     *         Bukkit).
     */
    public static @Nullable net.minecraft.world.item.ItemStack getNMSItemStack(
        final ItemStack item
    ) {
        if (item == null) {
            return null;
        }
        final net.minecraft.world.item.ItemStack nms = CraftItemStack.unwrap(item);
        if (nms == net.minecraft.world.item.ItemStack.EMPTY) {
            return null;
        }
        return nms;
    }

    /**
     * Decrements an item's amount, or returns null if the amount reaches zero.
     *
     * @param item The item to decrement in amount.
     * @return Returns the given item with a decremented amount, or null.
     */
    public static @Nullable ItemStack decrementItem(
        final ItemStack item
    ) {
        return decrementItem(item, 1);
    }

    /**
     * Decrements an item's amount, or returns null if the amount reaches zero.
     *
     * @param item The item to decrement in amount. Will return the item unchanged if the amount is zero of less.
     * @return Returns the given item with a decremented amount, or null.
     */
    public static @Nullable ItemStack decrementItem(
        final ItemStack item,
        final int amount
    ) {
        return item == null ? null : amount < 1 ? item : item.subtract(amount).getAmount() < 1 ? null : item;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static @Nullable Component getDisplayName(
        final ItemStack item
    ) {
        return item == null ? null : item.getData(DataComponentTypes.CUSTOM_NAME);
    }

    /**
     * Sets a display name to an item. Null will reset the display name from the item.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void setDisplayName(
        final @NotNull ItemStack item,
        final ComponentLike name
    ) {
        if (name == null) {
            item.resetData(DataComponentTypes.CUSTOM_NAME);
        }
        else {
            item.setData(DataComponentTypes.CUSTOM_NAME, name.asComponent());
        }
    }

    /**
     * Retrieves the lore from an item.
     *
     * @param item The item to retrieve the lore from.
     * @return Returns the lore, which is mutable and never null.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull List<@NotNull Component> getLore(
        final ItemStack item
    ) {
        if (item == null) {
            return new ArrayList<>(0);
        }
        return switch (item.getData(DataComponentTypes.LORE)) {
            case final ItemLore lore -> new ArrayList<>(lore.lines());
            case null -> new ArrayList<>(0);
        };
    }

    /**
     * Sets the lore for an item, replacing any lore that may have already been set.
     *
     * @param item  The item to set the lore to.
     * @param lines The lore to set to the item.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void setLore(
        final @NotNull ItemStack item,
        final @NotNull List<? extends @NotNull ComponentLike> lines
    ) {
        item.setData(DataComponentTypes.LORE, ItemLore.lore(List.copyOf(lines)));
    }

    /**
     * Appends lore to an item.
     *
     * @param item  The item to append the lore to.
     * @param lines The lore to append to the item.
     */
    public static void appendLore(
        final @NotNull ItemStack item,
        final @NotNull List<? extends @NotNull ComponentLike> lines
    ) {
        final List<Component> lore = getLore(item);
        lore.addAll(ComponentLike.asComponents(lines));
        setLore(item, lore);
    }

    /**
     * Prepends lore to an item.
     *
     * @param item  The item to prepend the lore to.
     * @param lines The lore to prepend to the item.
     */
    public static void prependLore(
        final @NotNull ItemStack item,
        final @NotNull List<? extends @NotNull ComponentLike> lines
    ) {
        final List<Component> lore = new ArrayList<>(ComponentLike.asComponents(lines));
        lore.addAll(getLore(item));
        setLore(item, lore);
    }

    /**
     * Retrieves the Damageable ItemMeta only if it's relevant to the item. This is necessary because [almost?] every
     * ItemMeta implements Damageable... for some reason. And so this will only return a Damageable instance if the
     * item material actually has a maximum durability above zero.
     *
     * @param item The item to get the Damageable meta from.
     * @return Returns an instance of Damageable, or null.
     */
    public static @Nullable Damageable getDamageable(
        final ItemStack item
    ) {
        if (item == null) {
            return null;
        }
        final Material material = item.getType();
        if (isValidItemMaterial(material)
            && material.getMaxDurability() > 0
            && item.getItemMeta() instanceof final Damageable damageable
        ) {
            return damageable;
        }
        return null;
    }

    /**
     * Retrieves the "minecraft:custom_data" component from an item.
     *
     * @param item Cannot be null or "empty" (as determined by Bukkit).
     *
     * @apiNote This should only be used for inspection, hence the lack of a setter method. If you want to set data,
     *          use {@link #editCustomData(org.bukkit.inventory.ItemStack, java.util.function.Consumer)} instead.
     */
    public static @Nullable CompoundTag getCustomData(
        final @NotNull ItemStack item
    ) {
        final net.minecraft.world.item.ItemStack nms = Objects.requireNonNull(getNMSItemStack(item));
        if (nms.get(DataComponents.CUSTOM_DATA) instanceof final CustomData data) {
            return data.copyTag();
        }
        return null;
    }

    /**
     * Edits the "minecraft:custom_data" component of an item. If the provided NBT is empty after being edited, the
     * component is removed from the item.
     *
     * @param item Cannot be null or "empty" (as determined by Bukkit).
     *
     * @apiNote It is good practice to use a "namespace" tag, instead of setting data directly onto the provided nbt.
     */
    public static void editCustomData(
        final @NotNull ItemStack item,
        final @NotNull Consumer<@NotNull CompoundTag> editor
    ) {
        CustomData.update(
            DataComponents.CUSTOM_DATA,
            Objects.requireNonNull(getNMSItemStack(item)),
            editor
        );
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
     * @deprecated Just store the meta in a variable, it's not that hard. Or you could pattern match or use
     *             {@link org.bukkit.inventory.ItemStack#editMeta(java.util.function.Consumer)} instead.
	 */
	@Contract("null, _ -> false; _, null -> false")
	@SuppressWarnings("unchecked")
	public static <T> boolean handleItemMeta(
        final ItemStack item,
        final Predicate<T> handler
    ) {
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
     * @deprecated Use {@link #getDisplayName(ItemStack)} instead.
     */
    @Deprecated
    public static @Nullable String getLegacyDisplayName(
        final ItemStack item
    ) {
        if (item.getItemMeta() instanceof final ItemMeta meta) {
            return meta.getDisplayName();
        }
        return null;
    }

    /**
     * @deprecated Use {@link #setDisplayName(org.bukkit.inventory.ItemStack, net.kyori.adventure.text.ComponentLike)}
     *             instead.
     */
    @Deprecated
    public static void setLegacyDisplayName(
        final @NotNull ItemStack item,
        final String name
    ) {
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    /**
     * @deprecated Use {@link #getLore(ItemStack)} instead.
     */
    @Deprecated
    public static @NotNull List<@NotNull String> getLegacyLore(
        final ItemStack item
    ) {
        if (item != null
            && item.getItemMeta() instanceof final ItemMeta meta
            && meta.getLore() instanceof final List<String> lore
        ) {
            return new ArrayList<>(lore);
        }
        return new ArrayList<>(0);
    }

    /**
     * @deprecated Use {@link #setLore(org.bukkit.inventory.ItemStack, java.util.List)} instead.
     */
    @Deprecated
    public static void setLegacyLore(
        final @NotNull ItemStack item,
        final @NotNull String @NotNull ... lines
    ) {
        setLegacyLore(item, Arrays.asList(lines));
    }

    /**
     * @deprecated Use {@link #setLore(org.bukkit.inventory.ItemStack, java.util.List)} instead.
     */
    @Deprecated
    public static void setLegacyLore(
        final @NotNull ItemStack item,
        final @NotNull List<@NotNull String> lines
    ) {
        final ItemMeta meta = item.getItemMeta();
        meta.setLore(List.copyOf(lines));
        item.setItemMeta(meta);
    }

    /**
     * @deprecated Use {@link #appendLore(org.bukkit.inventory.ItemStack, java.util.List)} instead.
     */
    @Deprecated
    public static void appendLegacyLore(
        final @NotNull ItemStack item,
        final @NotNull String @NotNull ... lines
    ) {
        appendLegacyLore(item, Arrays.asList(lines));
    }

    /**
     * @deprecated Use {@link #prependLore(org.bukkit.inventory.ItemStack, java.util.List)} instead.
     */
    @Deprecated
    public static void appendLegacyLore(
        final @NotNull ItemStack item,
        final @NotNull List<@NotNull String> lines
    ) {
        final List<String> lore = getLegacyLore(item);
        lore.addAll(List.copyOf(lines));
        setLegacyLore(item, lore);
    }
}
