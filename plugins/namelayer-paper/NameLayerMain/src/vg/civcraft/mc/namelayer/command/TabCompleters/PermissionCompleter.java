package vg.civcraft.mc.namelayer.command.TabCompleters;

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
        List<String> type_strings = new LinkedList<>();
        List<String> result = new LinkedList<>();

        for (PermissionType type: PermissionType.getAllPermissions()){
            type_strings.add(type.getName());
        }

        if (lastArg != null) {
            for(String type: type_strings){
                if (type.toLowerCase().startsWith(lastArg.toLowerCase()))
                    result.add(type);
            }
        } else {
            result = type_strings;
        }

        return result;
    }
}
