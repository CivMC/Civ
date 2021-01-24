package vg.civcraft.mc.civmodcore.items;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import vg.civcraft.mc.civmodcore.bukkit.PseudoServer;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.NullUtils;

public class ItemMetaTester {

	private static final ItemStack TEMPLATE_ITEM = new ItemStack(Material.STICK);

	@BeforeClass
	public static void setupBukkit() {
		PseudoServer.setup();
	}

	/**
	 * <p>Sometimes components will be stored out in different orders.</p>
	 *
	 * <p>
	 *     For example:
	 *     <ul>
	 *         <li>{@code {"text":"","extra":[{"text":"Test!"}]}}</li>
	 *         <li>{@code {"extra":[{"text":"Test!"}],"text":""}}</li>
	 *     </ul>
	 * </p>
	 */
	@Test
	public void testComponentOrderEquality() {
		// Setup
		final String formerMessage = "{\"text\":\"\",\"extra\":[{\"text\":\"Test!\"}]}";
		final String latterMessage = "{\"extra\":[{\"text\":\"Test!\"}],\"text\":\"\"}";
		// Process
		final BaseComponent[] formerParsed = ComponentSerializer.parse(formerMessage);
		final BaseComponent[] latterParsed = ComponentSerializer.parse(latterMessage);
		// Check
		Assert.assertArrayEquals(formerParsed, latterParsed);
	}

	/**
	 *
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
		ItemUtils.setDisplayName(latterItem, "Hello!");
		// Check
		Assert.assertTrue(ChatUtils.areComponentsEqual(
				ItemUtils.getComponentDisplayName(formerItem),
				ItemUtils.getComponentDisplayName(latterItem)));
	}

	/**
	 *
	 */
	@Test
	public void testAdvancedDisplayNameEquality() {
		// Setup
		final var formerItem = TEMPLATE_ITEM.clone();
		ItemUtils.setDisplayName(formerItem, "Hello!");
		final var latterItem = TEMPLATE_ITEM.clone();
		ItemUtils.setComponentDisplayName(latterItem, new TextComponent("Hello!"));
		// Check
		Assert.assertTrue(ChatUtils.areComponentsEqual(
				ItemUtils.getComponentDisplayName(formerItem),
				ItemUtils.getComponentDisplayName(latterItem)));
	}

	/**
	 *
	 */
	@Test
	public void testCustomItemStackSimilarity() {
		// Setup
		final var formerItem = TEMPLATE_ITEM.clone();
		ItemUtils.setDisplayName(formerItem, "Hello!");
		final var latterItem = TEMPLATE_ITEM.clone();
		ItemUtils.setComponentDisplayName(latterItem, new TextComponent("Hello!"));
		// Check
		Assert.assertTrue(ItemUtils.areItemsSimilar(formerItem, latterItem));
	}

}
