package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.BroadcastLevel;
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
	 * Is this config active?
	 * 
	 * Default of false.
	 */
	private boolean enabled;
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

	public CTAnnounceConfig(ConfigurationSection base) {
		super(base);
	}
	
	@Override
	protected void wireup(ConfigurationSection config) {
		this.enabled = config.getBoolean("enabled", false);
		this.broadcastDelay = config.getLong("delay", 5000l);
		this.broadcastMessage = config.getString("message", "%Victim% was combat tagged by %Attacker%");
		
		List<String> broadcastTo = config.getStringList("broadcast");
		if (this.broadcast == null) {
			this.broadcast = new LinkedList<BroadcastLevel>();
		}
		
		this.broadcast.clear();

		if (broadcastTo == null) {
			this.enabled = false; // disable if no broadcasters.
		}
		for (String type : broadcastTo) {
			try {
				broadcast.add(BroadcastLevel.valueOf(type));
			} catch (IllegalArgumentException iae) {
				// noop
			}
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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
