package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.ModifierHandler;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "DISPLAY", order = 100)
public final class DisplayNameModifier extends ModifierData {

	public static final DisplayNameModifier TEMPLATE = new DisplayNameModifier();

	private static final String DISPLAY_NAME_KEY = "displayName";

	private String displayName;

	@Override
	public DisplayNameModifier construct(ItemStack item) {
		DisplayNameModifier modifier = new DisplayNameModifier();
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			modifier.setDisplayName(meta.getDisplayName());
		}
		return modifier;
	}

	@Override
	public boolean isBroken() {
		return false;
	}

	@Override
	public boolean conforms(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return false;
		}
		if (hasDisplayName() != meta.hasDisplayName()) {
			return false;
		}
		if (hasDisplayName() && !StringUtils.equals(getDisplayName(), meta.getDisplayName())) {
			return false;
		}
		return true;
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		if (hasDisplayName()) {
			nbt.setString(DISPLAY_NAME_KEY, getDisplayName());
		}
		else {
			nbt.remove(DISPLAY_NAME_KEY);
		}
	}

	@Nonnull
	public static DisplayNameModifier fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new DisplayNameModifier();
		modifier.setDisplayName(nbt.getString(DISPLAY_NAME_KEY));
		return modifier;
	}

	@Override
	public String toString() {
		return getSlug() +
				"{" +
				"name=" + getDisplayName() +
				"}";
	}

	// ------------------------------------------------------------
	// Commands
	// ------------------------------------------------------------

	@Subcommand("displayname|display|name")
	@Description("Sets or resets the item's display name.")
	@Syntax("[name]")
	public void commandSetDisplayName(Player player, @Optional String value) {
		try (ModifierHandler<DisplayNameModifier> handler = new ModifierHandler<>(player, this)) {
			if (Strings.isNullOrEmpty(value)) {
				handler.setModifier(null);
				handler.relay(ChatColor.GREEN + "Now ignoring display names.");
			}
			else {
				handler.ensureModifier().setDisplayName(value);
				handler.relay(ChatColor.GREEN + "Display name set to: " + value);
			}
		}
	}

	@Subcommand("ignoredisplayname|ignoredisplay|ignorename")
	@Description("Toggles the rule of ignoring display names.")
	public void commandToggleIgnoreDisplayName(Player player) {
		try (ModifierHandler<DisplayNameModifier> handler = new ModifierHandler<>(player, this)) {
			if (handler.getModifier() == null) {
				handler.ensureModifier();
				handler.relay(ChatColor.GREEN + "That rule will no longer ignore display names.");
			}
			else {
				handler.setModifier(null);
				handler.relay(ChatColor.GREEN + "That rule will now ignore display names.");
			}
		}
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public boolean hasDisplayName() {
		return !Strings.isNullOrEmpty(this.displayName);
	}

	public String getDisplayName() {
		if (Strings.isNullOrEmpty(this.displayName)) {
			return null;
		}
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
