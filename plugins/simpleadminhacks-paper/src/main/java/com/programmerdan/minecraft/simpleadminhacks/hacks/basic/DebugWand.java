package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;
import vg.civcraft.mc.civmodcore.custom.items.CustomItems;
import vg.civcraft.mc.civmodcore.custom.items.NBTCriteria;

public class DebugWand extends BasicHack {

	private static final String PERMISSION = "simpleadmin.debugwand";

	private static final NamespacedKey CUSTOM_ITEM_KEY;
	private static final NBTCriteria CUSTOM_ITEM_CRITERIA;
	private static final ItemStack CUSTOM_ITEM_TEMPLATE;

	static {
		CUSTOM_ITEM_KEY = new NamespacedKey(SimpleAdminHacks.instance(), "db_wand");
		CUSTOM_ITEM_CRITERIA = new NBTCriteria(CUSTOM_ITEM_KEY, ChatColor.GOLD + "Block Wand");
		CUSTOM_ITEM_TEMPLATE = CUSTOM_ITEM_CRITERIA.applyToItem(new ItemStack(Material.BLAZE_ROD));
	}

	private AikarCommandManager commands;

	public DebugWand(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void dataBootstrap() {
		CustomItems.register(CUSTOM_ITEM_CRITERIA);
	}

	@Override
	public void dataCleanup() {
		CustomItems.deregister(CUSTOM_ITEM_KEY);
	}

	@Override
	public void registerCommands() {
		this.commands = new AikarCommandManager(plugin()) {
			@Override
			public void registerCommands() {
				registerCommand(new WandCommand());
			}
		};
	}

	@Override
	public void unregisterCommands() {
		if (this.commands != null) {
			this.commands.reset();
			this.commands = null;
		}
	}

	@Override
	public String status() {
		return DebugWand.class.getSimpleName() + " is " + (isEnabled() ? "enabled" : "disabled") + ".";
	}

	@CommandPermission(PERMISSION)
	public static class WandCommand extends AikarCommand {

		@CommandAlias("dbwand")
		@Description("Creates a wand for debugging")
		public void giveWand(Player sender) {
			sender.getInventory().addItem(CUSTOM_ITEM_TEMPLATE.clone());
			sender.sendMessage(ChatColor.GREEN + "Wand created.");
		}

	}

	@EventHandler
	public void onWandUsageOnBlock(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPermission(PERMISSION)) {
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		ItemStack held = event.getItem();
		if (!ItemAPI.isValidItem(held)
				|| !ItemAPI.areItemsSimilar(held, CUSTOM_ITEM_TEMPLATE)
				|| !CUSTOM_ITEM_CRITERIA.matches(held)) {
			return;
		}
		Block block = Objects.requireNonNull(event.getClickedBlock());
		player.sendMessage(ChatColor.AQUA + "Material: " + ChatColor.WHITE + block.getType().name());
		player.sendMessage(ChatColor.AQUA + "Location: "
				+ ChatColor.RED + block.getLocation().getBlockX() + ChatColor.WHITE + ","
				+ ChatColor.GREEN + block.getLocation().getBlockY() + ChatColor.WHITE + ","
				+ ChatColor.BLUE + block.getLocation().getBlockZ());
		BlockData data = block.getBlockData();
		player.sendMessage(ChatColor.AQUA + "Block Data: " + ChatColor.YELLOW + data.getClass().getName());
		player.sendMessage(data.toString());
		player.sendMessage(ChatColor.RED + "Data end.");
		event.setCancelled(true);
	}

	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

}
