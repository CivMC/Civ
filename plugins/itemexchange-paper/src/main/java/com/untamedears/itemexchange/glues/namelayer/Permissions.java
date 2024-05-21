package com.untamedears.itemexchange.glues.namelayer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public final class Permissions {
    public static final String CHESTS_PERMISSION = "CHESTS";
    public static final String PURCHASE_PERMISSION = "PURCHASE_PERMISSION";

    public static boolean testPermission(
        final @NotNull String permission,
        final @NotNull Group group,
        final @NotNull Player player
    ) {
        final PermissionType matchedPermission = PermissionType.getPermission(permission);
        if (matchedPermission == null) {
            return false;
        }
        return NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(), matchedPermission);
    }
}
