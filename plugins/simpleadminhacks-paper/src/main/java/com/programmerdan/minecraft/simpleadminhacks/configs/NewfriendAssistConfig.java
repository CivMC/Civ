package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

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
	private String announceMessage;
	/**
	 * Who should I broadcast to?
	 */
	private List<BroadcastLevel> announceBroadcast;
	/**
	 * Should I give out Newbie Kits?
	 */
	private boolean hasNewbieKit;
	/**
	 * What is the newbie kit composed of.
	 */
	private ItemStack[] newbieKit;

	//private List<HelpTips> helps; // TODO

	public NewfriendAssistConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	public NewfriendAssistConfig(ConfigurationSection base) {
		super(SimpleAdminHacks.instance(), base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {		
		this.announceMessage = config.getString("announce", "&f%Player% is brand new!");

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
		this.hasNewbieKit = config.getBoolean("introkit.enabled", false);
		if (this.hasNewbieKit) {
			List<?> rawList = config.getList("introkit.contents");
			if (rawList != null && rawList.size() > 0) {
				try {
					this.newbieKit = rawList.toArray(new ItemStack[rawList.size()]);
					plugin().log(Level.INFO, " introkit enabled");
				} catch(ArrayStoreException ase) {
					plugin().log(Level.WARNING, " introkit was enabled, but is invalid", ase);
				}
			} else {
				plugin().log(Level.WARNING, " introkit was enabled, but is missing or empty");
			}
		} else {
			plugin().log(Level.INFO, " introkit disabled");
		}
	}

	public List<BroadcastLevel> getAnnounceBroadcast() {
		return announceBroadcast;
	}

	public String getAnnounceMessage() {
		return announceMessage;
	}

	public boolean isIntroKitEnabled() {
		return hasNewbieKit;
	}

	public ItemStack[] getIntroKit() {
		return newbieKit;
	}
}
