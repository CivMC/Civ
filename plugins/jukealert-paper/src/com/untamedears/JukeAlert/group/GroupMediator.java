package com.untamedears.JukeAlert.group;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.entity.Faction;

public class GroupMediator {

    GroupManager groupManager;

    public GroupMediator() {
        this.groupManager = Citadel.getGroupManager();
    }

    public Faction getGroupByName(String groupName) {
        return groupManager.getGroup(groupName);
    }
}
