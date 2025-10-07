package com.untamedears.itemexchange.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.events.BlockInventoryRequestEvent;
import com.untamedears.itemexchange.rules.ExchangeRule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.InventoryAccessor;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;

@CommandAlias(TestCommand.ALIAS)
@CommandPermission(TestCommand.PERMISSION)
public final class TestCommand extends BaseCommand {
	public static final String ALIAS = "ietest";
	public static final String PERMISSION = "itemexchange.test";

	@Subcommand("shop-setup-1")
	@Description("Sets up your and the shop's inventories where the shop cannot accept more items.")
	public void shopSetup1(
		final @NotNull Player player
	) {
		{ // Set up shop chest inventory
			final InventoryAccessor shopAccessor = getShop(player);
			if (shopAccessor == null) return;
			shopAccessor.editContents((contents) -> {
				InventoryUtils.fillContents(contents, (i) -> new ItemStack(Material.DIRT));
				{ // Input rule
					final Material material = Material.DIAMOND;
					final var rule = new ExchangeRule(ExchangeRule.Type.INPUT, new ItemStack(material));
					rule.setAmount(material.getMaxStackSize() * 2);
					contents[0] = rule.toItem();
				}
				{ // Output rule
					final var rule = new ExchangeRule(ExchangeRule.Type.OUTPUT, new ItemStack(Material.DIAMOND_PICKAXE));
					contents[1] = rule.toItem();
				}
				contents[2] = new ItemStack(Material.DIAMOND_PICKAXE);
				return true;
			});
		}
		{ // Set up player inventory
			final PlayerInventory playerInventory = player.getInventory();
			InventoryAccessor.playerStorage(playerInventory).editContents((contents) -> {
				InventoryUtils.clearContents(contents);
				contents[0] = new ItemStack(Material.DIAMOND, 64);
				contents[1] = new ItemStack(Material.DIAMOND, 64);
				return true;
			});
			playerInventory.setHeldItemSlot(0);
		}
	}

	@Subcommand("shop-setup-2")
	@Description("Sets up your and the shop's inventories where your inventory cannot accept more items.")
	public void shopSetup2(
		final @NotNull Player player
	) {
		{ // Set up shop chest inventory
			final InventoryAccessor shopAccessor = getShop(player);
			if (shopAccessor == null) return;
			shopAccessor.editContents((contents) -> {
				InventoryUtils.clearContents(contents);
				{ // Input rule
					final var rule = new ExchangeRule(ExchangeRule.Type.INPUT, new ItemStack(Material.DIAMOND));
					contents[0] = rule.toItem();
				}
				{ // Output rule
					final var rule = new ExchangeRule(ExchangeRule.Type.OUTPUT, new ItemStack(Material.DIAMOND_PICKAXE));
					rule.setAmount(4);
					contents[1] = rule.toItem();
				}
				contents[2] = new ItemStack(Material.DIAMOND_PICKAXE);
				contents[3] = new ItemStack(Material.DIAMOND_PICKAXE);
				contents[4] = new ItemStack(Material.DIAMOND_PICKAXE);
				contents[5] = new ItemStack(Material.DIAMOND_PICKAXE);
				return true;
			});
		}
		{ // Set up player inventory
			final PlayerInventory playerInventory = player.getInventory();
			InventoryAccessor.playerStorage(playerInventory).editContents((contents) -> {
				InventoryUtils.fillContents(contents, (i) -> new ItemStack(Material.DIRT));
				contents[0] = new ItemStack(Material.DIAMOND);
				return true;
			});
			playerInventory.setHeldItemSlot(0);
		}
	}

	@Subcommand("shop-setup-3")
	@Description("Spreads the shop's stock across multiple small-ish stacks, with a full player inventory.")
	public void shopSetup3(
		final @NotNull Player player
	) {
		{ // Set up shop chest inventory
			final InventoryAccessor shopAccessor = getShop(player);
			if (shopAccessor == null) return;
			shopAccessor.editContents((contents) -> {
				InventoryUtils.clearContents(contents);
				{ // Input rule
					final var rule = new ExchangeRule(ExchangeRule.Type.INPUT, new ItemStack(Material.DIAMOND));
					contents[0] = rule.toItem();
				}
				{ // Output rule
					final var rule = new ExchangeRule(ExchangeRule.Type.OUTPUT, new ItemStack(Material.IRON_INGOT));
					rule.setAmount(70);
					contents[1] = rule.toItem();
				}
				contents[2] = new ItemStack(Material.IRON_INGOT, 7);
				contents[3] = new ItemStack(Material.IRON_INGOT, 3);
				contents[4] = new ItemStack(Material.IRON_INGOT, 9);
				contents[5] = new ItemStack(Material.IRON_INGOT, 20);
				contents[6] = new ItemStack(Material.IRON_INGOT, 15);
				contents[7] = new ItemStack(Material.IRON_INGOT, 2);
				contents[8] = new ItemStack(Material.IRON_INGOT, 7);
				contents[9] = new ItemStack(Material.IRON_INGOT, 11);
				contents[10] = new ItemStack(Material.IRON_INGOT, 20);
				return true;
			});
		}
		{ // Set up player inventory
			final PlayerInventory playerInventory = player.getInventory();
			InventoryAccessor.playerStorage(playerInventory).editContents((contents) -> {
				InventoryUtils.fillContents(contents, (i) -> new ItemStack(Material.DIRT));
				contents[0] = new ItemStack(Material.DIAMOND);
				return true;
			});
			playerInventory.setHeldItemSlot(0);
		}
	}

	// ============================================================
	//
	// ============================================================

	private static @Nullable InventoryAccessor getShop(
		final @NotNull Player player
	) {
		Block shopBlock = null;
		for (final var ray = new BlockIterator(player, 6); ray.hasNext();) {
			final Block hit = ray.next();
			final Material hitMaterial = hit.getType();
			if (ItemExchangeConfig.hasCompatibleShopBlock(hitMaterial)) {
				shopBlock = hit;
				break;
			}
			if (hitMaterial.isOccluding()) {
				break;
			}
		}
		if (shopBlock == null) {
			player.sendMessage(Component.text("There's no ItemExchange compatible shop-block in-front of you.", NamedTextColor.RED));
			return null;
		}
		final Inventory shopInventory; {
			final var event = BlockInventoryRequestEvent.emit(
				shopBlock,
				player,
				BlockInventoryRequestEvent.Purpose.ACCESS
			);
			if (event.isCancelled()) {
				player.sendMessage(Component.text("That ItemExchange compatible shop-block has no retrievable inventory!", NamedTextColor.RED));
				return null;
			}
			shopInventory = event.getInventory();
			if (shopInventory == null) {
				player.sendMessage(Component.text("That ItemExchange compatible shop-block has a null inventory!", NamedTextColor.RED));
				return null;
			}
			if (shopInventory.getSize() < InventoryUtils.CHEST_1_ROW) {
				player.sendMessage(Component.text("That ItemExchange compatible shop-block is not large enough to be suitable for testing!", NamedTextColor.RED));
				return null;
			}
		}
		return InventoryAccessor.fullContents(shopInventory);
	}
}
