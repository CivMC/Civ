package vg.civcraft.mc.namelayer.permission;

import vg.civcraft.mc.namelayer.group.Group;

public class PermissionHandler {

    /**
     * Returns a GroupPermission view for the supplied group. The returned object reads from the cached Group state and
     * does not cache itself, so callers do not need to worry about stale instances after cache reloads.
     */
    public GroupPermission getGroupPermission(final Group group) {
        return new GroupPermission(group);
    }
}
