package vg.civcraft.mc.civmodcore.config;

import java.util.HashMap;
import org.bukkit.Material;
import org.junit.Assert;
import org.junit.Test;

public class ConfigTests {

	@Test
	public void pseudoConfigTest() {
		// Setup
		final var data = new HashMap<String, Object>() {{
			put("key", "value");
			put("material", Material.NAME_TAG.name());
			put("amount", 100);
		}};
		// Process
		final var config = ConfigSection.fromData(data);
		// Check
		Assert.assertEquals("value", config.getString("key"));
		Assert.assertEquals(Material.NAME_TAG.name(), config.getString("material"));
		Assert.assertEquals(100, config.getInt("amount"));
	}

}
