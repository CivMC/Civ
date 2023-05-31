package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.OneTimeTeleportConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;

import java.util.*;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.LongSetting;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class OneTimeTeleport extends SimpleHack<OneTimeTeleportConfig> implements CommandExecutor {

	private BooleanSetting hasOTT;
	private LongSetting timeSinceGranted;
	private final Map<UUID, UUID> senderToReciever;

	public OneTimeTeleport(SimpleAdminHacks plugin, OneTimeTeleportConfig config) {
		super(plugin, config);
		this.senderToReciever = new HashMap<>();
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(ChatColor.AQUA + "Old school command sender!, Go away console, this is players only");
			return true;
		}
		// /ott should return if you can ott
		// /ott <player> should request ott
		// /ott revoke should cancel a request
		// /ott accept <player> should do teleport if valid ott request
		switch (args.length) {
			case 0:
				if(!this.checkOTT(player.getUniqueId())){
					player.sendMessage(Component.text("You don't have a one time teleport.", NamedTextColor.RED));
				}else{
					//TextUtil.formatDuration()
					long time = this.config.getTimelimitOnUsageInMillis() - (System.currentTimeMillis() - this.timeSinceGranted.getValue(player.getUniqueId()));
					player.sendMessage(Component.text("Your one time teleport will expire in " + TextUtil.formatDuration(time), NamedTextColor.GREEN));
				}
				return true;
			case 1:

				if (args[0].equalsIgnoreCase("revoke")) {
					if (!this.senderToReciever.containsKey(player.getUniqueId())) {
						player.sendMessage(Component.text("You have no active OTT requests", NamedTextColor.RED));
						return true;
					}
					this.hasOTT.setValue(player.getUniqueId(), true);
					this.senderToReciever.remove(player.getUniqueId());
					player.sendMessage(Component.text("You have revoked your OTT request!", NamedTextColor.GREEN));
					return true;
				}

				if(!this.checkOTT(player.getUniqueId())){
					// ott has expired, add message
					player.sendMessage(Component.text("Your one-time teleport has expired!"));
					return true;
				}

				Player target = Bukkit.getPlayer(args[0]);
				if (target == null) {
					player.sendMessage(Component.text("The player " + args[0] + " does not exist or isn't online!", NamedTextColor.RED));
					return true;
				}

				if (this.senderToReciever.containsKey(player.getUniqueId())) {
					player.sendMessage(Component.text("Revoke your existing request first!", NamedTextColor.RED));
					return true;
				}

				if (!this.hasOTT.getValue(player.getUniqueId())) {
					player.sendMessage(Component.text("You have already used your OTT!", NamedTextColor.RED));
					return true;
				}
				player.sendMessage(Component.text("You have requested to teleport to " + target.getName() + "!", NamedTextColor.GREEN));
				requestOTT(player.getUniqueId(), target.getUniqueId());

				String commandStr = "/ott accept " + player.getName();

				Component msg = Component.text(player.getName() + " has requested to teleport to you! ", NamedTextColor.GREEN)
						.append(
								Component.text("Click me or type /ott accept " + player.getName() + " to accept!",
												NamedTextColor.DARK_GREEN,
												TextDecoration.BOLD)
										.clickEvent(ClickEvent.runCommand(commandStr))
										.hoverEvent(HoverEvent.showText(Component.text(commandStr)))
						);

				target.sendMessage(msg);
				return true;
			case 2:
				if (args[0].equalsIgnoreCase("accept")) {
					Player targetPlayer = Bukkit.getPlayer(args[1]);
					if (targetPlayer == null) {
						player.sendMessage(Component.text("The player " + args[1] + " does not exist or isn't online!", NamedTextColor.RED));
						return true;
					}

					if(!this.senderToReciever.containsKey(targetPlayer.getUniqueId()) ||
							!this.senderToReciever.get(targetPlayer.getUniqueId()).equals(player.getUniqueId())){
						player.sendMessage(Component.text("There are no active requests from that player!", NamedTextColor.RED));
						return true;
					}

					if(!this.checkOTT(targetPlayer.getUniqueId())){
						// ott has expired, add message
						player.sendMessage(Component.text(targetPlayer.getName()+"'s one-time teleport has expired!"));
						targetPlayer.sendMessage(Component.text("Failed to teleport because your one-time teleport has expired!"));
						this.senderToReciever.remove(targetPlayer.getUniqueId());
						return true;
					}

					if(!this.isSafeLocation(player, targetPlayer)){
						player.sendMessage(Component.text("This isn't a safe location to accept a one-time teleport!"));
						targetPlayer.sendMessage(Component.text(player.getName() + " tried to accept your one-time teleport in an unsafe location!"));
						return true;
					}


					/*
					long timeJoined = targetPlayer.getFirstPlayed();
					if (System.currentTimeMillis() > (timeJoined + config.getTimelimitOnUsageInMillis())) {
						targetPlayer.sendMessage(Component.text("You have ran out of time to use your one time teleport!"));
						this.hasOTT.setValue(targetPlayer.getUniqueId(), false);
						return true;
					}
					 */

					removeBlacklistItems(targetPlayer.getInventory());
					targetPlayer.sendMessage(Component.text("You may find some items missing after teleporting, these were removed as they are blacklisted to be teleported with!", NamedTextColor.AQUA));
					targetPlayer.teleport(player.getLocation());
					player.sendMessage(Component.text(targetPlayer.getName() + " has been teleported to you!", NamedTextColor.GREEN));
					this.invalidate(targetPlayer.getUniqueId());
					return true;
				}
			default:
				return false;
		}
	}

	public void requestOTT(UUID sender, UUID reciever) {
		this.senderToReciever.put(sender, reciever);
		//this.hasOTT.setValue(sender, false);
	}

	public void removeBlacklistItems(Inventory inventory) {
		for (ItemStack is : inventory.getContents()) {
			if (is == null || is.getType() == Material.AIR) {
				continue;
			}
			if (this.config.getMaterialBlacklist().contains(is.getType())) {
				inventory.remove(is);
			}
		}
	}

	private List<Block> getNearbyBlocks(Block start, int radius){
		if (radius < 0) {
			return new ArrayList<>(0);
		}
		int iterations = (radius * 2) + 1;
		List<Block> blocks = new ArrayList<>(iterations * iterations * iterations);
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					blocks.add(start.getRelative(x, y, z));
				}
			}
		}
		return blocks;
	}

	private boolean isSafeLocation(Player player, Player targetPlayer){
		if(player == null || targetPlayer == null){
			return false;
		}

		Set<BastionBlock> bastions = Bastion.getBastionManager().getBlockingBastions(player.getLocation());
		if(!bastions.stream().allMatch(bastion -> bastion.canPlace(player) && bastion.canPlace(targetPlayer))){
			return false;
		}

		List<Block> blocks = this.getNearbyBlocks(player.getLocation().getBlock(), 8);
		return blocks.stream().noneMatch(block -> this.config.getUnsafeMaterials().contains(block.getType()));
	}

	private void invalidate(UUID uuid){
		this.senderToReciever.remove(uuid);
		this.hasOTT.setValue(uuid, false);
	}

	private boolean checkOTT(UUID uuid){
		if(uuid == null){
			return false;
		}
		//UUID uuid = player.getUniqueId();
		long timeSince = this.timeSinceGranted.getValue(uuid);
		if(timeSince == -1L && !this.hasOTT.getValue(uuid)){
			this.hasOTT.setValue(uuid, true);
			this.timeSinceGranted.setValue(uuid, System.currentTimeMillis());
			return true;
		}else if(timeSince != -1L && System.currentTimeMillis() >= (timeSince + this.config.getTimelimitOnUsageInMillis()) && this.hasOTT.getValue(uuid)){
			this.hasOTT.setValue(uuid, false);
			this.senderToReciever.remove(uuid);
			return false;
		}

		return this.hasOTT.getValue(uuid);
	}

	@EventHandler
	public void onFirstJoin(PlayerSpawnLocationEvent event) {
		if (!config.isEnabled()) {
			return;
		}

		this.checkOTT(event.getPlayer().getUniqueId());
		/*
		if (event.getPlayer().hasPlayedBefore()) {
			return;
		}
		this.hasOTT.setValue(event.getPlayer().getUniqueId(), true);
		 */
	}

	@Override
	public void onEnable() {
		super.onEnable();
		registerSettings();
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	@Override
	public void registerCommands() {
		if (config.isEnabled()) {
			plugin().registerCommand("ott", this);
		}
	}

	private void registerSettings() {
		//Default this to false since we want to set it true if the player has logged in for the first time
		this.hasOTT = new BooleanSetting(this.plugin,
				false,
				"Can you use a one time teleport?",
				"hasOTT",
				"Allows usage of /ott <player>");

		this.timeSinceGranted = new LongSetting(this.plugin,
				-1L,
				"Time since OTT granted",
				"timeSinceOTTGrant");

		//We don't want to expose the setting to a players /config
		PlayerSettingAPI.registerSetting(hasOTT, null);
		PlayerSettingAPI.registerSetting(timeSinceGranted, null);
	}

	public static OneTimeTeleportConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new OneTimeTeleportConfig(plugin, config);
	}
}
