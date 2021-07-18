package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.ModifierHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.MoreCollectionUtils;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "LORE", order = 300)
public final class LoreModifier extends ModifierData {

	public static final LoreModifier TEMPLATE = new LoreModifier();

	public static final String LORE_KEY = "lore";

	private List<String> lore;

	@Override
	public LoreModifier construct(ItemStack item) {
		LoreModifier modifier = new LoreModifier();
		modifier.lore = ItemUtils.getLore(item);
		return modifier;
	}

	@Override
	public boolean conforms(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return false;
		}
		if (hasLore() != meta.hasLore()) {
			return false;
		}
		if (hasLore() && !getLore().equals(meta.getLore())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isBroken() {
		return false;
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		if (this.lore == null) {
			nbt.remove(LORE_KEY);
		}
		else {
			nbt.setStringArray(LORE_KEY, this.lore.toArray(new String[0]));
		}
	}

	@Nonnull
	public static LoreModifier fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new LoreModifier();
		modifier.setLore(MoreCollectionUtils.collect(ArrayList::new, nbt.getStringArray(LORE_KEY)));
		return modifier;
	}

	@Override
	public List<String> getDisplayInfo() {
		return this.lore.stream()
				.map(line -> "" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + line)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public String toString() {
		return getSlug() +
				"{" +
				"lore=" + getLore() +
				"}";
	}

	// ------------------------------------------------------------
	// Commands
	// ------------------------------------------------------------

	@Subcommand("ignorelore")
	@Description("Removes the lore modifier.")
	public void commandIgnoreLore(Player player) {
		try (ModifierHandler<LoreModifier> handler = new ModifierHandler<>(player, this)) {
			handler.setModifier(null);
			handler.relay(ChatColor.GREEN + "Now ignoring lore.");
		}
	}

	@Subcommand("lore")
	@Description("Sets or resets the item's lore.")
	@Syntax("[...lore]")
	public void commandSetLore(Player player, @Optional String value) {
		try (ModifierHandler<LoreModifier> handler = new ModifierHandler<>(player, this)) {
			LoreModifier modifier = handler.ensureModifier();
			if (Strings.isNullOrEmpty(value)) {
				modifier.setLore(null);
				handler.relay(ChatColor.GREEN + "Successfully removed lore.");
			}
			else {
				modifier.setLore(Arrays.stream(value.split(";"))
						.map(ChatUtils::parseColor)
						.collect(Collectors.toCollection(ArrayList::new)));
				handler.relay(ChatColor.GREEN + "Successfully changed lore.");
			}
		}
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public boolean hasLore() {
		return CollectionUtils.isNotEmpty(this.lore);
	}

	public List<String> getLore() {
		if (this.lore == null) {
			return Lists.newArrayList();
		}
		return this.lore;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

}
