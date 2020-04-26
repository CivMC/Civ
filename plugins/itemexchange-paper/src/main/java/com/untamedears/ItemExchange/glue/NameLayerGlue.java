package com.untamedears.itemexchange.glue;

import com.google.common.base.Strings;
import java.util.Arrays;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.util.DependencyGlue;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public final class NameLayerGlue extends DependencyGlue {

	public static final String PURCHASE_PERMISSION = "ITEM_EXCHANGE_GROUP_PURCHASE";

	public static final String ESTABLISH_PERMISSION = "ITEM_EXCHANGE_GROUP_ESTABLISH";

	public static final NameLayerGlue INSTANCE = new NameLayerGlue();

	private NameLayerGlue() {
		super("NameLayer");
	}

	@Override
	protected void onGlueEnabled() {
		PermissionType.registerPermission(PURCHASE_PERMISSION,
				Arrays.asList(GroupManager.PlayerType.MEMBERS,
						GroupManager.PlayerType.MODS,
						GroupManager.PlayerType.ADMINS,
						GroupManager.PlayerType.OWNER),
				"The ability to purchase exchanges set to this group.");
		PermissionType.registerPermission(ESTABLISH_PERMISSION,
				Arrays.asList(GroupManager.PlayerType.MODS,
						GroupManager.PlayerType.ADMINS,
						GroupManager.PlayerType.OWNER),
				"The ability to set exchanges to be exclusive to this group.");
	}

	public boolean hasAccess(String group, String permission, Player player) {
		if (!isSafeToUse() || Strings.isNullOrEmpty(group)) {
			return false;
		}
		return hasAccess(GroupManager.getGroup(group), permission, player);
	}

	public boolean hasAccess(Group group, String permission, Player player) {
		if (!isSafeToUse() || group == null || Strings.isNullOrEmpty(permission) || player == null) {
			return false;
		}
		PermissionType perm = PermissionType.getPermission(permission);
		if (perm == null) {
			return false;
		}
		GroupManager manager = NameAPI.getGroupManager();
		if (manager == null) {
			return false;
		}
		return manager.hasAccess(group, player.getUniqueId(), perm);
	}

}
