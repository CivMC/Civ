package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public final class DebugWand extends BasicHack {

	private static final String PERMISSION = "simpleadmin.debugwand";
	private static final ItemStack WAND = new ItemStack(Material.BLAZE_ROD);

	static {
		ItemUtils.setDisplayName(WAND, ChatColor.GOLD + "Block Wand");
	}

	private final CommandManager commands;

	public DebugWand(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		this.commands = new CommandManager(plugin) {
			@Override
			public void registerCommands() {
				registerCommand(new WandCommand());
			}
		};
	}

	@Override
	public void onEnable() {
		super.onEnable();
		this.commands.init();
	}

	@Override
	public void onDisable() {
		this.commands.reset();
		super.onDisable();
	}

	@CommandPermission(PERMISSION)
	public static class WandCommand extends BaseCommand {
		@CommandAlias("debugwand|dbwand")
		@Description("Creates a wand for debugging")
		public void giveWand(final Player sender) {
			sender.getInventory().addItem(WAND.clone());
			sender.sendMessage(ChatColor.GREEN + "Wand created.");
		}
	}

	@EventHandler
	public void onWandUsageOnBlock(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (!player.hasPermission(PERMISSION)) {
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		final ItemStack held = event.getItem();
		if (!ItemUtils.isValidItem(held) || !ItemUtils.areItemsSimilar(held, WAND)) {
			return;
		}
		final Block block = Objects.requireNonNull(event.getClickedBlock());
		final Location blockLocation = block.getLocation();
		player.sendMessage(ChatColor.GREEN + "Debug start.");
		player.sendMessage(ChatColor.AQUA + "Material: " + ChatColor.WHITE + block.getType().name());
		player.sendMessage(ChatColor.AQUA + "Location: "
				+ ChatColor.RED + "x:" + blockLocation.getBlockX() + ChatColor.WHITE + ", "
				+ ChatColor.GREEN + "y:" + blockLocation.getBlockY() + ChatColor.WHITE + ", "
				+ ChatColor.BLUE + "z:" + blockLocation.getBlockZ());
		final BlockData data = block.getBlockData();
		player.sendMessage(ChatColor.AQUA + "Block Data: " + ChatColor.YELLOW + data.getClass().getName());
		player.sendMessage(data.toString());
		player.sendMessage(ChatColor.RED + "Data end.");
		event.setCancelled(true);
	}

	@EventHandler
	public void onWandUsageOnEntity(final PlayerInteractEntityEvent event) {
		final Player player = event.getPlayer();
		if (!player.hasPermission(PERMISSION)) {
			return;
		}
		final ItemStack held = player.getInventory().getItem(event.getHand());
		if (!ItemUtils.isValidItem(held) || !ItemUtils.areItemsSimilar(held, WAND)) {
			return;
		}
		final Entity entity = event.getRightClicked();
		final Location entityLocation = entity.getLocation();
		player.sendMessage(ChatColor.GREEN + "Debug start.");
		player.sendMessage(ChatColor.AQUA + "Entity: " + ChatColor.WHITE + entity.getType().name());
		player.sendMessage(ChatColor.AQUA + "Location: "
				+ ChatColor.RED + "x:" + entityLocation.getX() + ChatColor.WHITE + ", "
				+ ChatColor.GREEN + "y:" + entityLocation.getY() + ChatColor.WHITE + ", "
				+ ChatColor.BLUE + "z:" + entityLocation.getZ());
		player.sendMessage(ChatColor.AQUA + "Rotation: "
				+ ChatColor.YELLOW + "p:" + entityLocation.getPitch() + ", "
				+ ChatColor.GOLD + "y:" + entityLocation.getYaw());
		final CompoundTag nbt = new CompoundTag();
		((CraftEntity) entity).getHandle().save(nbt);
		nbt.remove("Pos"); // Remove redundant position
		nbt.remove("Rotation"); // Remove redundant rotation
		player.sendMessage(nbt.toString());
		player.sendMessage(ChatColor.RED + "Data end.");
		event.setCancelled(true);
	}

}
