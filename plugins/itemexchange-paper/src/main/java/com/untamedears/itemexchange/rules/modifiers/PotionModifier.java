package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.NBTEncodings;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import vg.civcraft.mc.civmodcore.inventory.items.PotionUtils;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.MoreClassUtils;
import vg.civcraft.mc.civmodcore.utilities.NullUtils;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "POTION", order = 400)
public final class PotionModifier extends ModifierData {

	public static final PotionModifier TEMPLATE = new PotionModifier();

	public static final String BASE_KEY = "base";
	public static final String EFFECTS_KEY = "effects";

	private PotionData base;
	private List<PotionEffect> effects;

	@Override
	public PotionModifier construct(ItemStack item) {
		PotionMeta meta = MoreClassUtils.castOrNull(PotionMeta.class, item.getItemMeta());
		if (meta == null) {
			return null;
		}
		PotionModifier modifier = new PotionModifier();
		modifier.base = meta.getBasePotionData();
		modifier.effects = meta.getCustomEffects();
		return modifier;
	}

	@Override
	public boolean isBroken() {
		if (this.base == null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean conforms(ItemStack item) {
		PotionMeta meta = MoreClassUtils.castOrNull(PotionMeta.class, item.getItemMeta());
		if (meta == null) {
			return false;
		}
		if (!NullUtils.equalsNotNull(this.base, meta.getBasePotionData())) {
			return false;
		}
		List<PotionEffect> heldEffects = getEffects();
		List<PotionEffect> metaEffects = meta.getCustomEffects();
		if (metaEffects.size() != heldEffects.size()) {
			return false;
		}
		if (!metaEffects.containsAll(heldEffects)) {
			return false;
		}
		return true;
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		nbt.set(BASE_KEY, NBTEncodings.encodePotionData(this.base));
		nbt.setCompoundArray(EFFECTS_KEY, getEffects().stream()
				.map(NBTEncodings::encodePotionEffect)
				.toArray(NBTCompound[]::new));
	}

	@Nonnull
	public static PotionModifier fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new PotionModifier();
		modifier.setPotionData(NBTEncodings.decodePotionData(nbt.getCompound(BASE_KEY)));
		modifier.setEffects(Arrays.stream(nbt.getCompoundArray(EFFECTS_KEY))
				.map(NBTEncodings::decodePotionEffect)
				.collect(Collectors.toCollection(ArrayList::new)));
		return modifier;
	}

	@Override
	public String getDisplayListing() {
		String listing = getName();
		if (Strings.isNullOrEmpty(listing)) {
			return null;
		}
		return ChatColor.WHITE + listing;
	}

	@Override
	public List<String> getDisplayInfo() {
		return Collections.singletonList(ChatColor.AQUA + "Potion Name: " + ChatColor.WHITE + getName());
	}

	@Override
	public String toString() {
		return getSlug() +
				"{" +
				"base=" + Utilities.potionDataToString(getPotionData()) + "," +
				"effects=" + Utilities.potionEffectsToString(getEffects()) + "," +
				"}";
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public String getName() {
		if (this.base == null) {
			return null;
		}
		return PotionUtils.getPotionNiceName(this.base.getType());
	}

	public PotionData getPotionData() {
		if (this.base == null) {
			return new PotionData(PotionType.UNCRAFTABLE, false, false);
		}
		return this.base;
	}

	public void setPotionData(PotionData data) {
		this.base = data;
	}

	public List<PotionEffect> getEffects() {
		if (this.effects == null) {
			return Lists.newArrayList();
		}
		return this.effects;
	}

	public void setEffects(List<PotionEffect> effects) {
		this.effects = effects;
	}

}
