package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.TeleportUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.util.DoubleInteractFixer;

public class GoldBlockElevators extends BasicHack {

	private BooleanSetting useJumpSneakTP;
	private DoubleInteractFixer interactFixer;

	public GoldBlockElevators(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
		this.interactFixer = new DoubleInteractFixer(plugin);
		initSettings();
	}

	private void initSettings() {
		MenuSection mainMenu = plugin.getSettingManager().getMainMenu();
		this.useJumpSneakTP =
				new BooleanSetting(plugin, true, "Use Jump or Sneak to teleport on gold blocks", "jumpOrSneakTp",
						"When true, jumping or sneaking on gold blocks will teleport you up/down, when false, right or left clicking will do the same.");
		PlayerSettingAPI.registerSetting(this.useJumpSneakTP, mainMenu);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void goldBlockSneak(PlayerToggleSneakEvent event) {
		if (!this.useJumpSneakTP.getValue(event.getPlayer())) {
			return;
		}
		Block below = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (below.getType() != Material.GOLD_BLOCK) {
			return;
		}
		if (!event.isSneaking()) {
			return;
		}
		for (int y = (below.getY() - 1); y > below.getWorld().getMinHeight(); y--) {
			if (doTeleport(below, event.getPlayer(), y)) {
				return;
			}
		}
		event.getPlayer().sendMessage(
				ChatColor.RED + "No gold block to teleport you down to. Jump to teleport up instead.");
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void goldBlockJump(PlayerJumpEvent event) {
		if (!this.useJumpSneakTP.getValue(event.getPlayer())) {
			return;
		}
		Block below = event.getFrom().getBlock().getRelative(BlockFace.DOWN);
		if (below.getType() != Material.GOLD_BLOCK) {
			return;
		}
		for (int y = below.getY() + 1; y <= below.getWorld().getMaxHeight(); y++) {
			if (doTeleport(below, event.getPlayer(), y)) {
				return;
			}
		}
		event.getPlayer()
				.sendMessage(ChatColor.RED + "No gold block to teleport you up to. Sneak to teleport down instead.");
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void goldBlockInteract(PlayerInteractEvent event) {
		if (this.useJumpSneakTP.getValue(event.getPlayer())) {
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (interactFixer.checkInteracted(event.getPlayer(), event.getClickedBlock())) {
				return;
			}
		} else if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		Block below = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (below.getType() != Material.GOLD_BLOCK) {
			return;
		}
		if (event.getClickedBlock() == null) {
			return;
		}
		if (!event.getClickedBlock().equals(below)) {
			return;
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			for (int y = below.getY() - 1; y > below.getWorld().getMinHeight(); y--) {
				if (doTeleport(below, event.getPlayer(), y)) {
					return;
				}
			}
			event.getPlayer().sendMessage(
					Component.text("No gold block to teleport you down to. Right click to teleport up instead.")
							.color(NamedTextColor.RED));
		} else {
			for (int y = below.getY() + 1; y <= below.getWorld().getMaxHeight(); y++) {
				if (doTeleport(below, event.getPlayer(), y)) {
					return;
				}
			}
			event.getPlayer().sendMessage(
					Component.text("No gold block to teleport you up to. Left click to teleport down instead.")
							.color(NamedTextColor.RED));
		}
	}

	private boolean doTeleport(Block source, Player player, int y) {
		Block target = source.getWorld().getBlockAt(source.getX(), y, source.getZ());
		if (target.getType() != Material.GOLD_BLOCK) {
			return false;
		}
		if (!TeleportUtil.checkForTeleportSpace(target.getRelative(BlockFace.UP).getLocation())) {
			return false;
		}
		source.getWorld().playSound(source.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 8.0F);
		Location adjustedLocation = target.getLocation().clone();
		adjustedLocation.add(0.5, 1.02, 0.5);
		adjustedLocation.setYaw(player.getLocation().getYaw());
		adjustedLocation.setPitch(player.getLocation().getPitch());
		player.playSound(adjustedLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 8.0F);
		player.teleport(adjustedLocation);
		return true;
	}
}
