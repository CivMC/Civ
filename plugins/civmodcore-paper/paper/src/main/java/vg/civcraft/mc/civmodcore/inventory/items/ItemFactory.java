package vg.civcraft.mc.civmodcore.inventory.items;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Utility class that provides a means to construct {@link CraftItemStack CraftItemStacks} in lieu of
 * {@link ItemStack#ItemStack(Material, int) new ItemStack(Material, Int)}. The reason being that
 * {@link CraftItemStack CraftItemStacks} interact more efficiently with NMS than {@link ItemStack} does as the latter
 * is entirely abstract in nature.
 */
@UtilityClass
public class ItemFactory {

	private static final Constructor<CraftItemStack> CRAFT_ITEM_STACK_CONSTRUCTOR;

	static {
		final Constructor<CraftItemStack> constructor;
		try {
			constructor = CraftItemStack.class.getDeclaredConstructor(
					Material.class, Integer.TYPE, Short.TYPE, ItemMeta.class);
			constructor.setAccessible(true);
		}
		catch (final NoSuchMethodException exception) {
			throw new IllegalStateException(exception);
		}
		CRAFT_ITEM_STACK_CONSTRUCTOR = constructor;
	}

	/**
	 * Creates a new {@link CraftItemStack} based on a given material.
	 *
	 * @param material The material of the item.
	 * @return Returns a new instance of {@link CraftItemStack}.
	 */
	public static ItemStack createItemStack(@Nonnull final Material material) {
		return createItemStack(material, 1);
	}

	/**
	 * Creates a new {@link CraftItemStack} based on a given material.
	 *
	 * @param material The material of the item.
	 * @param amount The item amount.
	 * @return Returns a new instance of {@link CraftItemStack}.
	 */
	public static ItemStack createItemStack(@Nonnull final Material material, final int amount) {
		if (material == null || !material.isItem()) { // Ignore highlighter
			throw new IllegalArgumentException("Material must be a valid item material!");
		}
		if (amount <= 0 || amount > material.getMaxStackSize()) {
			throw new IllegalArgumentException("Must have a valid item amount");
		}
		try {
			return CRAFT_ITEM_STACK_CONSTRUCTOR.newInstance(material, amount,
					(short) 0, Bukkit.getItemFactory().getItemMeta(material));
		}
		catch (final InvocationTargetException | InstantiationException | IllegalAccessException exception) {
			throw new RuntimeException("Could not construct item stack", exception);
		}
	}

}
