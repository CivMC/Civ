package com.programmerdan.minecraft.simpleadminhacks.hacks;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.google.common.collect.Lists;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.OneTimeTeleportConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.TabComplete;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.LongSetting;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

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
		final Collection<PlayerSetting<?>> ourSettings = Lists.newArrayList(this.grantedTimestamps);
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

	private final Map<UUID, UUID> senderToReceiver = new HashMap<>();

	private static final long OTT_AVAILABLE = -1L;
	private static final long OTT_UNAVAILABLE = -2L;
	private final LongSetting grantedTimestamps = new LongSetting(
			plugin(),
			OTT_AVAILABLE,
			"Time since OTT granted",
			"timeSinceOTTGrant"
	);

	private final OTTCommand commands = new OTTCommand();
	@CommandAlias("ott")
	private final class OTTCommand extends BaseCommand {
		@Default
		public void defaultCommand(
				final Player sender
		) {
			if (checkOTT(sender.getUniqueId())) {
				final long expiresIn = config().getTimeLimitOnUsageInMillis() - (System.currentTimeMillis() - OneTimeTeleport.this.grantedTimestamps.getValue(sender.getUniqueId()));
				sender.sendMessage(Component.text()
						.content("Your one time teleport will expire in " + TextUtil.formatDuration(expiresIn))
						.color(NamedTextColor.GREEN)
						.decorate(TextDecoration.UNDERLINED)
						.hoverEvent(HoverEvent.showText(Component.text("Clicking this message will suggest an OTT request command")))
						.clickEvent(ClickEvent.suggestCommand("/ott to "))
				);
				return;
			}
			sender.sendMessage(Component.text("You don't have a one-time teleport.", NamedTextColor.RED));
		}

		@Subcommand("to|request")
		@Description("Makes an OTT request to a player. If accepted, you'll teleport to them.")
		@Syntax("<player>")
		public void requestOTT(
				final Player sender,
				@Flags("other")
				final Player targetPlayer
		) {
			if (targetPlayer == sender) {
				sender.sendMessage(Component.text("You cannot OTT to yourself!", NamedTextColor.RED));
				return;
			}

			switch (testPermissibility(sender, targetPlayer)) {
				case OK -> {}
				case FAIL_NO_OTT -> {
					sender.sendMessage(Component.text("Your are no longer able to use OTT!"));
					return;
				}
				case FAIL_IN_COMBAT -> {
					sender.sendMessage(Component.text("You cannot OTT while in combat!", NamedTextColor.RED));
					return;
				}
				case FAIL_IS_PEARLED -> {
					sender.sendMessage(Component.text("You cannot OTT while pearled!", NamedTextColor.RED));
					return;
				}
				case FAIL_DIFFERENT_WORLD -> {
					sender.sendMessage(Component.text("You cannot OTT to another world!", NamedTextColor.RED));
					return;
				}
			}

			final UUID previousRequest = OneTimeTeleport.this.senderToReceiver.put(
					sender.getUniqueId(),
					targetPlayer.getUniqueId()
			);

			if (previousRequest != null) {
				final Player previousTargetPlayer = Bukkit.getPlayer(previousRequest);
				if (previousTargetPlayer != null) {
					previousTargetPlayer.sendMessage(Component.text(sender.getName() + " has rescinded their OTT request to you.", NamedTextColor.GREEN));
				}
			}

			sender.sendMessage(Component.text("You have requested to teleport to " + targetPlayer.getName() + "!", NamedTextColor.GREEN));

			final String commandStr = "/ott accept " + sender.getName();
			targetPlayer.sendMessage(Component.text()
					.content("Click me or type \"" + commandStr + "\" to accept!")
					.color(NamedTextColor.DARK_GREEN)
					.decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
					.hoverEvent(HoverEvent.showText(Component.text("Clicking this message will suggest an accept command.")))
					.clickEvent(ClickEvent.suggestCommand(commandStr))
			);
		}

		@Subcommand("revoke|rescind|stop")
		@Description("Revokes your OTT request.")
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
		@Description("Accepts an OTT request to your location.")
		@Syntax("<requester>")
		public void acceptOTT(
				final Player sender,
				@Flags("other")
				final Player requestingPlayer
		) {
			if (requestingPlayer == sender) {
				sender.sendMessage(Component.text("You cannot accept an OTT from yourself!", NamedTextColor.RED));
				return;
			}

			if (!OneTimeTeleport.this.senderToReceiver.remove(
					requestingPlayer.getUniqueId(),
					sender.getUniqueId()
			)) {
				sender.sendMessage(Component.text("There are no active requests from that player!", NamedTextColor.RED));
				return;
			}

			switch (testPermissibility(requestingPlayer, sender)) {
				case OK -> {}
				case FAIL_NO_OTT -> {
					sender.sendMessage(Component.text(requestingPlayer.getName() + "'s one-time teleport has expired!"));
					requestingPlayer.sendMessage(Component.text("Failed to teleport because your one-time teleport has expired!"));
					return;
				}
				case FAIL_IN_COMBAT -> {
					sender.sendMessage(Component.text(requestingPlayer.getName() + " could not one-time teleport as they're in combat!", NamedTextColor.RED));
					requestingPlayer.sendMessage(Component.text(sender.getName() + " accepted your request, but you're in combat!", NamedTextColor.RED));
					OneTimeTeleport.this.senderToReceiver.put(requestingPlayer.getUniqueId(), sender.getUniqueId()); // Be kind and put the request back!
					return;
				}
				case FAIL_IS_PEARLED -> {
					sender.sendMessage(Component.text(requestingPlayer.getName() + " could not one-time teleport as they're pearled!", NamedTextColor.RED));
					requestingPlayer.sendMessage(Component.text(sender.getName() + " accepted your request, but you're pearled!", NamedTextColor.RED));
					return;
				}
				case FAIL_DIFFERENT_WORLD -> {
					sender.sendMessage(Component.text(requestingPlayer.getName() + " could not one-time teleport as they're in a different world!", NamedTextColor.RED));
					requestingPlayer.sendMessage(Component.text(sender.getName() + " accepted your request, but you're in a different world!", NamedTextColor.RED));
					return;
				}
			}

			if (Bukkit.getPluginManager().isPluginEnabled("Bastion")) {
				for (final BastionBlock bastion : Bastion.getBastionManager().getBlockingBastions(sender.getLocation())) {
					if (!bastion.canPlace(sender) || !bastion.canPlace(requestingPlayer)) {
						sender.sendMessage(Component.text(requestingPlayer.getName() + " could not one-time teleport to a hostile bastion!", NamedTextColor.RED));
						requestingPlayer.sendMessage(Component.text(sender.getName() + " accepted your request, but they're in a hostile bastion!", NamedTextColor.RED));
						OneTimeTeleport.this.senderToReceiver.put(requestingPlayer.getUniqueId(), sender.getUniqueId()); // Be kind and put the request back!
						return;
					}
				}
			}

			for (final Block block : getNearbyBlocks(sender.getLocation().getBlock(), 8)) {
				if (config().getUnsafeMaterials().contains(block.getType())) {
					sender.sendMessage(Component.text(requestingPlayer.getName() + " could not one-time teleport with unsafe blocks nearby!", NamedTextColor.RED));
					requestingPlayer.sendMessage(Component.text(sender.getName() + " accepted your request, but there's unsafe blocks nearby!", NamedTextColor.RED));
					OneTimeTeleport.this.senderToReceiver.put(requestingPlayer.getUniqueId(), sender.getUniqueId()); // Be kind and put the request back!
					return;
				}
			}

			// Remove any blacklisted materials from the player's inventory
			requestingPlayer.sendMessage(Component.text("You may find some items missing after teleporting, these were removed as they are blacklisted to be teleported with!", NamedTextColor.AQUA));
			final Inventory requestingPlayerInventory = requestingPlayer.getInventory();
			config().getMaterialBlacklist().forEach(requestingPlayerInventory::remove);

			OneTimeTeleport.this.grantedTimestamps.setValue(requestingPlayer.getUniqueId(), OTT_UNAVAILABLE);

			OneTimeTeleport.this.logger.info("Player[" + requestingPlayer.getName() + "] has OTT-teleported from [" + WorldUtils.getBlockLocation(requestingPlayer.getLocation()) + "] to [" + sender.getName() + "] at [" + WorldUtils.getBlockLocation(sender.getLocation()) + "]");
			requestingPlayer.teleport(sender.getLocation());
			sender.sendMessage(Component.text(requestingPlayer.getName() + " has been teleported to you!", NamedTextColor.GREEN));
			requestingPlayer.sendMessage(Component.text("You have been teleported to " + sender.getName() + "!", NamedTextColor.GREEN));
		}

		@Subcommand("reject|refuse|deny|no")
		@CommandCompletion("@sah_ott_requests")
		@Description("Rejects an OTT to your location.")
		@Syntax("<requester>")
		public void rejectOTT(
				final Player sender,
				@Flags("other")
				final Player requestingPlayer
		) {
			if (requestingPlayer == sender) {
				sender.sendMessage(Component.text("You cannot reject an OTT from yourself!", NamedTextColor.RED));
				return;
			}

			if (!OneTimeTeleport.this.senderToReceiver.remove(
					requestingPlayer.getUniqueId(),
					sender.getUniqueId()
			)) {
				sender.sendMessage(Component.text("There are no active requests from that player!", NamedTextColor.RED));
				return;
			}
			sender.sendMessage(Component.text("You have rejected " + requestingPlayer.getName() + "'s OTT request!", NamedTextColor.GREEN));
			requestingPlayer.sendMessage(Component.text(sender.getName() + " has denied your OTT request!", NamedTextColor.RED));
		}

		@Subcommand("grant|bestow|give")
		@CommandPermission("simpleadmin.grantott")
		@CommandCompletion("@allplayers")
		@Description("Gives a player a use of OTT as if they first joined.")
		@Syntax("<recipient>")
		public void grantOTT(
				final CommandSender sender,
				final String receivingPlayerName
		) {
			final OfflinePlayer receivingPlayer = Bukkit.getOfflinePlayerIfCached(receivingPlayerName);
			if (receivingPlayer == null) {
				sender.sendMessage(Component.text("Could not find player " + receivingPlayerName + "!", NamedTextColor.RED));
				return;
			}
			OneTimeTeleport.this.grantedTimestamps.setValue(receivingPlayer.getUniqueId(), System.currentTimeMillis());
			sender.sendMessage(Component.text("You have granted " + receivingPlayer.getName() + " an OTT!", NamedTextColor.GREEN));
			final Player onlineReceivingPlayer = receivingPlayer.getPlayer();
			if (onlineReceivingPlayer != null) {
				onlineReceivingPlayer.sendMessage(Component.text("You have been granted an OTT!", NamedTextColor.GREEN));
			}
			OneTimeTeleport.this.logger.info(sender.getName() + " has granted " + receivingPlayer.getName() + " an OTT!");
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

	/** All possible permissible states that requesting and accepting share! */
	private enum OttPermissible { OK, FAIL_NO_OTT, FAIL_IN_COMBAT, FAIL_IS_PEARLED, FAIL_DIFFERENT_WORLD }
	private @NotNull OttPermissible testPermissibility(
			final @NotNull Player requestingPlayer,
			final @NotNull Player destinationPlayer
	) {
		if (!checkOTT(requestingPlayer.getUniqueId())) {
			return OttPermissible.FAIL_NO_OTT;
		}
		if (Bukkit.getPluginManager().isPluginEnabled("CombatTagPlus")) {
			final CombatTagPlus combatTagPlus = JavaPlugin.getPlugin(CombatTagPlus.class);
			if (combatTagPlus.getTagManager().isTagged(requestingPlayer.getUniqueId())) {
				return OttPermissible.FAIL_IN_COMBAT;
			}
		}
		if (Bukkit.getPluginManager().isPluginEnabled("ExilePearl")) {
			if (ExilePearlPlugin.getApi().getPearlManager().getPearl(requestingPlayer.getUniqueId()) != null) {
				return OttPermissible.FAIL_IS_PEARLED;
			}
		}
		if (config().isLimitingToSameWorld()) {
			if (!Objects.equals(requestingPlayer.getWorld(), destinationPlayer.getWorld())) {
				return OttPermissible.FAIL_DIFFERENT_WORLD;
			}
		}
		return OttPermissible.OK;
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

	private boolean checkOTT(
			final @NotNull UUID uuid
	) {
		final long grantedTimestamp = this.grantedTimestamps.getValue(uuid);
		if (grantedTimestamp == OTT_UNAVAILABLE) {
			return false;
		}
		if (grantedTimestamp == OTT_AVAILABLE) {
			this.grantedTimestamps.setValue(uuid, System.currentTimeMillis());
			return true;
		}
		if (System.currentTimeMillis() >= (grantedTimestamp + config().getTimeLimitOnUsageInMillis())) {
			this.grantedTimestamps.setValue(uuid, OTT_UNAVAILABLE);
			return false;
		}
		return true;
	}
}
