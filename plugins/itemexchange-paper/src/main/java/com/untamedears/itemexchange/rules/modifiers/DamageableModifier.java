package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.ModifierHandler;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.MoreClassUtils;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "DAMAGE", order = 500)
public final class DamageableModifier extends ModifierData {

	public static final DamageableModifier TEMPLATE = new DamageableModifier();

	public static final String DAMAGE_KEY = "damage";

	private int damage;

	@Override
	public DamageableModifier construct(ItemStack item) {
		Damageable meta = ItemUtils.getDamageable(item);
		if (meta == null) {
			return null;
		}
		DamageableModifier modifier = new DamageableModifier();
		modifier.damage = meta.getDamage();
		return modifier;
	}

	@Override
	public boolean isBroken() {
		if (this.damage < 0) {
			switch (this.damage) {
				case ExchangeRule.ANY:
				case ExchangeRule.USED:
					break;
				default:
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean conforms(ItemStack item) {
		Damageable meta = MoreClassUtils.castOrNull(Damageable.class, item.getItemMeta());
		if (meta == null) {
			return false;
		}
		int itemDamage = meta.getDamage();
		if (this.damage == ExchangeRule.ANY) {
			return itemDamage >= 0;
		}
		if (this.damage == ExchangeRule.USED) {
			return itemDamage > 0;
		}
		if (this.damage >= ExchangeRule.NEW) {
			return itemDamage == this.damage;
		}
		return false;
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		nbt.setInt(DAMAGE_KEY, getDamage());
	}

	@Nonnull
	public static DamageableModifier fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new DamageableModifier();
		modifier.setDamage(nbt.getInt(DAMAGE_KEY));
		return modifier;
	}

	@Override
	public List<String> getDisplayInfo() {
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

	// ------------------------------------------------------------
	// Commands
	// ------------------------------------------------------------

	@Subcommand("durability|d|damage|dmg")
	@Description("Sets the durability of an exchange rule.")
	@Syntax("<durability>")
	public void commandSetDurability(Player player, @Optional @Single String value) {
		try (ModifierHandler<DamageableModifier> handler = new ModifierHandler<>(player, this)) {
			if (Strings.isNullOrEmpty(value)) {
				handler.setModifier(null);
				handler.relay(ChatColor.GREEN + "Now ignoring durability.");
				return;
			}
			DamageableModifier modifier = handler.ensureModifier();
			switch (value.toUpperCase()) {
				case "ANY":
				case "%":
				case "*": {
					modifier.setDamage(ExchangeRule.ANY);
					handler.relay(ChatColor.YELLOW + "Modifier will now accept any damage level.");
					break;
				}
				case "DAMAGED":
				case "USED": {
					modifier.setDamage(ExchangeRule.USED);
					handler.relay(ChatColor.YELLOW + "Modifier will only accept damaged items.");
					break;
				}
				default:
					short durability = ExchangeRule.ERROR;
					try {
						durability = Short.parseShort(value);
						if (durability < 0) {
							durability = ExchangeRule.ERROR;
						}
					}
					catch (NumberFormatException ignored) {
					}
					if (durability == ExchangeRule.ERROR) {
						throw new InvalidCommandArgument("Please enter a valid durability.");
					}
					modifier.setDamage(durability);
					handler.relay(ChatColor.YELLOW + "Successfully set a new damage level!");
					break;
			}
		}
	}

	@Override
	public String toString() {
		return getSlug() +
				"{" +
				"damage=" + getDamage() +
				"}";
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public int getDamage() {
		return this.damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

}
