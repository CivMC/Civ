package com.untamedears.ItemExchange.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.untamedears.ItemExchange.utility.ExchangeRule;
import org.bukkit.potion.PotionType;

public class PotionMetadata implements AdditionalMetadata {

	private String name = "Uncraftable Potion";
	private PotionData base = new PotionData(PotionType.UNCRAFTABLE, false, false);
	private List<PotionEffect> effects = new ArrayList<>();

	private PotionMetadata() {}
	
	public PotionMetadata(PotionMeta meta) {
		this.base = meta.getBasePotionData();
		this.effects.addAll(getCustomEffects(meta));
		if (meta.hasDisplayName()) {
			this.name = meta.getDisplayName();
		}
		else if (this.base != null) {
			boolean isUpgraded = this.base.isUpgraded();
			switch (this.base.getType()) {
				default:
				case UNCRAFTABLE:
					this.name = "Uncraftable Potion";
					break;
				case WATER:
					this.name = "Water Bottle";
					break;
				case MUNDANE:
					this.name = "Mundane Potion";
					break;
				case THICK:
					this.name = "Thick Potion";
					break;
				case AWKWARD:
					this.name = "Awkward Potion";
					break;
				case NIGHT_VISION:
					this.name = "Potion of Night Vision";
					break;
				case INVISIBILITY:
					this.name = "Potion of Invisibility";
					break;
				case JUMP:
					this.name = isUpgraded ? "Potion of Leaping II" : "Potion of Leaping";
					break;
				case FIRE_RESISTANCE:
					this.name = "Potion of Fire Resistance";
					break;
				case SPEED:
					this.name = isUpgraded ? "Potion of Swiftness II" : "Potion of Swiftness";
					break;
				case SLOWNESS:
					this.name = "Potion of Slowness";
					break;
				case WATER_BREATHING:
					this.name = "Potion of Water Breathing";
					break;
				case INSTANT_HEAL:
					this.name = isUpgraded ? "Potion of Healing II" : "Potion of Healing";
					break;
				case INSTANT_DAMAGE:
					this.name = isUpgraded ? "Potion of Harming II" : "Potion of Harming";
					break;
				case POISON:
					this.name = isUpgraded ? "Potion of Poison II" : "Potion of Poison";
					break;
				case REGEN:
					this.name = isUpgraded ? "Potion of Regeneration II" : "Potion of Regeneration";
					break;
				case STRENGTH:
					this.name = isUpgraded ? "Potion of Strength II" : "Potion of Strength";
					break;
				case WEAKNESS:
					this.name = "Potion of Weakness";
					break;
				case LUCK:
					this.name = "Potion of Luck";
					break;
			}
		}
	}
	
	@Override
	public String serialize() {
		StringBuilder serialized = new StringBuilder();
		// Serialise the potion name
		serialized.append(this.name);
		serialized.append(ExchangeRule.secondarySpacer);
		// Serialise the base potion
		if (this.base == null) {
			serialized.append(0);	// Base Potion ID
			serialized.append(ExchangeRule.tertiarySpacer);
			serialized.append(0);	// Extended? 1 == TRUE
			serialized.append(ExchangeRule.tertiarySpacer);
			serialized.append(0);	// Upgraded? 1 == TRUE
		}
		else {
			switch (this.base.getType()) {
				case UNCRAFTABLE:
					serialized.append(0);
					break;
				case WATER:
					serialized.append(-1);
					break;
				case MUNDANE:
					serialized.append(-2);
					break;
				case THICK:
					serialized.append(-3);
					break;
				case AWKWARD:
					serialized.append(-4);
					break;
				default:
					serialized.append(this.base.getType().getEffectType().getId());
					break;
			}
			serialized.append(ExchangeRule.tertiarySpacer);
			serialized.append(this.base.isExtended() ? 1 : 0);
			serialized.append(ExchangeRule.tertiarySpacer);
			serialized.append(this.base.isUpgraded() ? 1 : 0);
		}
		serialized.append(ExchangeRule.secondarySpacer);
		// Serialise addition effects
		Iterator<PotionEffect> iterator = this.effects.iterator();
		while(iterator.hasNext()) {
			PotionEffect effect = iterator.next();
			serialized.append(effect.getType().getId());
			serialized.append(ExchangeRule.tertiarySpacer);
			serialized.append(effect.getAmplifier());
			serialized.append(ExchangeRule.tertiarySpacer);
			serialized.append(effect.getDuration());
			serialized.append(ExchangeRule.tertiarySpacer);
			serialized.append(effect.isAmbient() ? 1 : 0);
			if (iterator.hasNext()) {
				serialized.append(ExchangeRule.secondarySpacer);
			}
		}
		return serialized.toString();
	}

