package vg.civcraft.mc.civmodcore.nbt;

import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.pseudo.PseudoServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;

public class NBTTests {

	@BeforeClass
	public static void setupBukkit() {
		PseudoServer.setup();
	}

	@Test
	public void testStringSerialization() {
		// Setup
		String STRING_KEY = "test_string";
		String expectedString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
				"incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation " +
				"ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
				"voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non " +
				"proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		// Process
		final var nbt = new NBTCompound();
		nbt.setString(STRING_KEY, expectedString);
		// Check
		Assert.assertEquals(expectedString, nbt.getString(STRING_KEY));
	}

	@Test
	public void testStringArraySerialization() {
		// Setup
		String STRING_ARRAY_KEY = "test_string_array";
		String[] expectedStringArray = { "one", "two", "three" };
		// Process
		final var nbt = new NBTCompound();
		nbt.setStringArray(STRING_ARRAY_KEY, expectedStringArray);
		// Check
		Assert.assertArrayEquals(expectedStringArray, nbt.getStringArray(STRING_ARRAY_KEY));
	}

	@Test
	public void testByteSerialization() {
		// Setup
		String STRING_KEY = "test_byte";
		String expectedString = "Ultricies leo integer malesuada nunc vel risus commodo viverra. Fames ac turpis " +
				"egestas sed tempus urna. Sollicitudin nibh sit amet commodo. Cras sed felis eget velit aliquet " +
				"sagittis. Convallis tellus id interdum velit laoreet id donec ultrices. Mauris nunc congue nisi " +
				"vitae suscipit tellus mauris a diam. Leo vel fringilla est ullamcorper. Justo nec ultrices dui " +
				"sapien eget mi. Nisl vel pretium lectus quam id leo in. Nisi vitae suscipit tellus mauris a diam. " +
				"Proin fermentum leo vel orci porta non pulvinar. Facilisis magna etiam tempor orci eu lobortis " +
				"elementum nibh tellus. Aliquet eget sit amet tellus cras adipiscing enim.";
		// Process
		final var nbt = new NBTCompound();
		nbt.setString(STRING_KEY, expectedString);
		final byte[] data = NBTSerialization.toBytes(nbt);
		final NBTTagCompound actual = NBTSerialization.fromBytes(data);
		// Check
		Assert.assertNotNull(actual);
		Assert.assertEquals(expectedString, actual.l(STRING_KEY));
	}

	@Test
	public void testNullSerialization() {
		// Setup
		String STRING_KEY = "test_null_string";
		// Process
		final var nbt = new NBTCompound();
		nbt.setString(STRING_KEY, null);
		final byte[] data = NBTSerialization.toBytes(nbt);
		final var actual = new NBTCompound(NBTSerialization.fromBytes(data));
		// Check
		Assert.assertNull(actual.getNullableString(STRING_KEY));
	}

	@Test
	public void testNBTClearing() {
		// Setup
		String STRING_KEY = "test_clear";
		String expectedString = "In hac habitasse platea dictumst quisque sagittis purus. Consectetur purus ut " +
				"faucibus pulvinar elementum integer enim neque. Scelerisque eleifend donec pretium vulputate " +
				"sapien nec. In cursus turpis massa tincidunt dui ut ornare lectus. Imperdiet massa tincidunt " +
				"nunc pulvinar sapien et ligula ullamcorper. Lorem sed risus ultricies tristique nulla aliquet enim " +
				"tortor at. Arcu odio ut sem nulla. Etiam non quam lacus suspendisse. Tincidunt tortor aliquam " +
				"nulla facilisi cras. Magna ac placerat vestibulum lectus mauris. Tortor at auctor urna nunc id. " +
				"Turpis egestas pretium aenean pharetra magna ac placerat vestibulum lectus. Faucibus in ornare " +
				"quam viverra orci sagittis. Lectus proin nibh nisl condimentum id venenatis a. Diam in arcu cursus " +
				"euismod. Cras semper auctor neque vitae tempus. Leo a diam sollicitudin tempor id eu. Non sodales " +
				"neque sodales ut etiam. Elementum integer enim neque volutpat ac tincidunt vitae semper quis.";
		// Process
		final var nbt = new NBTCompound();
		nbt.setString(STRING_KEY, expectedString);
		nbt.clear();
		// Check
		Assert.assertNull(nbt.getNullableString(STRING_KEY));
	}

	@Test
	public void testLocationSerialisation() {
		// Setup
		final var location = new Location(null,
				123.456d, 789.654d, 321.098d,
				1.12344f, 123.234f);
		// Process
		final var nbt = NBTHelper.locationToNBT(location);
		final var parsed = NBTHelper.locationFromNBT(nbt);
		// Check
		Assert.assertEquals(location, parsed);
	}

	@Test
	public void testItemStackSerialisation() {
		// Setup
		final var item = new ItemStack(Material.STONE);
		ItemUtils.handleItemMeta(item, (ItemMeta meta) -> {
			meta.displayName(Component.text("Hello World!"));
			MetaUtils.setComponentLore(meta, Component.text("Testing!",
					NamedTextColor.YELLOW, TextDecoration.UNDERLINED));
			return true;
		});
		// Process
		final var nbt = NBTHelper.itemStackToNBT(item);
		final var parsed = NBTHelper.itemStackFromNBT(nbt);
		// Check
		Assert.assertEquals(item, parsed);
	}

	@Test
	public void testMapDeserialisation() {
		// Setup
		final NBTTagCompound targetNBT = new NBTTagCompound() {{
			a("EntityTag", new NBTTagCompound() {{
				a("id", "minecraft:vex");
			}});
		}};
		final Map<String, Object> testData = new HashMap<>() {{
			put("EntityTag", new HashMap<String, Object>() {{
				put("id", "minecraft:vex");
			}});
		}};
		// Process
		final NBTTagCompound convertedNBT = NBTSerialization.fromMap(testData);
		// Check
		Assert.assertEquals(targetNBT, convertedNBT);
	}

}
