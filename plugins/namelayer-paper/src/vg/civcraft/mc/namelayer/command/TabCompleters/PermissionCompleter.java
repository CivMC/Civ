package vg.civcraft.mc.namelayer.command.TabCompleters;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by isaac on 2/2/2015.
 *
 * Used by tab completers to get a list of user types
 */
public class PermissionCompleter {


    public static List<String> complete(String lastArg) {
        PermissionType[] types = PermissionType.values();
        List<String> type_strings = new LinkedList<>();
        List<String> result = new LinkedList<>();

        for (PermissionType type: types){
            type_strings.add(type.toString());
        }

        if (lastArg != null) {
            for(String type: type_strings){
                if (type.startsWith(lastArg))
                    result.add(type);
            }
        } else {
            result = type_strings;
        }

        return result;
    }
}
