package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.programmerdan.minecraft.simpleadminhacks.BroadcastLevel;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.CTAnnounceConfig;

import net.minelink.ctplus.event.PlayerCombatTagEvent;

/**
 * Ties into CombatTagPlus, listens for {@link PlayerCombatTagEvent}
 * 
 * @author ProgrammerDan
 */
public class CTAnnounce extends SimpleHack<CTAnnounceConfig> implements Listener{

	public static final String NAME = "CombatTagAnnounce";

	private Map<UUID, Long> lastCTAnnounce;

	public CTAnnounce(SimpleAdminHacks plugin, CTAnnounceConfig config) {
		super(plugin, config);

		if (!plugin.serverHasPlugin("CombatTagPlus")){
			plugin.log("CombatTagPlus not found, disabling broadcast hook.");
			config.setEnabled(false);
		}
	}

	/**
	 * Capture player combattag and broadcast it
	 * 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void ctEvent(PlayerCombatTagEvent event) {
		if (!config.isEnabled()) return; // ignore if off
		if (event.getVictim() == null || event.getAttacker() == null) return; // ignore non-pvp and admin-pvp
		plugin().debug("  Victim: {0} Attacker: {1}", event.getVictim().getName(), event.getAttacker().getName());

		// Throttle broadcast frequency
		Long lastTag = lastCTAnnounce.get(event.getVictim().getUniqueId());
		Long now = System.currentTimeMillis();
		if (lastTag != null && now - lastTag < config.getBroadcastDelay()) return;
		lastCTAnnounce.put(event.getVictim().getUniqueId(), now);

		// Prepare message
		String cleanMessage = cleanMessage(event);

		// Overlap is possible. Some people might get double-notified
		for (BroadcastLevel level : config.getBroadcast()) {
			plugin().debug("  Broadcast to {0}", level);
			switch(level) {
			case OP:
				plugin().serverOperatorBroadcast(cleanMessage);
				break;
			case PERM:
				plugin().serverBroadcast(cleanMessage); 
				break;
			case CONSOLE:
				plugin().serverSendConsoleMessage(cleanMessage);
				break;
			case ALL:
				plugin().serverOnlineBroadcast(cleanMessage);
				break;
			}
		}
	}

	private String cleanMessage(PlayerCombatTagEvent event) {
		return ChatColor.translateAlternateColorCodes('&',
				config.getBroadcastMessage()
					.replaceAll("%Victim%", event.getVictim().getDisplayName())
					.replaceAll("%Attacker%", event.getAttacker().getDisplayName())
				);
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering CombatTagEvent listener");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
	}

	@Override
	public void dataBootstrap() {
		lastCTAnnounce = new ConcurrentHashMap<>();
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		lastCTAnnounce.clear();
		lastCTAnnounce = null;
	}

	@Override
	public String status() {
		if (config != null && config.isEnabled()) {
			return "CombatTagPlus.PlayerCombatTagEvent monitoring active";
		} else {
			return "CombatTagPlus.PlayerCombatTagEvent monitoring not active";
		}
	}

	public static CTAnnounceConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new CTAnnounceConfig(plugin, config);
	}
}
