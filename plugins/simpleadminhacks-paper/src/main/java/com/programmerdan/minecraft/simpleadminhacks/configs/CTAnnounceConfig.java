package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.BroadcastLevel;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * CombatTag event hook hack
 * 
 * @author ProgrammerDan
 *
 */
public class CTAnnounceConfig extends SimpleHackConfig {

	/**
	 * Who should I broadcast to?
	 */
	private List<BroadcastLevel> broadcast;
	/**
	 * How long to wait inbetween broadcasts for a particular player.
	 * 
	 * Default of 5 seconds (5000 ms).
	 */
	private long broadcastDelay;
	/**
	 * The message to send to notify of event.
	 * 
	 * %Victim% -- who was struck
	 * %Attacker% -- who struck
	 */
	private String broadcastMessage;

	public CTAnnounceConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	public CTAnnounceConfig(ConfigurationSection base) {
		super(SimpleAdminHacks.instance(), base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.broadcastDelay = config.getLong("delay", 5000l);
		this.broadcastMessage = config.getString("message", "%Victim% was combat tagged by %Attacker%");

		plugin().log(Level.INFO, " delay: {0}, message: {1}", this.broadcastDelay, this.broadcastMessage);

		List<String> broadcastTo = config.getStringList("broadcast");
		if (this.broadcast == null) {
			this.broadcast = new LinkedList<BroadcastLevel>();
		}

		this.broadcast.clear();

		if (broadcastTo == null) {
			this.setEnabled(false); // disable if no broadcasters.
		}

		for (String type : broadcastTo) {
			try {
				broadcast.add(BroadcastLevel.valueOf(type));

				plugin().log(Level.INFO, " broadcast: {0}", type);
			} catch (IllegalArgumentException iae) {
				// noop
			}
		}
	}

	public List<BroadcastLevel> getBroadcast() {
		return broadcast;
	}

	public long getBroadcastDelay() {
		return broadcastDelay;
	}

	public String getBroadcastMessage() {
		return broadcastMessage;
	}
}
