package vg.civcraft.mc.civmodcore.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.pseudo.PseudoServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.NullUtils;

public class ItemMetaTests {

	private static final ItemStack TEMPLATE_ITEM = new ItemStack(Material.STICK);

	@BeforeClass
	public static void setupBukkit() {
		PseudoServer.setup();
	}

	/**
	 * Tests whether a primitive display name can match with a component.
	 */
	@Test
	public void testBasicDisplayNameEquality() {
		// Setup
		final var formerItem = NullUtils.isNotNull(NBTCompound.processItem(TEMPLATE_ITEM, (nbt) -> {
			final var display = new NBTCompound();
			display.setString("Name", "\"Hello!\"");
			nbt.setCompound("display", display);
		}));
		final var latterItem = TEMPLATE_ITEM.clone();
		ItemUtils.setComponentDisplayName(latterItem, Component.text("Hello!"));
		// Check
		Assert.assertTrue(ChatUtils.areComponentsEqual(
				ItemUtils.getComponentDisplayName(formerItem),
				ItemUtils.getComponentDisplayName(latterItem)));
	}

	/**
	 * How do different API methods of setting the display name fare?
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void testAdvancedDisplayNameEquality() {
		// Setup
		final var formerItem = TEMPLATE_ITEM.clone();
		ItemUtils.handleItemMeta(formerItem, (ItemMeta meta) -> {
			meta.setDisplayName("Hello!");
			return true;
		});
		final var latterItem = TEMPLATE_ITEM.clone();
		ItemUtils.setComponentDisplayName(latterItem, Component.text("Hello!"));
		// Check
		Assert.assertTrue(ChatUtils.areComponentsEqual(
				ItemUtils.getComponentDisplayName(formerItem),
				ItemUtils.getComponentDisplayName(latterItem)));
		Assert.assertTrue(ItemUtils.areItemsSimilar(formerItem, latterItem));
	}

	/**
	 * Tests whether {@link ChatUtils#isBaseComponent(Component)} works.
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void testBaseComponent() {
		// Setup
		final var formerItem = TEMPLATE_ITEM.clone();
		ItemUtils.handleItemMeta(formerItem, (ItemMeta meta) -> {
			meta.setDisplayName("Hello!");
			return true;
		});
		final var latterItem = TEMPLATE_ITEM.clone();
		ItemUtils.handleItemMeta(latterItem, (ItemMeta meta) -> {
			meta.displayName(Component.text("Hello!"));
			return true;
		});
		// Check
		Assert.assertTrue(ChatUtils.isBaseComponent(
				ItemUtils.getComponentDisplayName(formerItem)));
		Assert.assertFalse(ChatUtils.isBaseComponent(
				ItemUtils.getComponentDisplayName(latterItem)));
	}

}
