package vg.civcraft.mc.civmodcore.inventory.items;

import com.google.common.base.Strings;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.kyori.adventure.text.Component;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

/**
 * Class of static APIs for Items. Replaces ISUtils.
 */
public final class ItemUtils {

	private static final EnumMap<Material, String> MATERIAL_NAMES = new EnumMap<>(Material.class);

	/**
	 * Loads item names from configurable files and requests any custom item names programmatically from plugins.
	 *
	 * @param plugin The CivModCore instance plugin.
	 */
	public static void loadItemNames(final CivModCorePlugin plugin) {
		final var logger = CivLogger.getLogger(ItemUtils.class);
		MATERIAL_NAMES.clear();
		final File materialsFile = plugin.getDataFile("materials.yml");
		final YamlConfiguration materialsConfig = YamlConfiguration.loadConfiguration(materialsFile);
		for (final String key : materialsConfig.getKeys(false)) {
			if (Strings.isNullOrEmpty(key)) {
				logger.warning("Material key was empty.");
				continue;
			}
			final Material material = Material.getMaterial(key);
			if (material == null) {
				logger.warning("Could not find material: " + key);
				return;
			}
			final String name = materialsConfig.getString(key);
			if (Strings.isNullOrEmpty(name)) {
				logger.warning("Name for [" + key + "] was empty.");
				continue;
			}
			MATERIAL_NAMES.put(material, ChatUtils.parseColor(name));
		}
		logger.info("Loaded a total of " + MATERIAL_NAMES.size() + " item names from materials.yml");
		// Determine if there's any materials missing
		final Set<Material> missing = new HashSet<>();
		CollectionUtils.addAll(missing, Material.values());
		missing.removeIf(MATERIAL_NAMES::containsKey);
		if (!missing.isEmpty()) {
			logger.warning("The following materials are missing from materials.yml: " +
					missing.stream().map(Enum::name).collect(Collectors.joining(",")) + ".");
		}
	}

	/**
	 * Gets the name of an item based off a material, e.g: POLISHED_GRANITE to Polished Granite
	 *
	 * @param material The material to get the name of.
	 * @return Returns the material name.
	 */
	public static String getItemName(final Material material) {
		if (material == null) {
			throw new IllegalArgumentException("Cannot retrieve name of invalid material.");
		}
		return MATERIAL_NAMES.computeIfAbsent(material, (ignored) -> material.name());
	}

	/**
	 * Gets the name of an item either based off its material or its custom item tag.
	 *
	 * @param item The item to get the name of.
	 * @return Returns the item's name.
	 */
	public static String getItemName(final ItemStack item) {
		if (item == null) {
			return null;
		}
		return getItemName(item.getType());
	}

	/**
	 * Checks if an ItemStack is valid. An ItemStack is considered valid if when added to an inventory, it shows as an
	 * item with an amount within appropriate bounds. Therefore {@code new ItemStack(Material.AIR)} will not be
	 * considered valid, nor will {@code new ItemStack(Material.STONE, 80)}
	 *
	 * @param item The item to validate.
	 * @return Returns true if the item is valid.
	 */
	public static boolean isValidItem(final ItemStack item) {
		return item != null
				&& isValidItemMaterial(item.getType())
				&& isValidItemAmount(item);
	}

