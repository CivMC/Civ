/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.JukeAlert.tasks;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.chat.SendSnitchInfo;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

/**
 *
 * @author Dylan
 */
public class GetSnitchInfoPlayerTask implements Runnable {

    private final List<String> info = new ArrayList<String>();
    private boolean shouldCensor;
    private final int offset;
    private int snitchId;
    private String group;
    private final Player player;
    private final JukeAlert plugin;

    public GetSnitchInfoPlayerTask(JukeAlert plugin, int snitchId, int offset, Player player, boolean shouldCensor) {
        this.snitchId = snitchId;
        this.offset = offset;
        this.player = player;
        this.plugin = plugin;
        this.shouldCensor = shouldCensor;
    }

    public GetSnitchInfoPlayerTask(JukeAlert plugin, String group,
			int offset, Player player) {
    	this.group = group;
        this.offset = offset;
        this.player = player;
        this.plugin = plugin;
	}

	@Override
    public void run() {
		SendSnitchInfo sendSnitchInfo;
		if (group == null)
			sendSnitchInfo = new SendSnitchInfo(plugin.getJaLogger().getSnitchInfo(snitchId, (offset-1) * 10), this.player, offset, shouldCensor);
		else
			sendSnitchInfo = new SendSnitchInfo(plugin.getJaLogger().getSnitchGroupInfo(group, (offset-1) * 10), this.player, offset, false);
		sendSnitchInfo.run();
    }

    public List<String> getInfo() {
        return info;
    }
}
