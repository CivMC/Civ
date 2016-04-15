package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.md_5.bungee.api.ChatColor;
import net.minelink.ctplus.event.PlayerCombatTagEvent;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.BroadcastLevel;
import com.programmerdan.minecraft.simpleadminhacks.configs.CTAnnounceConfig;

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
	@EventHandler(ignoreCancelled = true)
	public void CTEvent(PlayerCombatTagEvent event) {
		if (!config.isEnabled()) return; // ignore if off
		if (event.getVictim() == null || event.getAttacker() == null) return; // ignore non-pvp

		// Throttle broadcast frequency
		Long lastTag = lastCTAnnounce.get(event.getVictim().getUniqueId());
		Long now = System.currentTimeMillis();
		if (lastTag != null && now - lastTag < config.getBroadcastDelay()) return;
		lastCTAnnounce.put(event.getVictim().getUniqueId(), now);

		// Prepare message
		String cleanMessage = cleanMessage(event);
		
		// And Gooooo
		for (BroadcastLevel level : config.getBroadcast()) {
			switch(level) {
			// Overlap is possible. Some people might get double-notified
			case OP:
				for( OfflinePlayer op : plugin().getServer().getOperators()) {
					if (op.isOnline() && op.getPlayer() != null) {
						op.getPlayer().sendMessage(cleanMessage);
					}
				}
			case PERM:
				plugin().getServer().broadcast(cleanMessage, 
						plugin().config().getBroadcastPermission());
			case CONSOLE:
				plugin().getServer().getConsoleSender().sendMessage(cleanMessage);
			case ALL:
				for (Player p : plugin().getServer().getOnlinePlayers()) {
					if ( p != null && p.isOnline() )
						p.sendMessage(cleanMessage);
				}
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
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
	}

	@Override
	public void dataBootstrap() {
		lastCTAnnounce = new ConcurrentHashMap<UUID, Long>();
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
}
