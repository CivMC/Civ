package com.untamedears.citadel.command;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.FactionMember;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;

import static com.untamedears.citadel.Utility.sendMessage;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 11:23 AM
 */
public class ModifyFaction extends PlayerCommand {

    public ModifyFaction(Citadel plugin) {
        super(plugin, 1);
    }

    public boolean onCommand(Command command, String[] args) {
        String name = player.getDisplayName();
        FactionMember member = plugin.dao.findGroupMember(name, args[0]);

        String cmd = command.getName().toLowerCase();
        if (cmd.equals("ctallow")) {
            if (member == null) {
                member = new FactionMember(name, args[0]);
                plugin.dao.save(member);
                sendMessage(player, ChatColor.GREEN, "Allowed %s access to your group's blocks", member.getMemberName());
            }
        } else if (cmd.equals("ctdisallow")) {
            if (member != null) {
                plugin.dao.delete(member);
                sendMessage(player, ChatColor.GREEN, "Disallowed %s from access to your group's blocks", member.getMemberName());
            }
        }
        return true;
    }
}
