package vg.civcraft.mc.civmodcore.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

public class TextTests {

	/**
	 * Tests whether two components of different order can match.
	 */
	@Test
	public void testComponentOrderEquality() {
		// Setup
		final String formerMessage = "{\"text\":\"\",\"extra\":[{\"text\":\"Test!\"}]}";
		final String latterMessage = "{\"extra\":[{\"text\":\"Test!\"}],\"text\":\"\"}";
		// Process
		final Component formerParsed = GsonComponentSerializer.gson().deserialize(formerMessage);
		final Component latterParsed = GsonComponentSerializer.gson().deserialize(latterMessage);
		// Check
		Assertions.assertEquals(formerParsed, latterParsed);
	}

	/**
	 * Tests whether two components of different arrangement but equal content can match.
	 */
	@Test
	public void testComponentArrangementEquality() {
		// Setup
		final Component formerComponent = Component.text("Test").color(NamedTextColor.RED);
		final Component latterComponent = Component.text().color(NamedTextColor.RED)
				.append(Component.text("Test")).build();
		// Check
		Assertions.assertTrue(ChatUtils.areComponentsEqual(formerComponent, latterComponent));
	}

}
