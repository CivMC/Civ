package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.ModifierHandler;
import com.untamedears.itemexchange.utility.NBTEncodings;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.EnchantUtils;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.KeyedUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreMapUtils;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "ENCHANTS", order = 200)
public final class EnchantModifier extends ModifierData {

	public static final EnchantModifier TEMPLATE = new EnchantModifier();
	public static final Pattern SET_ENCHANT_PATTERN = Pattern.compile("^([+?\\-])([A-Za-z_]+)([\\d]*)$");

	public static final String REQUIRED_KEY = "required";
	public static final String EXCLUDED_KEY = "excluded";
	public static final String UNLISTED_KEY = "unlisted";

	private Map<Enchantment, Integer> requiredEnchants;
	private Set<Enchantment> excludedEnchants;
	private boolean allowUnlistedEnchants;

	@Override
	public EnchantModifier construct(ItemStack item) {
		EnchantModifier modifier = new EnchantModifier();
		modifier.requiredEnchants = item.getEnchantments();
		return modifier;
	}

	@Override
	public boolean isBroken() {
		for (Map.Entry<Enchantment, Integer> entry : getRequiredEnchants().entrySet()) {
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
		Map<Enchantment, Integer> enchants = item.getEnchantments();
		if (!Utilities.conformsRequiresEnchants(getRequiredEnchants(), enchants, isAllowingUnlistedEnchants())) {
			return false;
		}
		if (!Collections.disjoint(getExcludedEnchants(), enchants.keySet())) {
			return false;
		}
		return true;
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		nbt.set(REQUIRED_KEY, NBTEncodings.encodeLeveledEnchants(getRequiredEnchants()));
		nbt.setStringArray(EXCLUDED_KEY, getExcludedEnchants().stream()
				.map(KeyedUtils::getString)
				.filter(Objects::nonNull)
				.toArray(String[]::new));
		nbt.setBoolean(UNLISTED_KEY, isAllowingUnlistedEnchants());
	}

	@Nonnull
	public static EnchantModifier fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new EnchantModifier();
		modifier.setRequiredEnchants(NBTEncodings.decodeLeveledEnchants(nbt.getCompound(REQUIRED_KEY)));
		modifier.setExcludedEnchants(Arrays.stream(nbt.getStringArray(EXCLUDED_KEY))
				.map(EnchantUtils::getEnchantment)
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(HashSet::new)));
		modifier.setAllowUnlistedEnchants(nbt.getBoolean(UNLISTED_KEY));
		return modifier;
	}

	@Override
	public List<String> getDisplayInfo() {
		List<String> info = Lists.newArrayList();
		for (Map.Entry<Enchantment, Integer> requiredEnchant : getRequiredEnchants().entrySet()) {
			String name = EnchantUtils.getEnchantNiceName(requiredEnchant.getKey());
			if (requiredEnchant.getValue() == ExchangeRule.ANY) {
				info.add(ChatColor.AQUA + name);
			}
			else {
				info.add(ChatColor.AQUA + name + " " + requiredEnchant.getValue());
			}
		}
		for (Enchantment excludedEnchant : getExcludedEnchants()) {
			info.add(ChatColor.RED + "!" + EnchantUtils.getEnchantNiceName(excludedEnchant));
		}
		if (isAllowingUnlistedEnchants()) {
			info.add(ChatColor.GREEN + "Other enchantments allowed");
		}
		return info;
	}

	@Override
	public String toString() {
		return getSlug() +
				"{" +
				"required=" + Utilities.leveledEnchantsToString(getRequiredEnchants()) + "," +
				"excluded=" + Utilities.enchantsToString(getExcludedEnchants()) + "," +
				"allowingUnlisted=" + isAllowingUnlistedEnchants() +
				"}";
	}

	// ------------------------------------------------------------
	// Commands
	// ------------------------------------------------------------

	@Subcommand("ignoreenchantments|ignoreenchants")
	@Description("Allows items with unspecified enchantments to be bought and sold.")
	public void commandIgnoreEnchantments(Player player) {
		try (ModifierHandler<EnchantModifier> handler = new ModifierHandler<>(player, this)) {
			handler.setModifier(null);
			handler.relay(ChatColor.GREEN + "Now ignoring enchantments.");
		}
	}

	@Subcommand("allowenchantments|allowenchants")
	@Description("Allows items with unspecified enchantments to be bought and sold.")
	public void commandAllowUnlistedEnchantments(Player player) {
		try (ModifierHandler<EnchantModifier> handler = new ModifierHandler<>(player, this)) {
			handler.ensureModifier().setAllowUnlistedEnchants(true);
			handler.relay(ChatColor.GREEN + "Unlisted enchantments are now allowed.");
		}
	}

	@Subcommand("denyenchantments|denyenchants")
	@Description("Disallows items with unspecified enchantments to be bought and sold.")
	public void commandDisallowUnlistedEnchantments(Player player) {
		try (ModifierHandler<EnchantModifier> handler = new ModifierHandler<>(player, this)) {
			handler.ensureModifier().setAllowUnlistedEnchants(false);
			handler.relay(ChatColor.GREEN + "Unlisted enchantments are now denied.");
		}
	}

	@Subcommand("enchantment|enchant|e")
	@Description("Disallows items with unspecified enchantments to be bought and sold.")
	@Syntax("<+/?/-><enchantment>[level]")
	public void commandSetEnchantment(Player player, @Single String details) {
		try (ModifierHandler<EnchantModifier> handler = new ModifierHandler<>(player, this)) {
			EnchantModifier modifier = handler.ensureModifier();
			if (Strings.isNullOrEmpty(details)) {
				throw new InvalidCommandArgument("You must enter an enchantment.");
			}
			Matcher matcher = SET_ENCHANT_PATTERN.matcher(details);
			if (!matcher.matches()) {
				throw new InvalidCommandArgument("You must enter a valid instruction.");
			}
			Enchantment enchantment = EnchantUtils.getEnchantment(matcher.group(2));
			if (enchantment == null) {
				throw new InvalidCommandArgument("You must enter a valid enchantment.");
			}
			Map<Enchantment, Integer> required = modifier.getRequiredEnchants();
			Set<Enchantment> excluded = modifier.getExcludedEnchants();
			switch (matcher.group(1)) {
				case "+": {
					int level = ExchangeRule.ERROR;
					if (matcher.groupCount() < 3) {
						level = ExchangeRule.ANY;
					}
					else {
						try {
							level = Integer.parseInt(matcher.group(3));
						}
						catch (Exception ignored) {
						} // No need to error here because it'll error below
						if (level < enchantment.getStartLevel() || level > enchantment.getMaxLevel()) {
							throw new InvalidCommandArgument("You must enter a valid level.");
						}
					}
					required.put(enchantment, level);
					excluded.remove(enchantment);
					handler.relay(ChatColor.GREEN + "Successfully added required enchantment.");
					break;
				}
				case "-": {
					required.remove(enchantment);
					excluded.add(enchantment);
					handler.relay(ChatColor.GREEN + "Successfully added excluded enchantment.");
					break;
				}
				case "?": {
					required.remove(enchantment);
					excluded.remove(enchantment);
					handler.relay(ChatColor.GREEN + "Successfully removed rules relating to enchantment.");
					break;
				}
				default: {
					throw new InvalidCommandArgument("You entered an invalid instruction.");
				}
			}
			modifier.setRequiredEnchants(required);
			modifier.setExcludedEnchants(excluded);
		}
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public Map<Enchantment, Integer> getRequiredEnchants() {
		if (this.requiredEnchants == null) {
			return Maps.newHashMap();
		}
		return this.requiredEnchants;
	}

	public void setRequiredEnchants(Map<Enchantment, Integer> required) {
		this.requiredEnchants = required;
	}

	public Set<Enchantment> getExcludedEnchants() {
		if (this.excludedEnchants == null) {
			return Sets.newHashSet();
		}
		return this.excludedEnchants;
	}

	public void setExcludedEnchants(Set<Enchantment> excluded) {
		this.excludedEnchants = excluded;
	}

	public boolean isAllowingUnlistedEnchants() {
		return this.allowUnlistedEnchants;
	}

	public void setAllowUnlistedEnchants(boolean allowUnlistedEnchants) {
		this.allowUnlistedEnchants = allowUnlistedEnchants;
	}

}
