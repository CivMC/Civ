package vg.civcraft.mc.namelayer.command.TabCompleters;

import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by isaac on 2/2/2015.
 */
public class InviteTabCompleter {
    public static List<String> complete(String lastArg, Player sender) {
        UUID uuid = NameAPI.getUUID(sender.getName());
        Set<Group> groups = PlayerListener.getNotifications(uuid);
        List<String> result = new LinkedList<>();
        
        if (groups == null)
            return new ArrayList<>();

        for (Group group : groups){
            if (lastArg == null || group.getName().toLowerCase().startsWith(lastArg.toLowerCase())){
                result.add(group.getName());
            }
        }

        return result;
    }
}
