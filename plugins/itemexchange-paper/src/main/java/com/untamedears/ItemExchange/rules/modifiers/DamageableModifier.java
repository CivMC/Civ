package com.untamedears.itemexchange.rules.modifiers;

import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public final class DamageableModifier extends ModifierData {

	public static final String SLUG = "DAMAGE";

	public DamageableModifier() {
		super(SLUG, 100);
	}

	public static ModifierData fromItem(ItemStack item) {
		if (ItemExchangePlugin.IS_DAMAGEABLE.contains(item.getType())) {
			DamageableModifier modifier = new DamageableModifier();
			modifier.trace(item);
			return modifier;
		}
		return null;
	}

	@Override
	public boolean isValid() {
		return !this.nbt.isEmpty();
	}

	@Override
	public void trace(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta instanceof Damageable) {
			setDamage(((Damageable) meta).getDamage());
		}
	}

	@Override
	public boolean conforms(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (!(meta instanceof Damageable)) {
			return false;
		}
		int ruleDamage = getDamage();
		int itemDamage = ((Damageable) meta).getDamage();
		if (ruleDamage == ExchangeRule.ANY) {
			return itemDamage >= 0;
		}
		else if (ruleDamage == ExchangeRule.USED) {
			return itemDamage > 0;
		}
		else if (ruleDamage >= ExchangeRule.NEW) {
			return itemDamage == ruleDamage;
		}
		else {
			return false;
		}
	}

	@Override
	public List<String> getDisplayedInfo() {
		int ruleDamage = getDamage();
		switch (ruleDamage) {
			case ExchangeRule.ANY:
				return Collections.singletonList(ChatColor.GOLD + "Condition: Any");
			case ExchangeRule.NEW:
				return Collections.singletonList(ChatColor.GOLD + "Condition: Undamaged");
			default:
			case ExchangeRule.USED:
				return Collections.singletonList(ChatColor.GOLD + "Condition: Damaged");
		}
	}

	public int getDamage() {
		return this.nbt.getInteger("damage");
	}

	public void setDamage(int damage) {
		this.nbt.setInteger("damage", damage);
	}

}
