package com.untamedears.itemexchange.rules.modifiers;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.castOrNull;

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
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import vg.civcraft.mc.civmodcore.api.EnchantAPI;
import vg.civcraft.mc.civmodcore.api.EnchantNames;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.Iteration;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "BOOKCHANTS", order = 201)
public final class EnchantStorageModifier extends ModifierData {

	public static final EnchantStorageModifier TEMPLATE = new EnchantStorageModifier();

	public static final String ENCHANTS_KEY = "enchants";

	private Map<Enchantment, Integer> enchants;

	@Override
	public EnchantStorageModifier construct(ItemStack item) {
		EnchantmentStorageMeta meta = castOrNull(EnchantmentStorageMeta.class, item.getItemMeta());
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
			if (!Iteration.validEntry(entry)) {
				return true;
			}
			if (entry.getValue() == ExchangeRule.ANY) {
				continue;
			}
			if (!EnchantAPI.isSafeEnchantment(entry.getKey(), entry.getValue())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean conforms(ItemStack item) {
		EnchantmentStorageMeta meta = castOrNull(EnchantmentStorageMeta.class, item.getItemMeta());
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
	public void serialize(NBTCompound nbt) {
		nbt.setCompound(ENCHANTS_KEY, NBTEncodings.encodeLeveledEnchants(getEnchants()));
	}

	@Override
	public void deserialize(NBTCompound nbt) {
		setEnchants(NBTEncodings.decodeLeveledEnchants(nbt.getCompound(ENCHANTS_KEY)));
	}

	@Override
	public List<String> getDisplayInfo() {
		List<String> info = Lists.newArrayList();
		for (Map.Entry<Enchantment, Integer> entry : getEnchants().entrySet()) {
			EnchantNames.SearchResult result = EnchantNames.findByEnchantment(entry.getKey());
			if (entry.getValue() == ExchangeRule.ANY) {
				info.add(ChatColor.AQUA + result.getDisplayName() + " %");
			}
			else {
				info.add(ChatColor.AQUA + result.getDisplayName() + " " + entry.getValue());
			}
		}
		return info;
	}

	@Override
	public String toString() {
		return getSlug() +
				"{" +
				"enchants={" + getEnchants() + "}" +
				"}";
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public boolean hasEnchants() {
		return !Iteration.isNullOrEmpty(this.enchants);
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
