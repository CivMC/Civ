package vg.civcraft.mc.namelayer.command.TabCompleters;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.util.StringUtil;
import vg.civcraft.mc.namelayer.GroupManager;

/**
 * Created by isaac on 2/2/2015.
 *
 * Used by tab completers to get a list of user types
 */
public class MemberTypeCompleter {
    public static List<String> complete(String lastArg) {
        GroupManager.PlayerType[] types = GroupManager.PlayerType.values();
        List<String> typeStrings = new ArrayList<>(types.length);
        for (GroupManager.PlayerType type : types){
          typeStrings.add(type.toString());
        }

        if (lastArg == null) {
          return typeStrings;
        }

		    return StringUtil.copyPartialMatches(lastArg, typeStrings, new ArrayList<>());
    }
}
