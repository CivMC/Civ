package vg.civcraft.mc.namelayer.command.TabCompleters;

import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by isaac on 2/2/2015.
 */
public class GroupTabCompleter {
    public static List<String> complete(String lastArg, PermissionType accessLevel, Player sender) {
        UUID uuid = NameAPI.getUUID(sender.getName());
        GroupManager gm = NameAPI.getGroupManager();
        List<String> groups = gm.getAllGroupNames(uuid);
        List<String> fitting_groups = new LinkedList<>();
        List<String> result = new LinkedList<>();

        if (lastArg != null){
            for (String group : groups){
                if (group.toLowerCase().startsWith(lastArg.toLowerCase())){
                    fitting_groups.add(group);
                } else {
                }
            }
        } else {
            fitting_groups = groups;
        }

        if (accessLevel == null) {
            for (String group_name: fitting_groups){
                Group group  = gm.getGroup(group_name);
                if (group.isMember(uuid))
                    result.add(group_name);
            }
        } else {
            for (String group_name : fitting_groups) {
                if (gm.hasAccess(group_name, uuid, accessLevel)) 
                    result.add(group_name);
            }
        }
        return result;
    }
}
