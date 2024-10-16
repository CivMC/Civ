package vg.civcraft.mc.civmodcore.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.pseudo.PseudoServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class ItemMetaTests {

	private static final ItemStack TEMPLATE_ITEM = new ItemStack(Material.STICK);

	@BeforeAll
	public static void setupBukkit() {
		PseudoServer.setup();
	}

	// TODO: Who knows.
//	/**
//	 * Tests whether a basic string display name can match with a component.
//	 */
//	@Test
//	public void testBasicDisplayNameEquality() {
//		// Setup
//		final var formerItem = NullUtils.isNotNull(NBTSerialization.processItem(TEMPLATE_ITEM, (nbt) -> {
//			final var display = new NBTCompound();
//			display.setString("Name", "Hello!");
//			nbt.setCompound("display-name", display);
//		}));
//		final var latterItem = TEMPLATE_ITEM.clone();
//		ItemUtils.setComponentDisplayName(latterItem, Component.text("Hello!"));
//		// Check
//		System.out.println(formerItem);
//		System.out.println(latterItem);
//		Assertions.assertTrue(ChatUtils.areComponentsEqual(
//				ItemUtils.getComponentDisplayName(formerItem),
//				ItemUtils.getComponentDisplayName(latterItem)));
//	}

	// TODO: Who knows.
//	/**
//	 * Tests whether a json primitive display name can match with a component.
//	 */
//	@Test
//	public void testBasicJsonPrimitiveDisplayNameEquality() {
//		// Setup
//		final var formerItem = NullUtils.isNotNull(NBTSerialization.processItem(TEMPLATE_ITEM, (nbt) -> {
//			final var display = new NBTCompound();
//			display.setString("Name", "\"Hello!\"");
//			nbt.setCompound("display", display);
//		}));
//		final var latterItem = TEMPLATE_ITEM.clone();
//		ItemUtils.handleItemMeta(latterItem, (ItemMeta meta) -> {
//			meta.displayName(Component.text("Hello!"));
//			return true;
//		});
//		// Check
//		System.out.println(formerItem);
//		System.out.println(latterItem);
//		Assertions.assertTrue(ChatUtils.areComponentsEqual(
//				ItemUtils.getComponentDisplayName(formerItem),
//				ItemUtils.getComponentDisplayName(latterItem)));
//	}

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
		ItemUtils.handleItemMeta(latterItem, (ItemMeta meta) -> {
			meta.displayName(Component.text("Hello!"));
			return true;
		});
		// Check
		Assertions.assertTrue(ChatUtils.areComponentsEqual(
				ItemUtils.getComponentDisplayName(formerItem),
				ItemUtils.getComponentDisplayName(latterItem)));
		Assertions.assertTrue(ItemUtils.areItemsSimilar(formerItem, latterItem));
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
		Assertions.assertTrue(ChatUtils.isBaseComponent(
				ItemUtils.getComponentDisplayName(formerItem)));
		Assertions.assertFalse(ChatUtils.isBaseComponent(
				ItemUtils.getComponentDisplayName(latterItem)));
	}

}
