package vg.civcraft.mc.civmodcore.config;

import java.util.HashMap;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
		Assertions.assertEquals("value", config.getString("key"));
		Assertions.assertEquals(Material.NAME_TAG.name(), config.getString("material"));
		Assertions.assertEquals(100, config.getInt("amount"));
	}

}
