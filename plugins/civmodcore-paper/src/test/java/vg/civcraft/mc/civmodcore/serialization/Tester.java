package vg.civcraft.mc.civmodcore.serialization;

import org.junit.Assert;
import org.junit.Test;

public class Tester {

	@Test
	public void testClassSerialization() {
		NBTSerialization.registerNBTSerializable(ExampleSerializable.class);

		String testString = "Hello, World!";

		// Create an instance of the example serializable and store the example text.
		ExampleSerializable example = new ExampleSerializable();
		example.setMessage(testString);

		// Serialize the class into an NBT compound, which *should* hold the relevant data to convert it back into an
		// ExampleSerializable, given that it was also registered.
		NBTCompound nbt = NBTSerialization.serialize(example);

		// Attempts to create an ExampleSerializable instance based on the NBT data.
		ExampleSerializable returned = (ExampleSerializable) NBTSerialization.deserialize(nbt);

		// Tests if the message from the new instance matches that of the serialized class.
		Assert.assertEquals(returned.getMessage(), example.getMessage());

		NBTSerialization.clearAllRegistrations();
	}

	@Test
	public void testByteSerialization() {
		// Define the data that will be set and tested.
		String testString = "The pen is mightier than the sword.";
		String testStringKey = "proverb";

		// Create the NBT object that will be serialized.
		NBTCompound firstNBT = new NBTCompound();
		firstNBT.setString(testStringKey, testString);

		byte[] firstNBTData = NBTCompound.toBytes(firstNBT);

		// Attempt to recreate an NBT compound from the byte data.
		NBTCompound secondNBT = NBTCompound.fromBytes(firstNBTData);

		// Test if the stored data on the newly created compound matches what is expected to be there.
		Assert.assertNotNull(secondNBT);
		Assert.assertEquals(secondNBT.getString(testStringKey), testString);
	}

	@Test
	public void testNullSerialization() {
		// Define the data that will be set and tested.
		String testStringKey = "first";
		String testStringArrayKey = "second";
		String[] testStringArray = { "one", "two", null, null, "five" };

		// Create the NBT object that will be serialized.
		NBTCompound firstNBT = new NBTCompound();
		firstNBT.setString(testStringKey, null);
		firstNBT.setStringArray(testStringArrayKey, testStringArray);

		byte[] firstNBTData = NBTCompound.toBytes(firstNBT);

		// Attempt to recreate an NBT compound from the byte data.
		NBTCompound secondNBT = NBTCompound.fromBytes(firstNBTData);

		// Test if the stored data on the newly created compound matches what is expected to be there.
		Assert.assertNotNull(secondNBT);
		Assert.assertNull(secondNBT.getString(testStringKey));
		Assert.assertArrayEquals(secondNBT.getStringArray(testStringArrayKey), testStringArray);
	}

}
