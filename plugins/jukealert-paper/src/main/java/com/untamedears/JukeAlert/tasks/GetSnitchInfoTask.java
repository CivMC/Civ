/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.JukeAlert.tasks;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.SnitchAction;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;

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
        List<SnitchAction> entries = plugin.getJaLogger().getSnitchInfo(snitchId, offset * 10);
        if (entries != null && !entries.isEmpty()){
            for (SnitchAction entry : entries){
                info.add(JukeAlertLogger.createInfoString(entry, false, false));
            }
        }
    }

    public List<String> getInfo() {
        return info;
    }
}