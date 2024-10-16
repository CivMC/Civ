package vg.civcraft.mc.namelayer.command.TabCompleters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

/**
 * Created by isaac on 2/2/2015.
 */
public class GroupTabCompleter {
    public static List<String> complete(String lastArg, PermissionType accessLevel, Player sender) {
        UUID uuid = NameAPI.getUUID(sender.getName());
        GroupManager gm = NameAPI.getGroupManager();
        List<String> groups = gm.getAllGroupNames(uuid);
        List<String> fittingGroups = new ArrayList<>();
        List<String> result = new ArrayList<>();

        if (lastArg != null){
            for (String group : groups){
                if (group.toLowerCase().startsWith(lastArg.toLowerCase())){
                    fittingGroups.add(group);
                }
			}
        } else {
            fittingGroups = groups;
        }

        if (accessLevel == null) {
            for (String groupName : fittingGroups){
                Group group  = GroupManager.getGroup(groupName);
                if (group != null && group.isMember(uuid)) {
					        result.add(groupName);
				        }
            }
        } else {
            for (String groupName : fittingGroups) {
                if (gm.hasAccess(groupName, uuid, accessLevel)) {
					result.add(groupName);
				}
            }
        }
        return result;
    }
}
