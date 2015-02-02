package vg.civcraft.mc.namelayer.command.TabCompleters;

import vg.civcraft.mc.namelayer.GroupManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by isaac on 2/2/2015.
 *
 * Used by tab completers to get a list of user types
 */
public class MemberTypeCompleter {


    public static List<String> complete(String lastArg) {
        GroupManager.PlayerType[] types = GroupManager.PlayerType.values();
        List<String> type_strings = new LinkedList<>();
        List<String> result = new LinkedList<>();

        for (GroupManager.PlayerType type: types){
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
