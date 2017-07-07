
package com.untamedears.JukeAlert.tasks;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.chat.SendSnitchList;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class GetSnitchListPlayerTask implements Runnable {

	private final List<String> info = new ArrayList<String>();

	private final int offset;

	private final Player player;

	private final JukeAlert plugin;

	private final List<String> groupNames;

	private final boolean truncateNames;

	public GetSnitchListPlayerTask(JukeAlert plugin, int offset, Player player, List<String> groupNames,
			boolean truncateNames) {

		this.offset = offset;
		this.player = player;
		this.plugin = plugin;
		this.groupNames = groupNames;
		this.truncateNames = truncateNames;
	}

	@Override
	public void run() {

		SendSnitchList sendSnitchList = plugin.getJaLogger().getSnitchList(player, offset, groupNames, truncateNames);
		if (sendSnitchList != null) {
			sendSnitchList.run();
		}
	}

	public List<String> getInfo() {

		return info;
	}
}
