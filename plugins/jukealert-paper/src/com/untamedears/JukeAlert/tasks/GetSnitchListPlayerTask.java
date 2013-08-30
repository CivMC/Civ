
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

    public GetSnitchListPlayerTask(JukeAlert plugin, int offset, Player player) {
        this.offset = offset;
        this.player = player;
        this.plugin = plugin;
    }

	@Override
    public void run() {
		SendSnitchList sendSnitchInfo;
			sendSnitchInfo = new SendSnitchList(plugin.getJaLogger().getSnitchList(player.getName(), (offset-1) * 10, 5), player, offset);
		sendSnitchInfo.run();
    }

    public List<String> getInfo() {
        return info;
    }
}
