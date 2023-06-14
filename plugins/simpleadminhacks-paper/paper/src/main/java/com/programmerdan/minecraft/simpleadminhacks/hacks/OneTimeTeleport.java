package com.programmerdan.minecraft.simpleadminhacks.hacks;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.google.common.collect.Lists;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.OneTimeTeleportConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.NamedCommand;
import vg.civcraft.mc.civmodcore.commands.TabComplete;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.LongSetting;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public final class OneTimeTeleport extends SimpleHack<OneTimeTeleportConfig> implements Listener {

	public OneTimeTeleport(
			final @NotNull SimpleAdminHacks plugin,
			final @NotNull OneTimeTeleportConfig config
	) {
		super(plugin, config);
	}

	public static @NotNull OneTimeTeleportConfig generate(
			final @NotNull SimpleAdminHacks plugin,
			final @NotNull ConfigurationSection config
	) {
		return new OneTimeTeleportConfig(plugin, config);
	}

	@Override
	public void onEnable() {
		// Since there's no way to unregister settings without some gnarly reflection, this will only register settings
		// that aren't already registered. Also, MoreCollectionUtils.getMissing() when?
		final Collection<PlayerSetting<?>> allSettings = PlayerSettingAPI.getAllSettings();
		final Collection<PlayerSetting<?>> ourSettings = Lists.newArrayList(this.hasOTT, this.timeSinceGranted);
		ourSettings.removeIf(allSettings::contains);
		ourSettings.forEach((setting) -> PlayerSettingAPI.registerSetting(setting, null));

		plugin().registerListener(this);
		plugin().getCommands().registerCommand(this.commands);
	}

	@Override
	public void onDisable() {
		plugin().getCommands().unregisterCommand(this.commands);
		HandlerList.unregisterAll(this);
	}

	private final Map<UUID, UUID> senderToReceiver = new TreeMap<>();

	private final BooleanSetting hasOTT = new BooleanSetting(
			plugin(),
			false,
			"Can you use a one time teleport?",
			"hasOTT",
			"Allows usage of /ott to <player>"
	);

	private final LongSetting timeSinceGranted = new LongSetting(
			plugin(),
			-1L,
			"Time since OTT granted",
			"timeSinceOTTGrant"
	);

	private final BaseCommand commands = new NamedCommand("ott") {
		@Default
		public void defaultCommand(
				final Player sender
		) {
			if (checkOTT(sender.getUniqueId())) {
				sender.sendMessage(Component.text("Your one time teleport will expire in " + TextUtil.formatDuration(
						config().getTimeLimitOnUsageInMillis() - (System.currentTimeMillis() - OneTimeTeleport.this.timeSinceGranted.getValue(sender.getUniqueId()))
				), NamedTextColor.GREEN));
				return;
			}
			sender.sendMessage(Component.text("You don't have a one time teleport.", NamedTextColor.RED));
		}

		@Subcommand("to|request")
		public void requestOTT(
				final Player sender,
				final OnlinePlayer targetPlayer
		) {
			if (!checkOTT(sender.getUniqueId())) {
				sender.sendMessage(Component.text("Your one-time teleport has expired!"));
				return;
			}

			if (!OneTimeTeleport.this.hasOTT.getValue(sender.getUniqueId())) {
				sender.sendMessage(Component.text("You have already used your OTT!", NamedTextColor.RED));
				return;
			}

			if (GLUE_isCombatTagged(sender.getUniqueId())) {
				sender.sendMessage(Component.text("You cannot OTT while in combat!", NamedTextColor.RED));
				return;
			}

			if (GLUE_isPearled(sender.getUniqueId())) {
				sender.sendMessage(Component.text("You cannot OTT while pearled!", NamedTextColor.RED));
				return;
			}

			final UUID previousRequest = OneTimeTeleport.this.senderToReceiver.put(
					sender.getUniqueId(),
					targetPlayer.getPlayer().getUniqueId()
			);

			if (previousRequest != null) {
				final Player previousTargetPlayer = Bukkit.getPlayer(previousRequest);
				if (previousTargetPlayer != null) {
					previousTargetPlayer.sendMessage(Component.text(sender.getName() + " has rescinded their OTT request to you.", NamedTextColor.GREEN));
				}
			}

			sender.sendMessage(Component.text("You have requested to teleport to " + targetPlayer.getPlayer().getName() + "!", NamedTextColor.GREEN));

			final String commandStr = "/ott accept " + sender.getName();
			targetPlayer.getPlayer().sendMessage(Component.text()
					.content("Click me or type \"" + commandStr + "\" to accept!")
					.color(NamedTextColor.DARK_GREEN)
					.decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
					.hoverEvent(HoverEvent.showText(Component.text("Clicking this message will accept the OTT request")))
					.clickEvent(ClickEvent.runCommand(commandStr))
			);
		}

		@Subcommand("revoke|rescind|stop")
		public void rescindRequest(
				final Player sender
		) {
			final UUID targetUUID = OneTimeTeleport.this.senderToReceiver.remove(sender.getUniqueId());
			if (targetUUID == null) {
				sender.sendMessage(Component.text("You have no active OTT requests.", NamedTextColor.RED));
				return;
			}
			sender.sendMessage(Component.text("You have revoked your OTT request!", NamedTextColor.GREEN));
			final Player targetPlayer = Bukkit.getPlayer(targetUUID);
			if (targetPlayer != null) {
				targetPlayer.sendMessage(Component.text(sender.getName() + " has rescinded their OTT request to you.", NamedTextColor.GREEN));
			}
		}

		@TabComplete("sah_ott_requests")
		public List<String> getRequestsToMe(
				final @NotNull BukkitCommandCompletionContext context
		) {
			final UUID targetPlayerUUID = context.getPlayer().getUniqueId();
			return OneTimeTeleport.this.senderToReceiver.entrySet()
					.stream()
					.filter((entry) -> Objects.equals(targetPlayerUUID, entry.getValue()))
					.map((entry) -> Bukkit.getPlayer(entry.getKey()))
					.filter(Objects::nonNull)
					.map(Player::getName)
					.toList();
		}

		@Subcommand("accept|approve|allow|yes")
		@CommandCompletion("@sah_ott_requests")
		public void acceptOTT(
				final Player sender,
				final OnlinePlayer requestingOnlinePlayer
		) {
			final Player requestingPlayer = requestingOnlinePlayer.getPlayer();

			if (!OneTimeTeleport.this.senderToReceiver.remove(
					requestingPlayer.getUniqueId(),
					sender.getUniqueId()
			)) {
				sender.sendMessage(Component.text("There are no active requests from that player!", NamedTextColor.RED));
				return;
			}

			if (!checkOTT(requestingPlayer.getUniqueId())) {
				sender.sendMessage(Component.text(requestingPlayer.getName() + "'s one-time teleport has expired!"));
				requestingPlayer.sendMessage(Component.text("Failed to teleport because your one-time teleport has expired!"));
				return;
			}

			if (GLUE_isCombatTagged(requestingPlayer.getUniqueId())) {
				sender.sendMessage(Component.text(requestingPlayer.getName() + " could not one-time teleport as they're in combat!", NamedTextColor.RED));
				requestingPlayer.sendMessage(Component.text(sender.getName() + " accepted your request, but you're in combat!", NamedTextColor.RED));
				OneTimeTeleport.this.senderToReceiver.put(requestingPlayer.getUniqueId(), sender.getUniqueId()); // Be kind and put the request back!
				return;
			}

			if (GLUE_isPearled(requestingPlayer.getUniqueId())) {
				sender.sendMessage(Component.text(requestingPlayer.getName() + " could not one-time teleport as they're pearled!", NamedTextColor.RED));
				requestingPlayer.sendMessage(Component.text(sender.getName() + " accepted your request, but you're pearled!", NamedTextColor.RED));
				return;
			}

			if (!isSafeLocation(sender, requestingPlayer)) {
				sender.sendMessage(Component.text("This isn't a safe location to accept a one-time teleport!", NamedTextColor.RED));
				requestingPlayer.sendMessage(Component.text(sender.getName() + " tried to accept your one-time teleport but is in an unsafe location!", NamedTextColor.RED));
				OneTimeTeleport.this.senderToReceiver.put(requestingPlayer.getUniqueId(), sender.getUniqueId()); // Be kind and put the request back!
				return;
			}

			// Remove any blacklisted materials from the player's inventory
			requestingPlayer.sendMessage(Component.text("You may find some items missing after teleporting, these were removed as they are blacklisted to be teleported with!", NamedTextColor.AQUA));
			final Inventory requestingPlayerInventory = requestingPlayer.getInventory();
			config().getMaterialBlacklist().forEach(requestingPlayerInventory::remove);

			OneTimeTeleport.this.hasOTT.setValue(requestingPlayer.getUniqueId(), false);

			requestingPlayer.teleport(sender.getLocation());
			sender.sendMessage(Component.text(requestingPlayer.getName() + " has been teleported to you!", NamedTextColor.GREEN));
		}

		@Subcommand("reject|refuse|deny|no")
		@CommandCompletion("@sah_ott_requests")
		public void rejectOTT(
				final Player sender,
				final OnlinePlayer requestingOnlinePlayer
		) {
			final Player requestingPlayer = requestingOnlinePlayer.getPlayer();

			if (!OneTimeTeleport.this.senderToReceiver.remove(
					requestingPlayer.getUniqueId(),
					sender.getUniqueId()
			)) {
				sender.sendMessage(Component.text("There are no active requests from that player!", NamedTextColor.RED));
				return;
			}

			requestingPlayer.sendMessage(Component.text(sender.getName() + " has denied your OTT request!", NamedTextColor.RED));
		}
	};

	@EventHandler
	public void onFirstJoin(final PlayerJoinEvent event) {
		checkOTT(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent event) {
		final Player whoQuit = event.getPlayer();
		// Remove all pending OTT's involving this player
		this.senderToReceiver.entrySet().removeIf((entry) -> {
			final UUID requesterUUID = entry.getKey(), targetUUID = entry.getValue();

			// The player was the target of OTT requests
			if (Objects.equals(whoQuit.getUniqueId(), targetUUID)) {
				final Player requesterPlayer = Bukkit.getPlayer(requesterUUID);
				if (requesterPlayer != null) { // Just in case
					requesterPlayer.sendMessage(Component.text("Your OTT request to " + whoQuit.getName() + " has been voided."));
				}
				return true;
			}

			// The player had made an OTT request
			if (Objects.equals(whoQuit.getUniqueId(), requesterUUID)) {
				final Player targetPlayer = Bukkit.getPlayer(targetUUID);
				if (targetPlayer != null) { // Just in case
					targetPlayer.sendMessage(Component.text(whoQuit.getName() + "'s OTT request has been voided."));
				}
			}
			return false;
		});
	}

	private static boolean GLUE_isPearled(
			final @NotNull UUID playerUUID
	) {
		if (Bukkit.getPluginManager().isPluginEnabled("ExilePearl")) {
			return ExilePearlPlugin.getApi().getPearlManager().getPearl(playerUUID) != null;
		}
		return false;
	}

	private static boolean GLUE_isCombatTagged(
			final @NotNull UUID playerUUID
	) {
		final Plugin cbtPlugin = Bukkit.getPluginManager().getPlugin("CombatTagPlus");
		return cbtPlugin != null && ((CombatTagPlus) cbtPlugin).getTagManager().isTagged(playerUUID);
	}

	private @NotNull List<Block> getNearbyBlocks(
			final @NotNull Block start,
			final int radius
	) {
		if (radius <= 0) {
			return new ArrayList<>(0);
		}
		final int iterations = (radius * 2) + 1;
		final var blocks = new ArrayList<Block>(iterations * iterations * iterations);
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					blocks.add(start.getRelative(x, y, z));
				}
			}
		}
		return blocks;
	}

	private boolean isSafeLocation(
			final @NotNull Player targetPlayer,
			final @NotNull Player requestingPlayer
	) {
		final Location destination = targetPlayer.getLocation();

		if (Bukkit.getPluginManager().isPluginEnabled("Bastion")) {
			for (final BastionBlock bastion : Bastion.getBastionManager().getBlockingBastions(destination)) {
				if (!bastion.canPlace(targetPlayer) || !bastion.canPlace(requestingPlayer)) {
					return false;
				}
			}
		}

		for (final Block block : getNearbyBlocks(destination.getBlock(), 8)) {
			if (config().getUnsafeMaterials().contains(block.getType())) {
				return false;
			}
		}

		return true;
	}

	private boolean checkOTT(
			final @NotNull UUID uuid
	) {
		final long timeSince = this.timeSinceGranted.getValue(uuid);
		if (timeSince == -1L && !this.hasOTT.getValue(uuid)) {
			this.hasOTT.setValue(uuid, true);
			this.timeSinceGranted.setValue(uuid, System.currentTimeMillis());
			return true;
		}
		else if (
				timeSince != -1L
						&& System.currentTimeMillis() >= (timeSince + config().getTimeLimitOnUsageInMillis())
						&& this.hasOTT.getValue(uuid)
		) {
			this.hasOTT.setValue(uuid, false);
			this.senderToReceiver.remove(uuid);
			return false;
		}
		return this.hasOTT.getValue(uuid);
	}
}
