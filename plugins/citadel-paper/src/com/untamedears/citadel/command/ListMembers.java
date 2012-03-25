package com.untamedears.citadel.command;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.FactionMember;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;

import java.util.Set;

import static com.untamedears.citadel.Utility.sendMessage;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 11:35 AM
 */
public class ListMembers extends PlayerCommand {
    public ListMembers(Citadel plugin) {
        super(plugin, 0);
    }

    @Override
    public boolean onCommand(Command command, String[] args) {
        StringBuilder buffer = new StringBuilder();
        String name = player.getDisplayName();
        Set<FactionMember> members = plugin.dao.findGroupMembers(name);
        if (members.isEmpty()) {
            sendMessage(player, ChatColor.GREEN, "You have no members in your group");
        } else {
            for (FactionMember member : plugin.dao.findGroupMembers(name)) {
                if (buffer.length() > 0) buffer.append(", ");
                buffer.append(member.getMemberName());
            }
            buffer.insert(0, "Your group members: ");
            sendMessage(player, ChatColor.GREEN, buffer.toString());
        }
        return true;
    }
}
