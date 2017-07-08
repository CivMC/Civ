package com.untamedears.JukeAlert.listener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.chat.TextComponent;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.external.Mercury;
import com.untamedears.JukeAlert.util.Utility;

public class MercuryListener implements Listener {

	private List<String> channels = new ArrayList<String>();

	public MercuryListener() {
		MercuryAPI.addChannels(Mercury.getChannels());
		for (String x: Mercury.getChannels()) {
			channels.add(x);
		}
	}

	private long failureReportDelay = 10000l;

	private long lastAsyncMessageFailure = System.currentTimeMillis() - failureReportDelay;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void asyncMercuryMessageEvent(AsyncPluginBroadcastMessageEvent event) {

		String channel = event.getChannel();
		if (!channels.contains(channel)) {
			return;
		}
		String m = event.getMessage();
		int spc = m.indexOf(" ");
		final String message = m.substring(spc + 1);
		final String grp = m.substring(0, spc);
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					Group g = GroupManager.getGroup(grp);
					if (g != null) {
						Utility.notifyGroup(g, new TextComponent(ChatColor.AQUA + message.toString()));
					} else {
						if (System.currentTimeMillis() - lastAsyncMessageFailure > failureReportDelay) {
							JukeAlert.getInstance().getLogger().log(
								Level.WARNING, "asyncMercuryMessageEvent encountered a null group when looking up {0}",
								grp);
							lastAsyncMessageFailure = System.currentTimeMillis();
						}
					}
				} catch (SQLException | NullPointerException e) {
					if (System.currentTimeMillis() - lastAsyncMessageFailure > failureReportDelay) {
						JukeAlert.getInstance().getLogger().log(
							Level.WARNING, "asyncMercuryMessageEvent generated an exception", e);
						lastAsyncMessageFailure = System.currentTimeMillis();
					}
				}
			}
		}.runTask(JukeAlert.getInstance());
	}
}
