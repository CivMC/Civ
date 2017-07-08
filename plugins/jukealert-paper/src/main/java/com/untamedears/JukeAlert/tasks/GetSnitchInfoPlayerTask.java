/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.untamedears.JukeAlert.tasks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.group.Group;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.chat.SendSnitchInfo;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.LoggedAction;

/**
 *
 * @author Dylan
 */
public class GetSnitchInfoPlayerTask implements Runnable {

	private final List<String> info = new ArrayList<String>();

	private Snitch snitch;

	private boolean shouldCensor;

	private final int offset;

	private int snitchId;

	private String snitchName;

	private boolean isJukebox;

	private String group;

	private LoggedAction filterAction;

	private String filterPlayer;

	private final Player player;

	private final JukeAlert plugin;

	public GetSnitchInfoPlayerTask(JukeAlert plugin, Snitch snitch, int offset, Player player, boolean shouldCensor) {

		this.snitch = snitch;
		this.offset = offset;
		this.player = player;
		this.plugin = plugin;
		this.shouldCensor = shouldCensor;
		this.filterAction = null;
		this.filterPlayer = "";
	}

	public GetSnitchInfoPlayerTask(JukeAlert plugin, Snitch snitch, int offset, Player player, boolean shouldCensor,
			LoggedAction filterAction, String filterPlayer) {

		this.snitch = snitch;
		this.offset = offset;
		this.player = player;
		this.plugin = plugin;
		this.shouldCensor = shouldCensor;
		this.filterAction = filterAction;
		this.filterPlayer = filterPlayer;
	}

	public GetSnitchInfoPlayerTask(JukeAlert plugin, Group group, int offset, Player player) {

		Location loc = new Location(Bukkit.getWorld("world"), 0, 0, 0);
		this.snitch = new Snitch(loc, group, true, false);
		this.offset = offset;
		this.player = player;
		this.plugin = plugin;
		this.snitch.setId(-1);
		this.shouldCensor = false;
		this.filterAction = null;
		this.filterPlayer = "";
	}

	@Override
	public void run() {

		SendSnitchInfo sendSnitchInfo;
		if (group == null) {
			sendSnitchInfo = new SendSnitchInfo(plugin.getJaLogger().getSnitchInfo(this.snitch.getId(), (offset - 1) * 10,
				this.filterAction, this.filterPlayer), this.player, offset, this.snitch, shouldCensor, false);
		} else {
			sendSnitchInfo = new SendSnitchInfo(plugin.getJaLogger().getSnitchGroupInfo(group, (offset - 1) * 10),
				this.player, offset, this.snitch, false, true);
		}
		sendSnitchInfo.run();
	}

	public List<String> getInfo() {

		return info;
	}
}
