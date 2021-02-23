package com.untamedears.jukealert.model.appender.config;

import com.untamedears.jukealert.JukeAlert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;

public class LeverToggleConfig implements AppenderConfig {

	private Map<BlockFace, SideEntry> sides;
	private Map<String, List<SideEntry>> sidesByTrigger;

	public LeverToggleConfig(ConfigurationSection config) {
		sides = new EnumMap<>(BlockFace.class);
		sidesByTrigger = new HashMap<>();
		Logger logger = JukeAlert.getInstance().getLogger();
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				logger.warning("Ignoring invalid entry " + key + " at " + config.getCurrentPath());
			}
			ConfigurationSection current = config.getConfigurationSection(key);
			BlockFace face;
			try {
				face = BlockFace.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException e) {
				logger.warning(key + " at " + config.getCurrentPath() + " is not a valid BlockFace, entry was ignored");
				continue;
			}
			long delay = ConfigParsing.parseTime(current.getString("disableDelay", "1 second"));
			if (!current.isList("trigger")) {
				logger.warning("No trigger actions specified for lever at " + current.getCurrentPath());
				continue;
			}
			List<String> trigger = current.getStringList("trigger");
			SideEntry entry = new SideEntry(face, delay, trigger);
			sides.put(face, entry);
			for (String action : trigger) {
				List<SideEntry> existingForTrigger = sidesByTrigger.computeIfAbsent(action, s -> new ArrayList<>());
				existingForTrigger.add(entry);
			}
		}
	}

	public List<SideEntry> getEntries(String actionType) {
		List<SideEntry> retSides = sidesByTrigger.get(actionType);
		if (retSides == null) {
			return Collections.emptyList();
		}
		return retSides;
	}

	public static class SideEntry {
		private BlockFace face;
		private long delay;
		private List<String> trigger;

		public SideEntry(BlockFace face, long delay, List<String> trigger) {
			this.face = face;
			this.delay = delay;
			this.trigger = trigger;
		}

		public BlockFace getFace() {
			return face;
		}

		public long getDelay() {
			return delay;
		}

		public List<String> getTrigger() {
			return trigger;
		}
	}

}
