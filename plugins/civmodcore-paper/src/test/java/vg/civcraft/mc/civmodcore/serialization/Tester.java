package vg.civcraft.mc.civmodcore.serialization;

import org.junit.Assert;
import org.junit.Test;
import vg.civcraft.mc.civmodcore.util.Validation;

public class Tester {

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
		NBTCompound nbt = new NBTCompound();
		nbt.setString(STRING_KEY, expectedString);
		// Check
		Assert.assertEquals(expectedString, nbt.getString(STRING_KEY));
	}

	@Test
	public void testStringArraySerialization() {
		// Setup
		String STRING_ARRAY_KEY = "test_string_array";
		String[] expectedStringArray = { "one", "two", null, null, "five" };
		// Process
		NBTCompound nbt = new NBTCompound();
		nbt.setStringArray(STRING_ARRAY_KEY, expectedStringArray);
		// Check
		Assert.assertArrayEquals(expectedStringArray, nbt.getStringArray(STRING_ARRAY_KEY));
	}

	@Test
	public void testClassSerialization() {
		// Setup
		String expectedString = "Turpis tincidunt id aliquet risus feugiat. Donec et odio pellentesque diam " +
				"volutpat commodo sed egestas. Mattis nunc sed blandit libero volutpat sed. Pellentesque diam " +
				"volutpat commodo sed egestas egestas fringilla phasellus. Nec feugiat in fermentum posuere urna " +
				"nec. Urna neque viverra justo nec ultrices dui sapien. Interdum consectetur libero id faucibus " +
				"nisl tincidunt eget nullam non. Dignissim sodales ut eu sem integer vitae justo eget magna. " +
				"Amet est placerat in egestas. Pharetra vel turpis nunc eget lorem. Mauris sit amet massa vitae " +
				"tortor condimentum lacinia quis vel. Faucibus turpis in eu mi bibendum neque egestas congue " +
				"quisque. Sit amet venenatis urna cursus eget nunc.";
		// Process
		ExampleSerializable expected = new ExampleSerializable();
		expected.setMessage(expectedString);
		NBTCompound nbt = NBTSerialization.serialize(expected);
		ExampleSerializable actual = (ExampleSerializable) NBTSerialization.deserialize(nbt);
		// Check
		Assert.assertNotNull(actual);
		Assert.assertEquals(expectedString, actual.getMessage());
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
		NBTCompound nbt = new NBTCompound();
		nbt.setString(STRING_KEY, expectedString);
		byte[] data = NBTCompound.toBytes(nbt);
		NBTCompound actual = NBTCompound.fromBytes(data);
		// Check
		Assert.assertTrue(Validation.checkValidity(actual));
		Assert.assertEquals(expectedString, actual.getString(STRING_KEY));
	}

	@Test
	public void testNullSerialization() {
		// Setup
		String STRING_KEY = "test_null_string";
		// Process
		NBTCompound nbt = new NBTCompound();
		nbt.setString(STRING_KEY, null);
		byte[] data = NBTCompound.toBytes(nbt);
		NBTCompound actual = NBTCompound.fromBytes(data);
		// Check
		Assert.assertTrue(Validation.checkValidity(actual));
		Assert.assertNull(actual.getString(STRING_KEY));
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
		NBTCompound nbt = new NBTCompound();
		nbt.setString(STRING_KEY, expectedString);
		nbt.clear();
		// Check
		Assert.assertNull(nbt.getString(STRING_KEY));
	}

}
