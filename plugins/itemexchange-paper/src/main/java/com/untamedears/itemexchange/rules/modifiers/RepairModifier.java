package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.ModifierHandler;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.MoreClassUtils;

/**
 * <p>This additional represents a repair level condition.</p>
 *
 * <ul>
 *     <li>Positive integers and zero mean that repair level specifically, eg: RepairCost == 15</li>
 *     <li>Negative integers mean that that repair level or lower, eg: RepairCost &lt;= 15</li>
 * </ul>
 */
@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "REPAIR", order = 600)
public final class RepairModifier extends ModifierData {

	public static final RepairModifier TEMPLATE = new RepairModifier();

	public static final String LEVEL_KEY = "repairLevel";

	private int level;

	@Override
	public RepairModifier construct(ItemStack item) {
		if (!ItemExchangeConfig.canRepairItem(item.getType())) {
			return null;
		}
		Repairable meta = MoreClassUtils.castOrNull(Repairable.class, item.getItemMeta());
		if (meta == null) {
			return null;
		}
		RepairModifier modifier = new RepairModifier();
		modifier.level = meta.getRepairCost();
		return modifier;
	}

	@Override
	public boolean isBroken() {
		return false;
	}

	@Override
	public boolean conforms(ItemStack item) {
		Repairable meta = MoreClassUtils.castOrNull(Repairable.class, item.getItemMeta());
		if (meta == null) {
			return false;
		}
		int itemRepair = meta.getRepairCost();
		if (this.level < 0) {
			return itemRepair <= this.level * -1;
		}
		else {
			return itemRepair == this.level;
		}
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		nbt.setInt(LEVEL_KEY, getRepairCost());
	}

	@Nonnull
	public static RepairModifier fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new RepairModifier();
		modifier.setRepairCost(nbt.getInt(LEVEL_KEY));
		return modifier;
	}

	@Override
	public List<String> getDisplayInfo() {
		List<String> info = Lists.newArrayList();
		int repairCost = getRepairCost();
		if (repairCost == 0) {
			info.add(ChatColor.GOLD + "Never repaired");
		}
		else if (repairCost < 0) {
			info.add(ChatColor.GOLD + "Repair level " + (repairCost * -1 + 2) + " or less");
		}
		else {
			info.add(ChatColor.GOLD + "Repair level " + (repairCost + 2));
		}
		return info;
	}

	@Override
	public String toString() {
		return getSlug() +
				"{" +
				"level=" + getRepairCost() +
				"}";
	}

	// ------------------------------------------------------------
	// Commands
	// ------------------------------------------------------------

	@Subcommand("repair|repairlevel")
	@Description("Sets or resets the exchange's repair level.")
	@Syntax("[repair level]")
	public void commandSetRepairLevel(Player player, @Optional @Single String value) {
		try (ModifierHandler<RepairModifier> handler = new ModifierHandler<>(player, this)) {
			if (Strings.isNullOrEmpty(value)) {
				handler.setModifier(null);
				handler.relay(ChatColor.GREEN + "Successfully removed repair level condition.");
				return;
			}
			RepairModifier modifier = handler.ensureModifier();
			if (value.startsWith("@")) {
				int repairLevel = ExchangeRule.ERROR;
				try {
					repairLevel = Integer.parseInt(value.substring(1));
				}
				catch (final NumberFormatException ignored) { }
				if (repairLevel < 2) {
					throw new InvalidCommandArgument("You must enter a valid value, e.g: @9");
				}
				modifier.setRepairCost(repairLevel - 2);
			}
			else if (StringUtils.equalsIgnoreCase(value, "NEW") || StringUtils.equalsIgnoreCase(value, "MINT")) {
				modifier.setRepairCost(0);
			}
			else {
				int repairLevel = ExchangeRule.ERROR;
				try {
					repairLevel = Integer.parseInt(value);
				}
				catch (final NumberFormatException ignored) { }
				if (repairLevel < 2) {
					throw new InvalidCommandArgument("You must enter a valid value, e.g: 9");
				}
				modifier.setRepairCost((repairLevel - 2) * -1);
			}
			handler.relay(ChatColor.GREEN + "Successfully changed repair level condition.");
		}
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public int getRepairCost() {
		return this.level;
	}

	public void setRepairCost(int repairLevel) {
		this.level = repairLevel;
	}

}
