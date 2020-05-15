package vg.civcraft.mc.civmodcore.enums;

import org.junit.Assert;
import org.junit.Test;
import vg.civcraft.mc.civmodcore.util.EnumUtils;

public class Tester {

	public enum Example {
		ONE,
		TWO,
		THREE
	}

	@Test
	public void testStringSerialization() {
		// Setup
		String ENUM_KEY = EnumUtils.getSlug(Example.THREE);
		// Process
		Enum<Example> result = EnumUtils.fromSlug(Example.class, ENUM_KEY);
		// Check
		Assert.assertEquals(Example.THREE, result);
	}

}