	public static PotionMetadata deserialize(String lore) {
		PotionMetadata metadata = new PotionMetadata();
		String[] data = lore.split(ExchangeRule.secondarySpacer);
		for (int i = 0; i < data.length; i++) {
			// Parse the potion name
			if (i == 0) {
				metadata.name = data[i];
			}
			// Parse the base effect
			else if (i == 1) {
				try {
					String[] effectData = data[i].split(ExchangeRule.tertiarySpacer);
					int effectId = Integer.parseInt(effectData[0]);
					boolean isExtended = Integer.parseInt(effectData[1]) == 1;
					boolean isUpgraded = Integer.parseInt(effectData[2]) == 1;
					switch (effectId) {
						case 0:
							metadata.base = new PotionData(PotionType.UNCRAFTABLE, isExtended, isUpgraded);
							break;
						case -1:
							metadata.base = new PotionData(PotionType.WATER, isExtended, isUpgraded);
							break;
						case -2:
							metadata.base = new PotionData(PotionType.MUNDANE, isExtended, isUpgraded);
							break;
						case -3:
							metadata.base = new PotionData(PotionType.THICK, isExtended, isUpgraded);
							break;
						case -4:
							metadata.base = new PotionData(PotionType.AWKWARD, isExtended, isUpgraded);
							break;
						default:
							PotionEffectType effectType = PotionEffectType.getById(effectId);
							PotionType potionType = PotionType.getByEffect(effectType);
							metadata.base = new PotionData(potionType, isExtended, isUpgraded);
							break;
					}
				}
				catch (Exception error) {
					metadata.base = new PotionData(PotionType.WATER, false, false);
					ItemExchangePlugin.instance.getLogger().log(Level.WARNING, "An error occurred parsing ItemExchange potion metadata: base effect. Defaulting to a water bottle.", error);
				}
			}
			// Parse any other effect
			else {
				try {
					String[] effectData = data[i].split(ExchangeRule.tertiarySpacer);
					int effectId = Integer.parseInt(effectData[0]);
					int amplifier = Integer.parseInt(effectData[1]);
					int duration = Integer.parseInt(effectData[2]);
					boolean ambient = Integer.parseInt(effectData[3]) == 1;
					PotionEffectType type = PotionEffectType.getById(effectId);
					metadata.effects.add(new PotionEffect(type, duration, amplifier, ambient));
				}
				catch (Exception error) {
					ItemExchangePlugin.instance.getLogger().log(Level.WARNING, "An error occurred parsing ItemExchange potion metadata: custom effect. Skipping.", error);
				}
			}
		}
		return metadata;
	}

	@Override
	public boolean matches(ItemStack item) {
		if (!item.hasItemMeta()) {
			return false;
		}
		ItemMeta itemMeta = item.getItemMeta();
		if (!(itemMeta instanceof PotionMeta)) {
			return false;
		}
		PotionMeta potionMeta = (PotionMeta) itemMeta;
		// Check if the base effect is the same
		if (!Objects.equals(this.base, potionMeta.getBasePotionData())) {
			return false;
		}
		// Check if custom effects were the same
		List<PotionEffect> effects = getCustomEffects(potionMeta);
		if (this.effects.size() != effects.size()) {
			return false;
		}
		if (!this.effects.containsAll(effects)) {
			return false;
		}
		// Done, it's practically the same
		return true;
	}

	@Override
	public String getDisplayedInfo() {
		return ChatColor.AQUA + "Potion Name: " + ChatColor.WHITE + this.name;
	}

	protected static List<PotionEffect> getCustomEffects(PotionMeta meta) {
		if (meta == null || !meta.hasCustomEffects()) {
			return new ArrayList<>();
		}
		List<PotionEffect> effects = new ArrayList<>();
		for (PotionEffect effect : meta.getCustomEffects()) {
			/**
			 * Brewery uses the duration of Regeneration as a custom effect as a
			 * unique identifier, so this effect must be removed otherwise brews
			 * that are identical in every other way and even come from the same
			 * batch will not match, so you could only sell one brew per rule.
			 * @see <a href="https://github.com/DevotedMC/Brewery/blob/master/src/com/dre/brewery/Brew.java#L104">Brewery's code</a>
			 * */
			if (effect.getType().equals(PotionEffectType.REGENERATION)) {
				if (effect.getDuration() < -1) {
					continue;
				}
			}
			// Otherwise add the effect
			effects.add(effect);
		}
		return effects;
	}

}
