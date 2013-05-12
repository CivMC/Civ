/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.JukeAlert.tasks;

import com.untamedears.JukeAlert.JukeAlert;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dylan
 */
public class GetSnitchInfoTask implements Runnable {

    private List<String> info = new ArrayList<String>();
    private int offset;
    private int snitchId;
    private JukeAlert plugin;

    public GetSnitchInfoTask(JukeAlert plugin, int snitchId, int offset) {
        this.snitchId = snitchId;
        this.offset = offset;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        info = plugin.getJaLogger().getSnitchInfo(snitchId, offset * 10);
    }

    public List<String> getInfo() {
        return info;
    }
}
