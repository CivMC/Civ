package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.ModifierHandler;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.TextUtil;

@Modifier(slug = "LORE", order = 30)
public class LoreModifier extends ModifierData<LoreModifier> {

	public static final String LORE_KEY = "lore";

	private List<String> lore;

	@Override
	public LoreModifier construct() {
		return new LoreModifier();
	}

	@Override
	public LoreModifier construct(@NotNull ItemStack item) {
		LoreModifier modifier = new LoreModifier();
		modifier.lore = ItemAPI.getLore(item);
		return modifier;
	}

	@Override
	public boolean conforms(@NotNull ItemStack item) {
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
	public void serialize(NBTCompound nbt) {
		nbt.setStringArray(LORE_KEY, getLore().toArray(new String[0]));
	}

	@Override
	public void deserialize(NBTCompound nbt) {
		setLore(Arrays.asList(nbt.getStringArray(LORE_KEY)));
	}

	@Override
	public List<String> getDisplayedInfo() {
		return this.lore;
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
						.map(TextUtil::parse)
						.collect(Collectors.toList()));
				handler.relay(ChatColor.GREEN + "Successfully changed lore.");
			}
		}
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public boolean hasLore() {
		return !Iteration.isNullOrEmpty(this.lore);
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
