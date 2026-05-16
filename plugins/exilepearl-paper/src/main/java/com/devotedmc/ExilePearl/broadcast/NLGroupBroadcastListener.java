package com.devotedmc.ExilePearl.broadcast;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.util.NameLayerPermissions;
import com.google.common.base.Preconditions;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class NLGroupBroadcastListener implements BroadcastListener {

    private final String groupName;

    public NLGroupBroadcastListener(final String groupName) {
        Preconditions.checkNotNull(groupName, "groupName");

        this.groupName = groupName;
    }

    @Override
    public void broadcast(ExilePearl pearl) {
        Location l = pearl.getHolder().getLocation();
        String name = pearl.getHolder().getName();

        Group group = GroupManager.getGroup(groupName);
        if (group == null) {
            return;
        }
        String msg = String.format(ChatUtils.parseColor(Lang.groupPearlBroadcast), groupName, pearl.getPlayerName(), name, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());

        GroupManager gm = NameLayerAPI.getGroupManager();

        if (!gm.hasAccess(group, pearl.getPlayerId(), PermissionType.getPermission(NameLayerPermissions.ALLOW_EXILE_BROADCAST)))
            return;

        for (UUID uid : group.getCurrentMembers()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) {
                p.sendMessage(msg);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NLGroupBroadcastListener other = (NLGroupBroadcastListener) o;

        return this.groupName.equals(other.groupName);
    }

    @Override
    public int hashCode() {
        return groupName.hashCode();
    }

    @Override
    public boolean contains(Object o) {
        return groupName.equals(o);
    }
}
