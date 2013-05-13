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

    private List<String> info = new ArrayList<String>();
    private int offset;
    private int snitchId;
    private Player player;
    private JukeAlert plugin;

    public GetSnitchInfoPlayerTask(JukeAlert plugin, int snitchId, int offset, Player player) {
        this.snitchId = snitchId;
        this.offset = offset;
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        SendSnitchInfo runnable = new SendSnitchInfo(plugin.getJaLogger().getSnitchInfo(snitchId, offset * 10), this.player);
        runnable.run();
    }

    public List<String> getInfo() {
        return info;
    }
}
