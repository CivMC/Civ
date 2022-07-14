package com.untamedears.itemexchange.glues.namelayer;

import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public enum PermissionsGlue {

	PURCHASE_PERMISSION("PURCHASE_PERMISSION") {
		@Override
		protected PermissionType registerPermission() {
			return PermissionType.registerPermission(this.slug,
					List.of(GroupManager.PlayerType.MEMBERS,
							GroupManager.PlayerType.MODS,
							GroupManager.PlayerType.ADMINS,
							GroupManager.PlayerType.OWNER),
					"Determines whether players can purchase from shops limited to this group.");
		}
	},

	CHESTS("CHESTS") {
		@Override
		protected PermissionType registerPermission() {
			return null; // If this gets here, Citadel is not loaded
		}
	};

	// ------------------------------------------------------------
	// Implementation
	// ------------------------------------------------------------

	protected final String slug;
	private PermissionType permission;

	PermissionsGlue(final @NotNull String slug) {
		this.slug = Objects.requireNonNull(slug);
		this.permission = null;
	}

	protected abstract PermissionType registerPermission();

	private PermissionType INTERNAL_getPermission() {
		if (this.permission != null) {
			return this.permission;
		}
		this.permission = PermissionType.getPermission(this.slug);
		if (this.permission != null) {
			return this.permission;
		}
		return this.permission = registerPermission();
	}

	/**
	 * Tests whether a given player has this permission on a given group.
	 *
	 * @param group The group to test.
	 * @param player The player to test.
	 * @return Returns true if the player has
	 */
	public final boolean testPermission(final Group group,
										final Player player) {
		if (group == null || player == null) {
			return false;
		}
		final PermissionType permission = INTERNAL_getPermission();
		if (permission == null) {
			return false;
		}
		return NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(), permission);
	}

	static void init() {
		for (final PermissionsGlue permission : values()) {
			permission.permission = permission.registerPermission();
		}
	}

	static void reset() {
		for (final var permission : values()) {
			permission.permission = null;
		}
	}

}
