package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ShowCommandHelp;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

@CommandAlias("fmtest")
@CommandPermission("cmc.debug")
public final class TestCommand extends BaseCommand {
	@Default
	@CatchUnknown
	public void showHelp() {
		throw new ShowCommandHelp();
	}

	/**
	 * Use this when you want to test the factory making process instead of just using /fmc
	 */
	@Subcommand("give factory")
	@Description("Gives the items necessary to create a particular factory.")
	public void giveFactoryItems(
			final @NotNull Player sender,
			final @NotNull IFactoryEgg factory
	) {
		final List<ItemStack> items;
		if (factory instanceof final FurnCraftChestEgg fccFactory) {
			final ItemMap setupCost = fccFactory.getSetupCost();
			if (setupCost == null) {
				sender.sendMessage(Component.text(
						"Factory [" + factory.getName() + "] does not have any setup cost. It's probably created through a recipe instead!",
						NamedTextColor.RED
				));
				return;
			}
			items = setupCost.getItemStackRepresentation();
		}
		else {
			sender.sendMessage(Component.text(
					"Cannot give setup items for factory [" + factory.getName() + "]!",
					NamedTextColor.RED
			));
			return;
		}

		if (!InventoryUtils.safelyAddItemsToInventory(sender.getInventory(), items.toArray(ItemStack[]::new))) {
			sender.sendMessage(Component.text(
					"Wasn't able to add items to your inventory!",
					NamedTextColor.RED
			));
			return;
		}

		sender.sendMessage(Component.text(
				"Successfully created setup items for factory [" + factory.getName() + "]!",
				NamedTextColor.GREEN
		));
	}

	public enum RecipeItems { INGREDIENTS, RESULT }
	@Subcommand("give recipe")
	@Description("Gives the items that a particular recipe takes as ingredients, or produces as a result.")
	public void giveRecipeItems(
			final @NotNull Player sender,
			final @NotNull RecipeItems category,
			final @NotNull IRecipe recipe
	) {
		final List<ItemStack> items;
		switch (category) {
			case INGREDIENTS -> {
				if (!(recipe instanceof final InputRecipe inputRecipe)) {
					sender.sendMessage(Component.text(
							"The recipe [" + recipe.getIdentifier() + "] does not accept ingredients!",
							NamedTextColor.RED
					));
					return;
				}
				items = inputRecipe.getInput().getItemStackRepresentation();
			}
			case RESULT -> {
				if (!(recipe instanceof final ProductionRecipe productionRecipe)) {
					sender.sendMessage(Component.text(
							"The recipe [" + recipe.getIdentifier() + "] does not produce constant outputs!",
							NamedTextColor.RED
					));
					return;
				}
				items = productionRecipe.getOutput().getItemStackRepresentation();
			}
			default -> throw new IllegalArgumentException("Unknown recipe items:" + category.name());
		}

		if (!InventoryUtils.safelyAddItemsToInventory(sender.getInventory(), items.toArray(ItemStack[]::new))) {
			sender.sendMessage(Component.text(
					"Wasn't able to add items to your inventory!",
					NamedTextColor.RED
			));
			return;
		}

		sender.sendMessage(Component.text(
				"Successfully created items for recipe [" + recipe.getIdentifier() + "]!",
				NamedTextColor.GREEN
		));
	}
}
