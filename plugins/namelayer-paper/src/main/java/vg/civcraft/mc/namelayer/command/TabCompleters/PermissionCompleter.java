package vg.civcraft.mc.namelayer.command.TabCompleters;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.util.StringUtil;
import vg.civcraft.mc.namelayer.permission.PermissionType;

/**
 * Created by isaac on 2/2/2015.
 *
 * Used by tab completers to get a list of user types
 */
public class PermissionCompleter {
    public static List<String> complete(String lastArg) {
        List<String> typeStrings = new ArrayList<>();

        for (PermissionType type : PermissionType.getAllPermissions()) {
            typeStrings.add(type.getName());
        }

        if (lastArg == null) {
          	return typeStrings;
        } else {
            return StringUtil.copyPartialMatches(lastArg, typeStrings, new ArrayList<>());
        }
    }
}
