package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.NBTEncodings;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.collections4.MapUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import vg.civcraft.mc.civmodcore.inventory.items.EnchantUtils;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.MoreClassUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreMapUtils;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "BOOKCHANTS", order = 201)
public final class EnchantStorageModifier extends ModifierData {

	public static final EnchantStorageModifier TEMPLATE = new EnchantStorageModifier();

	public static final String ENCHANTS_KEY = "enchants";

	private Map<Enchantment, Integer> enchants;

	@Override
	public EnchantStorageModifier construct(ItemStack item) {
		EnchantmentStorageMeta meta = MoreClassUtils.castOrNull(EnchantmentStorageMeta.class, item.getItemMeta());
		if (meta == null) {
			return null;
		}
		EnchantStorageModifier modifier = new EnchantStorageModifier();
		modifier.enchants = meta.getStoredEnchants();
		return modifier;
	}

	@Override
	public boolean isBroken() {
		for (Map.Entry<Enchantment, Integer> entry : getEnchants().entrySet()) {
			if (!MoreMapUtils.validEntry(entry)) {
				return true;
			}
			if (entry.getValue() == ExchangeRule.ANY) {
				continue;
			}
			if (!EnchantUtils.isSafeEnchantment(entry.getKey(), entry.getValue())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean conforms(ItemStack item) {
		EnchantmentStorageMeta meta = MoreClassUtils.castOrNull(EnchantmentStorageMeta.class, item.getItemMeta());
		if (meta == null) {
			return false;
		}
		if (hasEnchants() != meta.hasStoredEnchants()) {
			return false;
		}
		if (hasEnchants() && !Utilities.conformsRequiresEnchants(this.enchants, meta.getStoredEnchants(), false)) {
			return false;
		}
		return true;
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		nbt.set(ENCHANTS_KEY, NBTEncodings.encodeLeveledEnchants(getEnchants()));
	}

	@Nonnull
	public static EnchantStorageModifier fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new EnchantStorageModifier();
		modifier.setEnchants(NBTEncodings.decodeLeveledEnchants(nbt.getCompound(ENCHANTS_KEY)));
		return modifier;
	}

	@Override
	public List<String> getDisplayInfo() {
		List<String> info = Lists.newArrayList();
		for (Map.Entry<Enchantment, Integer> entry : getEnchants().entrySet()) {
			String name = EnchantUtils.getEnchantNiceName(entry.getKey());
			if (entry.getValue() == ExchangeRule.ANY) {
				info.add(ChatColor.AQUA + name + " %");
			}
			else {
				info.add(ChatColor.AQUA + name + " " + entry.getValue());
			}
		}
		return info;
	}

	@Override
	public String toString() {
		return getSlug() +
				"{" +
				"enchants=" + Utilities.leveledEnchantsToString(getEnchants()) +
				"}";
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public boolean hasEnchants() {
		return MapUtils.isNotEmpty(this.enchants);
	}

	public Map<Enchantment, Integer> getEnchants() {
		if (this.enchants == null) {
			return Maps.newHashMap();
		}
		return this.enchants;
	}

	public void setEnchants(Map<Enchantment, Integer> enchants) {
		this.enchants = enchants;
	}

}
