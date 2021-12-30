package com.untamedears.itemexchange.utility;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import vg.civcraft.mc.civmodcore.inventory.items.EnchantUtils;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.KeyedUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreMapUtils;

public final class NBTEncodings {

	private static final String TYPE_KEY = "type";
	private static final String EXTENDED_KEY = "extended";
	private static final String UPGRADED_KEY = "upgraded";
	private static final String DURATION_KEY = "duration";
	private static final String AMPLIFIER_KEY = "amplifier";
	private static final String AMBIENT_KEY = "ambient";
	private static final String PARTICLES_KEY = "particles";

	// ------------------------------------------------------------
	// Leveled Enchantments
	// ------------------------------------------------------------

	public static NBTCompound encodeLeveledEnchants(Map<Enchantment, Integer> enchants) {
		NBTCompound nbt = new NBTCompound();
		if (enchants == null) {
			return nbt;
		}
		for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
			if (!MoreMapUtils.validEntry(entry)) {
				continue;
			}
			nbt.setInt(KeyedUtils.getString(entry.getKey()), entry.getValue());
		}
		return nbt;
	}

	public static Map<Enchantment, Integer> decodeLeveledEnchants(NBTCompound nbt) {
		Map<Enchantment, Integer> enchants = Maps.newHashMap();
		if (nbt == null) {
			return enchants;
		}
		for (String slug : nbt.getKeys()) {
			Enchantment enchantment = EnchantUtils.getEnchantment(slug);
			if (enchantment == null) {
				continue;
			}
			enchants.put(enchantment, nbt.getInt(slug));
		}
		return enchants;
	}

	// ------------------------------------------------------------
	// Potion Data
	// ------------------------------------------------------------

	public static NBTCompound encodePotionData(PotionData data) {
		NBTCompound nbt = new NBTCompound();
		if (data == null) {
			return nbt;
		}
		nbt.setString(TYPE_KEY, data.getType().name());
		nbt.setBoolean(EXTENDED_KEY, data.isExtended());
		nbt.setBoolean(UPGRADED_KEY, data.isUpgraded());
		return nbt;
	}

	public static PotionData decodePotionData(NBTCompound nbt) {
		if (nbt == null) {
			return null;
		}
		return new PotionData(
				EnumUtils.getEnum(PotionType.class, nbt.getString(TYPE_KEY)),
				nbt.getBoolean(EXTENDED_KEY),
				nbt.getBoolean(UPGRADED_KEY));
	}

	// ------------------------------------------------------------
	// Potion Effect
	// ------------------------------------------------------------

	public static NBTCompound encodePotionEffect(PotionEffect effect) {
		NBTCompound nbt = new NBTCompound();
		if (effect == null) {
			return nbt;
		}
		nbt.setString(TYPE_KEY, effect.getType().getName());
		nbt.setInt(DURATION_KEY, effect.getDuration());
		nbt.setInt(AMPLIFIER_KEY, effect.getAmplifier());
		nbt.setBoolean(AMBIENT_KEY, effect.isAmbient());
		nbt.setBoolean(PARTICLES_KEY, effect.hasParticles());
		return nbt;
	}

	public static PotionEffect decodePotionEffect(NBTCompound nbt) {
		if (nbt == null) {
			return null;
		}
		return new PotionEffect(
				Objects.requireNonNull(PotionEffectType.getByName(nbt.getString(TYPE_KEY))),
				nbt.getInt(DURATION_KEY),
				nbt.getInt(AMPLIFIER_KEY),
				nbt.getBoolean(AMBIENT_KEY),
				nbt.getBoolean(PARTICLES_KEY));
	}

}
