package com.github.igotyou.FactoryMod.compaction;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ShowCommandHelp;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

@CommandAlias("fmcompact")
@CommandPermission("cmc.debug")
public final class CompactionTestCommands extends BaseCommand {
	@Default
	@CatchUnknown
	private void showHelp() {
		throw new ShowCommandHelp();
	}

	@Subcommand("compact")
	@Description("Marks the item held in your main hand as compacted.")
	private void compactHeldItem(
		final @NotNull Player sender
	) {
		final ItemStack item = sender.getInventory().getItemInMainHand();
		if (ItemUtils.isEmptyItem(item)) {
			sender.sendMessage(Component.text(
					"You are not holding an item to compact!",
					NamedTextColor.YELLOW
			));
			return;
		}

		final ItemMeta meta = item.getItemMeta();
		if (Compaction.isCompacted(meta)) {
			sender.sendMessage(Component.text(
					"That item is already compacted!",
					NamedTextColor.YELLOW
			));
			return;
		}
		if (Compaction.hasCompactedLore(meta)) {
			sender.sendMessage(Component.text(
					"That is a legacy compacted item! Use '/compaction upgrade' instead!",
					NamedTextColor.YELLOW
			));
			return;
		}

		Compaction.markAsCompacted(meta);
		item.setItemMeta(meta);
	}

	@Subcommand("decompact")
	@Description("Removes the compacted marking on the item held in your main hand.")
	private void decompactHeldItem(
		final @NotNull Player sender
	) {
		final ItemStack item = sender.getInventory().getItemInMainHand();
		if (ItemUtils.isEmptyItem(item)) {
			sender.sendMessage(Component.text(
					"You are not holding an item to decompact!",
					NamedTextColor.YELLOW
			));
			return;
		}

		final ItemMeta meta = item.getItemMeta();
		if (Compaction.hasCompactedLore(meta)) {
			sender.sendMessage(Component.text(
					"That is a legacy compacted item! You must use '/compaction upgrade' first!",
					NamedTextColor.YELLOW
			));
			return;
		}
		if (!Compaction.isCompacted(meta)) {
			sender.sendMessage(Component.text(
					"That is not a compacted item!",
					NamedTextColor.YELLOW
			));
			return;
		}

		Compaction.removeCompactedMarking(meta);
		item.setItemMeta(meta);
	}

	private enum CompactedItemType { NEW, LEGACY }
	@Subcommand("give")
	@Description("Creates a new compacted item.")
	private void giveNewItem(
		final @NotNull Player sender,
		final @NotNull CompactedItemType type,
		final @NotNull @Default("DIAMOND") Material material,
		final @Default("1") int amount
	) {
		final ItemStack item = new ItemStack(material, amount);
		switch (type) {
			case NEW -> item.editMeta(Compaction::markAsCompacted);
			case LEGACY -> item.editMeta(Compaction::addLegacyCompactedLore);
		}
		sender.getInventory().addItem(item);
		sender.sendMessage(Component.text(
				"You've been given a compacted item!",
				NamedTextColor.GREEN
		));
	}

	@Subcommand("upgrade")
	@Description("Attempts to upgrade a legacy compacted item held in your main hand.")
	private void upgradeHeldLegacy(
		final @NotNull Player sender
	) {
		final ItemStack item = sender.getInventory().getItemInMainHand();
		switch (Compaction.attemptUpgrade(item)) {
			case SUCCESS -> sender.sendMessage(Component.text(
					"Successfully upgraded legacy compacted item!",
					NamedTextColor.GREEN
			));
			case EMPTY_ITEM -> sender.sendMessage(Component.text(
					"You are not holding an item to upgrade!",
					NamedTextColor.YELLOW
			));
			case ALREADY_COMPACTED -> sender.sendMessage(Component.text(
					"That item is already compacted!",
					NamedTextColor.YELLOW
			));
			case NOT_COMPACTED -> sender.sendMessage(Component.text(
					"That item is not a legacy compacted item!",
					NamedTextColor.YELLOW
			));
		}
	}

	@Subcommand("inventory")
	@Description("Shows you an example inventory window containing compacted items.")
	private void viewInventory(
			final @NotNull Player sender
	) {
		final Inventory inventory = Bukkit.createInventory(
				null,
				InventoryUtils.CHEST_3_ROWS,
				Component.text("Test Inventory")
		);

		inventory.addItem(new ItemStack(Material.STICK)); // Non-comapcted item

		{ // Compacted example
			final ItemStack item = new ItemStack(Material.DIAMOND);
			item.editMeta(Compaction::markAsCompacted);
			inventory.addItem(item);
		}

		sender.openInventory(inventory);
		sender.sendMessage(Component.text(
				"Opening example inventory!",
				NamedTextColor.GREEN
		));
	}

	@Subcommand("merchant")
	@Description("Shows you an example merchant window containing compacted items.")
	private void viewMerchantTest(
		final @NotNull Player sender
	) {
		final Merchant merchant = Bukkit.createMerchant(Component.text("Test Merchant"));

		final var recipes = new ArrayList<MerchantRecipe>();
		{ // Compacted result recipe
			final ItemStack result = new ItemStack(Material.DIAMOND);
			result.editMeta(Compaction::markAsCompacted);
			final var recipe = new MerchantRecipe(result, Short.MAX_VALUE);
			recipe.addIngredient(new ItemStack(Material.STICK));
			recipes.add(recipe);
		}
		{ // Compacted ingredient recipe
			final var recipe = new MerchantRecipe(new ItemStack(Material.DIAMOND), Short.MAX_VALUE);
			final ItemStack ingredient = new ItemStack(Material.STICK);
			ingredient.editMeta(Compaction::markAsCompacted);
			recipe.addIngredient(ingredient);
			recipes.add(recipe);
		}
		merchant.setRecipes(recipes);

		sender.openMerchant(merchant, true);
		sender.sendMessage(Component.text(
				"Opening example merchant window!",
				NamedTextColor.GREEN
		));
	}
}