	/**
	 * Checks if an ItemStack has a valid amount.
	 *
	 * @param item The item to validate.
	 * @return Returns true if the item has a valid amount.
	 */
	public static boolean isValidItemAmount(ItemStack item) {
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
	public static boolean isValidItemMaterial(final Material material) {
		return material != null
				&& !material.isAir()
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
	public static boolean areItemsEqual(final ItemStack former, final ItemStack latter) {
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
	public static boolean areItemsSimilar(final ItemStack former, final ItemStack latter) {
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
	public static net.minecraft.server.v1_16_R3.ItemStack getNMSItemStack(final ItemStack item) {
		if (item == null) {
			return null;
		}
		if (item instanceof CraftItemStack) {
			final var handle = ((CraftItemStack) item).getHandle();
			if (handle != null) {
				return handle;
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
	public static ItemStack decrementItem(final ItemStack item) {
		return item == null ? null : item.subtract().getAmount() == 0 ? null : item;
	}

	/**
	 * Normalizes an item.
	 *
	 * @param item The item to normalize.
	 * @return The normalized item.
	 */
	public static ItemStack normalizeItem(ItemStack item) {
		return item == null ? null : item.clone().asOne();
	}

	/**
	 * Retrieves the ItemMeta from an item.
	 *
	 * @param item The item to retrieve meta from.
	 * @return Returns the item meta.
	 */
	public static ItemMeta getItemMeta(final ItemStack item) {
		return item == null ? null : item.getItemMeta();
	}

	/**
	 * Determines whether an item has a display name.
	 *
	 * @param item The item to check the display name of.
	 * @return Returns true if the item has a display name.
	 */
	public static boolean hasDisplayName(final ItemStack item) {
		final var meta = getItemMeta(item);
		return meta != null && meta.hasDisplayName();
	}

	/**
	 * Retrieves the display name from an item.
	 *
	 * @param item The item to retrieve the display name from.
	 * @return Returns the display name of an item.
	 */
	public static Component getComponentDisplayName(final ItemStack item) {
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
	public static void setComponentDisplayName(final ItemStack item, final Component name) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			throw new IllegalArgumentException("Cannot set that display name: item has no meta.");
		}
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
	public static List<Component> getComponentLore(final ItemStack item) {
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
	public static void setComponentLore(final ItemStack item, final Component... lines) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			throw new IllegalArgumentException("Cannot set that lore: item has no meta.");
		}
		MetaUtils.setComponentLore(meta, lines);
		item.setItemMeta(meta);
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
	public static void setComponentLore(final ItemStack item, final List<Component> lines) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			throw new IllegalArgumentException("Cannot set that lore: item has no meta.");
		}
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
	public static void clearLore(final ItemStack item) {
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
	public static void addComponentLore(final ItemStack item, final Component... lines) {
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
	public static void addComponentLore(final ItemStack item, final List<Component> lines) {
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
	public static void addComponentLore(final ItemStack item,
										final boolean prepend,
										final Component... lines) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			throw new IllegalArgumentException("Cannot add that lore: item has no meta.");
		}
		MetaUtils.addComponentLore(meta, prepend, lines);
		item.setItemMeta(meta);
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
	public static void addComponentLore(final ItemStack item,
										final boolean prepend,
										final List<Component> lines) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			throw new IllegalArgumentException("Cannot add that lore: item has no meta.");
		}
		MetaUtils.addComponentLore(meta, prepend, lines);
		item.setItemMeta(meta);
	}

	/**
	 * Retrieves the Damageable ItemMeta only if it's relevant to the item. This is necessary because [almost?] every
	 * ItemMeta implements Damageable.. for some reason. And so this will only return a Damageable instance if the item
	 * material actually has a maximum durability above zero.
	 *
	 * @param item The item to get the Damageable meta from.
	 * @return Returns an instance of Damageable, or null.
	 */
	public static Damageable getDamageable(final ItemStack item) {
		if (item == null) {
			return null;
		}
		final Material material = item.getType();
		if (!isValidItemMaterial(material)
				|| material.getMaxDurability() <= 0) {
			return null;
		}
		final var meta = getItemMeta(item);
		if (!(meta instanceof Damageable)) {
			return null;
		}
		return (Damageable) meta;
	}

	/**
	 * Makes an item glow by adding an enchantment and the flag for hiding enchantments,
	 * so it has the enchantment glow without an enchantment being visible. Note that this
	 * does actually apply an enchantment to an item.
	 *
	 * @param item Item to apply glow to.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 */
	public static void addGlow(final ItemStack item) {
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
	@SuppressWarnings("unchecked")
	public static <T> boolean handleItemMeta(final ItemStack item, final Predicate<T> handler) {
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
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 *             Use {@link #getComponentDisplayName(ItemStack)} instead.
	 */
	@Deprecated
	public static String getDisplayName(final ItemStack item) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			return null;
		}
		return meta.getDisplayName();
	}

	/**
	 * Sets a display name to an item.
	 *
	 * @param item The item to set the display name to.
	 * @param name The display name to set on the item.
	 *
	 * @throws IllegalArgumentException Throws when the given item has no meta.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 *             Use {@link #setComponentDisplayName(ItemStack, Component)} instead.
	 */
	@Deprecated
	public static void setDisplayName(final ItemStack item, final String name) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			throw new IllegalArgumentException("Cannot set that display name: item has no meta.");
		}
		meta.setDisplayName(name);
		item.setItemMeta(meta);
	}

	/**
	 * Retrieves the lore from an item.
	 *
	 * @param item The item to retrieve the lore from.
	 * @return Returns the lore, which is never null.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 *             Use {@link #getComponentLore(ItemStack)} instead.
	 */
	@Deprecated
	public static List<String> getLore(final ItemStack item) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			return new ArrayList<>(0);
		}
		return MetaUtils.getLore(meta);
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
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 *             Use {@link #setComponentLore(ItemStack, Component...)} instead.
	 */
	@Deprecated
	public static void setLore(final ItemStack item, final String... lines) {
		final List<String> lore = new ArrayList<>();
		CollectionUtils.addAll(lore, lines);
		setLore(item, lore);
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
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 *             Use {@link #setComponentLore(ItemStack, List)} instead.
	 */
	@Deprecated
	public static void setLore(final ItemStack item, final List<String> lines) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			throw new IllegalArgumentException("Cannot set that lore: item has no meta.");
		}
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
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 *             Use {@link #addComponentLore(ItemStack, Component...)} instead.
	 */
	@Deprecated
	public static void addLore(final ItemStack item, final String... lines) {
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
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 *             Use {@link #addComponentLore(ItemStack, List)} instead.
	 */
	@Deprecated
	public static void addLore(final ItemStack item, final List<String> lines) {
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
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 *             Use {@link #addComponentLore(ItemStack, boolean, Component...)} instead.
	 */
	@Deprecated
	public static void addLore(final ItemStack item, final boolean prepend, final String... lines) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			throw new IllegalArgumentException("Cannot add that lore: item has no meta.");
		}
		MetaUtils.addLore(meta, prepend, lines);
		item.setItemMeta(meta);
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
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 *             Use {@link #addComponentLore(ItemStack, boolean, List)} instead.
	 */
	@Deprecated
	public static void addLore(final ItemStack item, final boolean prepend, final List<String> lines) {
		final var meta = getItemMeta(item);
		if (meta == null) {
			throw new IllegalArgumentException("Cannot add that lore: item has no meta.");
		}
		MetaUtils.addLore(meta, prepend, lines);
		item.setItemMeta(meta);
	}

}
