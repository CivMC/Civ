package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.BroadcastLevel;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Some simple new player related configuration options.
 * Might have overlap with other plugins at some point (CivMenu, looking at you)
 *  but for now should let us do a few neat-o things.
 *  
 * @author ProgrammerDan
 */
public class NewfriendAssistConfig extends SimpleHackConfig {
	/**
	 * What should I send to people when I announce a newfriend?
	 */
	private String announceMessage = "&%Player% is brand new!";
	/**
	 * Who should I broadcast to?
	 */
	private List<BroadcastLevel> announceBroadcast;
	
	//private List<HelpTips> helps; // TODO

	public NewfriendAssistConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}
	
	public NewfriendAssistConfig(ConfigurationSection base) {
		super(SimpleAdminHacks.instance(), base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
	    /*name: NewfriendAssist
	    enabled: true
	    announce:
	      message: '%Player% is brand new!'
	      broadcast: [PERM, CONSOLE]
	    helptips: on
	    helptips_end: 20m*/
		
		this.announceMessage = config.getString("announce.message", announceMessage);
		
		List<String> broadcastTo = config.getStringList("broadcast");
		if (this.announceBroadcast == null) {
			this.announceBroadcast = new LinkedList<BroadcastLevel>();
		}
		this.announceBroadcast.clear();
		for (String type : broadcastTo) {
			try {
				this.announceBroadcast.add(BroadcastLevel.valueOf(type));
				
				plugin().log(Level.INFO, " broadcast: {0}", type);
			} catch (IllegalArgumentException iae) {
				// noop
			}
		}
	}
	
	public List<BroadcastLevel> getAnnounceBroadcast() {
		return announceBroadcast;
	}

	public String getAnnounceMessage() {
		return announceMessage;
	}

}
