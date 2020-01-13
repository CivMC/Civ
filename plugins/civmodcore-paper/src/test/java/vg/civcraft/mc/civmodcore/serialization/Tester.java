package vg.civcraft.mc.civmodcore.serialization;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import vg.civcraft.mc.civmodcore.api.NBTCompound;

public class Tester {

	@Test
	public void testSerialization() {
		NBTSerialization.registerNBTSerializable(ExampleSerializable.class);

		String testString = "Hello, World!";

		// Create an instance of the example serializable and store the example text
		ExampleSerializable example = new ExampleSerializable();
		example.setMessage(testString);

		// Serialise the class into an NBT compound, which *should* hold the relevant data
		// to convert it back into an ExampleSerializable, given that it was also registered
		NBTCompound nbt = NBTSerialization.serialize(example);

		// Attempts to create an ExampleSerializable instance based on the NBT data
		ExampleSerializable returned = NBTSerialization.deserialize(nbt);

		// Tests if the message from the new instance matches that of the serialised class
		assertEquals(returned.getMessage(), example.getMessage());

		NBTSerialization.clearAllRegistrations();
	}

}
