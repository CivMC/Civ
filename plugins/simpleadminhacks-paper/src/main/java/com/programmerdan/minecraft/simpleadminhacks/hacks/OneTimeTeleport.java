package com.programmerdan.minecraft.simpleadminhacks.hacks;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.OneTimeTeleportConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.civmc.zorweth.CrossServerOttManager;
import net.civmc.zorweth.RocketTransferKeys;
import net.civmc.zorweth.ZorwethPlugin;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.TabComplete;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.LongSetting;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public final class OneTimeTeleport extends SimpleHack<OneTimeTeleportConfig> implements Listener, PluginMessageListener {

    private static final String BUNGEE_CHANNEL = "BungeeCord";
    private static final String OTT_CHANNEL = "SAH_OTT";
    private static final String MESSAGE_REQUEST = "REQUEST";
    private static final String MESSAGE_ACK = "ACK";
    private static final String MESSAGE_NOT_FOUND = "NOT_FOUND";
    private static final String MESSAGE_ACCEPT = "ACCEPT";
    private static final String MESSAGE_REJECT = "REJECT";
    private static final String MESSAGE_CANCEL = "CANCEL";
    private static final long REMOTE_REQUEST_TTL_MILLIS = 120_000L;
    private static final long REMOTE_ARRIVAL_TTL_MILLIS = 120_000L;

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
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin(), BUNGEE_CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin(), BUNGEE_CHANNEL, this);
    }

    @Override
    public void onDisable() {
        plugin().getCommands().unregisterCommand(this.commands);
        HandlerList.unregisterAll(this);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin(), BUNGEE_CHANNEL);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin(), BUNGEE_CHANNEL, this);
    }

    private final Map<UUID, UUID> senderToReceiver = new HashMap<>();
    private final Map<UUID, RemoteOutgoingRequest> remoteOutgoingRequests = new HashMap<>();
    private final Map<UUID, RemoteIncomingRequest> remoteIncomingRequests = new HashMap<>();

    private static final long OTT_AVAILABLE = -1L;
    private static final long OTT_UNAVAILABLE = -2L;
    private final LongSetting grantedTimestamps = new LongSetting(
        plugin(),
        OTT_AVAILABLE,
        "Time since OTT granted",
        "timeSinceOTTGrant"
    );

    private final OTTCommand commands = new OTTCommand();

    private record RemoteOutgoingRequest(UUID requestId, UUID requesterId, String requesterName, String targetName,
                                         String targetServer, long expiresAtMillis) {
    }

    private record RemoteIncomingRequest(UUID requestId, UUID requesterId, String requesterName, String sourceServer,
                                         UUID targetId, String targetName, long expiresAtMillis) {
    }

    @FunctionalInterface
    private interface MessageWriter {

        void write(DataOutputStream output) throws IOException;
    }

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
            final String targetPlayerName
        ) {
            final Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
            if (targetPlayer == null) {
                requestRemoteOTT(sender, targetPlayerName);
                return;
            }

            if (targetPlayer == sender) {
                sender.sendMessage(Component.text("You cannot OTT to yourself!", NamedTextColor.RED));
                return;
            }

            switch (testPermissibility(sender, targetPlayer)) {
                case OK -> {
                }
                case FAIL_NO_OTT -> {
                    sender.sendMessage(Component.text("You are no longer able to use OTT!"));
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
            final RemoteOutgoingRequest outgoingRequest = OneTimeTeleport.this.remoteOutgoingRequests.remove(sender.getUniqueId());
            if (outgoingRequest != null) {
                sendRemoteMessage(outgoingRequest.targetServer(), output -> {
                    output.writeUTF(MESSAGE_CANCEL);
                    output.writeUTF(outgoingRequest.requestId().toString());
                    output.writeUTF(sender.getName());
                });
                sender.sendMessage(Component.text("You have revoked your OTT request!", NamedTextColor.GREEN));
                return;
            }

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
                .collect(Collectors.collectingAndThen(Collectors.toList(), names -> {
                    OneTimeTeleport.this.remoteIncomingRequests.values().stream()
                        .filter(request -> Objects.equals(targetPlayerUUID, request.targetId()))
                        .filter(request -> !isExpired(request.expiresAtMillis()))
                        .map(RemoteIncomingRequest::requesterName)
                        .forEach(names::add);
                    return names;
                }));
        }

        @Subcommand("accept|approve|allow|yes")
        @CommandCompletion("@sah_ott_requests")
        @Description("Accepts an OTT request to your location.")
        @Syntax("<requester>")
        public void acceptOTT(
            final Player sender,
            final String requestingPlayerName
        ) {
            final Player requestingPlayer = Bukkit.getPlayerExact(requestingPlayerName);
            if (requestingPlayer == null) {
                acceptRemoteOTT(sender, requestingPlayerName);
                return;
            }

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
                case OK -> {
                }
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
                for (final BastionBlock bastion : Bastion.getBastionManager().getBlockingBastions(sender.getLocation(), b -> b.getType().isBlockLiquids())) {
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
            final String requestingPlayerName
        ) {
            final Player requestingPlayer = Bukkit.getPlayerExact(requestingPlayerName);
            if (requestingPlayer == null) {
                rejectRemoteOTT(sender, requestingPlayerName);
                return;
            }

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
    }

    private void requestRemoteOTT(final Player sender, final String targetPlayerName) {
        final CrossServerOttManager ottManager = getCrossServerOttManager();
        if (ottManager == null) {
            sender.sendMessage(Component.text("Could not find player " + targetPlayerName + "!", NamedTextColor.RED));
            return;
        }
        if (config().isLimitingToSameWorld()) {
            sender.sendMessage(Component.text("You cannot OTT to another server!", NamedTextColor.RED));
            return;
        }

        switch (testPermissibility(sender, sender)) {
            case OK -> {
            }
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

        final String targetServer = ottManager.getDestinationServer();
        final UUID requestId = UUID.randomUUID();
        final long expiresAtMillis = System.currentTimeMillis() + REMOTE_REQUEST_TTL_MILLIS;
        this.remoteOutgoingRequests.put(sender.getUniqueId(), new RemoteOutgoingRequest(
            requestId, sender.getUniqueId(), sender.getName(), targetPlayerName, targetServer, expiresAtMillis));

        if (!sendRemoteMessage(targetServer, output -> {
            output.writeUTF(MESSAGE_REQUEST);
            output.writeUTF(requestId.toString());
            output.writeUTF(sender.getUniqueId().toString());
            output.writeUTF(sender.getName());
            output.writeUTF(ottManager.getServerName());
            output.writeUTF(targetPlayerName);
            output.writeLong(expiresAtMillis);
        })) {
            this.remoteOutgoingRequests.remove(sender.getUniqueId());
            sender.sendMessage(Component.text("Unable to send cross-server OTT request.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Looking for " + targetPlayerName + " on " + targetServer + "...", NamedTextColor.GREEN));
        Bukkit.getScheduler().runTaskLater(plugin(), () -> {
            final RemoteOutgoingRequest request = this.remoteOutgoingRequests.get(sender.getUniqueId());
            if (request != null && request.requestId().equals(requestId) && isExpired(request.expiresAtMillis())) {
                this.remoteOutgoingRequests.remove(sender.getUniqueId());
                final Player online = Bukkit.getPlayer(sender.getUniqueId());
                if (online != null) {
                    online.sendMessage(Component.text("Your cross-server OTT request expired.", NamedTextColor.RED));
                }
            }
        }, REMOTE_REQUEST_TTL_MILLIS / 50L);
    }

    private void acceptRemoteOTT(final Player target, final String requesterName) {
        final RemoteIncomingRequest request = findIncomingRemoteRequest(target.getUniqueId(), requesterName);
        if (request == null) {
            target.sendMessage(Component.text("There are no active requests from that player!", NamedTextColor.RED));
            return;
        }
        if (isExpired(request.expiresAtMillis())) {
            this.remoteIncomingRequests.remove(request.requestId());
            target.sendMessage(Component.text("That OTT request has expired.", NamedTextColor.RED));
            return;
        }

        if (!validateDestination(target, request.requesterName())) {
            return;
        }

        final CrossServerOttManager ottManager = getCrossServerOttManager();
        if (ottManager == null) {
            target.sendMessage(Component.text("Cross-server OTT is not available.", NamedTextColor.RED));
            return;
        }

        ottManager.prepareArrivalAsync(request.requesterId(), target,
            System.currentTimeMillis() + REMOTE_ARRIVAL_TTL_MILLIS, () -> acceptPreparedRemoteOTT(target, request,
                ottManager.getServerName()), exception -> {
            plugin().getLogger().log(Level.SEVERE, "Failed to prepare cross-server OTT arrival", exception);
            target.sendMessage(Component.text("Unable to prepare cross-server OTT arrival.", NamedTextColor.RED));
        });
    }

    private void acceptPreparedRemoteOTT(final Player target, final RemoteIncomingRequest request,
                                         final String targetServer) {
        this.remoteIncomingRequests.remove(request.requestId());
        if (!sendRemoteMessage(request.sourceServer(), output -> {
            output.writeUTF(MESSAGE_ACCEPT);
            output.writeUTF(request.requestId().toString());
            output.writeUTF(target.getUniqueId().toString());
            output.writeUTF(target.getName());
            output.writeUTF(targetServer);
        })) {
            target.sendMessage(Component.text("Unable to notify " + request.requesterName() + "'s server.", NamedTextColor.RED));
            return;
        }
        target.sendMessage(Component.text("You have accepted " + request.requesterName() + "'s OTT request!", NamedTextColor.GREEN));
    }

    private void rejectRemoteOTT(final Player target, final String requesterName) {
        final RemoteIncomingRequest request = findIncomingRemoteRequest(target.getUniqueId(), requesterName);
        if (request == null) {
            target.sendMessage(Component.text("There are no active requests from that player!", NamedTextColor.RED));
            return;
        }
        this.remoteIncomingRequests.remove(request.requestId());
        sendRemoteMessage(request.sourceServer(), output -> {
            output.writeUTF(MESSAGE_REJECT);
            output.writeUTF(request.requestId().toString());
            output.writeUTF(target.getName());
        });
        target.sendMessage(Component.text("You have rejected " + request.requesterName() + "'s OTT request!", NamedTextColor.GREEN));
    }

    private boolean validateDestination(final Player target, final String requesterName) {
        if (Bukkit.getPluginManager().isPluginEnabled("Bastion")) {
            for (final BastionBlock bastion : Bastion.getBastionManager().getBlockingBastions(target.getLocation(), b -> b.getType().isBlockLiquids())) {
                if (!bastion.canPlace(target)) {
                    target.sendMessage(Component.text(requesterName + " could not one-time teleport to a hostile bastion!", NamedTextColor.RED));
                    return false;
                }
            }
        }

        for (final Block block : getNearbyBlocks(target.getLocation().getBlock(), 8)) {
            if (config().getUnsafeMaterials().contains(block.getType())) {
                target.sendMessage(Component.text(requesterName + " could not one-time teleport with unsafe blocks nearby!", NamedTextColor.RED));
                return false;
            }
        }
        return true;
    }

    private RemoteIncomingRequest findIncomingRemoteRequest(final UUID targetId, final String requesterName) {
        for (final RemoteIncomingRequest request : this.remoteIncomingRequests.values()) {
            if (request.targetId().equals(targetId) && request.requesterName().equalsIgnoreCase(requesterName)) {
                return request;
            }
        }
        return null;
    }

    private boolean isEligibleForCrossServerOtt(Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("Zorweth")) {
            return !player.getPersistentDataContainer().has(RocketTransferKeys.NO_OTT);
        }
        return true;
    }

    private CrossServerOttManager getCrossServerOttManager() {
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("Zorweth");
        if (plugin instanceof ZorwethPlugin zorweth && plugin.isEnabled()) {
            return zorweth.getCrossServerOttManager();
        }
        return null;
    }

    private boolean isExpired(final long expiresAtMillis) {
        return System.currentTimeMillis() > expiresAtMillis;
    }

    private boolean sendRemoteMessage(final String targetServer, final MessageWriter writer) {
        final Player relay = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (relay == null) {
            return false;
        }

        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Forward");
        output.writeUTF(targetServer);
        output.writeUTF(OTT_CHANNEL);

        final ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
        try (DataOutputStream messageOutput = new DataOutputStream(messageBytes)) {
            writer.write(messageOutput);
        } catch (final IOException exception) {
            plugin().getLogger().log(Level.WARNING, "Failed to write cross-server OTT plugin message", exception);
            return false;
        }

        output.writeShort(messageBytes.toByteArray().length);
        output.write(messageBytes.toByteArray());
        relay.sendPluginMessage(plugin(), BUNGEE_CHANNEL, output.toByteArray());
        return true;
    }

    @Override
    public void onPluginMessageReceived(final @NotNull String channel, final @NotNull Player player,
                                        final byte @NotNull [] message) {
        if (!channel.equals(BUNGEE_CHANNEL)) {
            return;
        }

        final DataInputStream input;
        try {
            final com.google.common.io.ByteArrayDataInput outerInput = ByteStreams.newDataInput(message);
            if (!outerInput.readUTF().equals(OTT_CHANNEL)) {
                return;
            }
            final short length = outerInput.readShort();
            final byte[] messageBytes = new byte[length];
            outerInput.readFully(messageBytes);
            input = new DataInputStream(new ByteArrayInputStream(messageBytes));
            handlePluginMessage(input);
        } catch (final IOException | IllegalArgumentException exception) {
            plugin().getLogger().log(Level.WARNING, "Failed to read cross-server OTT plugin message", exception);
        }
    }

    private void handlePluginMessage(final DataInputStream input) throws IOException {
        final String type = input.readUTF();
        switch (type) {
            case MESSAGE_REQUEST -> handleRemoteRequest(input);
            case MESSAGE_ACK -> handleRemoteAck(input);
            case MESSAGE_NOT_FOUND -> handleRemoteNotFound(input);
            case MESSAGE_ACCEPT -> handleRemoteAccept(input);
            case MESSAGE_REJECT -> handleRemoteReject(input);
            case MESSAGE_CANCEL -> handleRemoteCancel(input);
            default -> plugin().getLogger().warning("Unknown cross-server OTT plugin message type: " + type);
        }
    }

    private void handleRemoteRequest(final DataInputStream input) throws IOException {
        final UUID requestId = UUID.fromString(input.readUTF());
        final UUID requesterId = UUID.fromString(input.readUTF());
        final String requesterName = input.readUTF();
        final String sourceServer = input.readUTF();
        final String targetName = input.readUTF();
        final long expiresAtMillis = input.readLong();
        final Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || isExpired(expiresAtMillis)) {
            sendRemoteMessage(sourceServer, output -> {
                output.writeUTF(MESSAGE_NOT_FOUND);
                output.writeUTF(requestId.toString());
                output.writeUTF(targetName);
            });
            return;
        }

        this.remoteIncomingRequests.put(requestId, new RemoteIncomingRequest(
            requestId, requesterId, requesterName, sourceServer, target.getUniqueId(), target.getName(), expiresAtMillis));
        sendRemoteMessage(sourceServer, output -> {
            output.writeUTF(MESSAGE_ACK);
            output.writeUTF(requestId.toString());
            output.writeUTF(target.getName());
        });

        final String commandStr = "/ott accept " + requesterName;
        target.sendMessage(Component.text(requesterName + " has requested to OTT to you from " + sourceServer + ".", NamedTextColor.GREEN));
        target.sendMessage(Component.text()
            .content("Click me or type \"" + commandStr + "\" to accept!")
            .color(NamedTextColor.DARK_GREEN)
            .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
            .hoverEvent(HoverEvent.showText(Component.text("Clicking this message will suggest an accept command.")))
            .clickEvent(ClickEvent.suggestCommand(commandStr))
        );
    }

    private void handleRemoteAck(final DataInputStream input) throws IOException {
        final UUID requestId = UUID.fromString(input.readUTF());
        final String targetName = input.readUTF();
        final RemoteOutgoingRequest request = findOutgoingRequest(requestId);
        if (request == null) {
            return;
        }
        final Player requester = Bukkit.getPlayer(request.requesterId());
        if (requester != null) {
            requester.sendMessage(Component.text("You have requested to teleport to " + targetName + "!", NamedTextColor.GREEN));
        }
    }

    private void handleRemoteNotFound(final DataInputStream input) throws IOException {
        final UUID requestId = UUID.fromString(input.readUTF());
        final String targetName = input.readUTF();
        final RemoteOutgoingRequest request = removeOutgoingRequest(requestId);
        if (request == null) {
            return;
        }
        final Player requester = Bukkit.getPlayer(request.requesterId());
        if (requester != null) {
            requester.sendMessage(Component.text("Could not find player " + targetName + "!", NamedTextColor.RED));
        }
    }

    private void handleRemoteAccept(final DataInputStream input) throws IOException {
        final UUID requestId = UUID.fromString(input.readUTF());
        final UUID targetId = UUID.fromString(input.readUTF());
        final String targetName = input.readUTF();
        final String targetServer = input.readUTF();
        final RemoteOutgoingRequest request = removeOutgoingRequest(requestId);
        if (request == null) {
            return;
        }
        final Player requester = Bukkit.getPlayer(request.requesterId());
        if (requester == null) {
            cancelAcceptedRemoteArrival(targetServer, request);
            return;
        }

        switch (testPermissibility(requester, requester)) {
            case OK -> {
            }
            case FAIL_NO_OTT -> {
                requester.sendMessage(Component.text("Failed to teleport because your one-time teleport has expired!"));
                cancelAcceptedRemoteArrival(targetServer, request);
                return;
            }
            case FAIL_IN_COMBAT -> {
                requester.sendMessage(Component.text(targetName + " accepted your request, but you're in combat!", NamedTextColor.RED));
                cancelAcceptedRemoteArrival(targetServer, request);
                return;
            }
            case FAIL_IS_PEARLED -> {
                requester.sendMessage(Component.text(targetName + " accepted your request, but you're pearled!", NamedTextColor.RED));
                cancelAcceptedRemoteArrival(targetServer, request);
                return;
            }
            case FAIL_DIFFERENT_WORLD -> {
                requester.sendMessage(Component.text(targetName + " accepted your request, but you're in a different world!", NamedTextColor.RED));
                cancelAcceptedRemoteArrival(targetServer, request);
                return;
            }
        }

        final CrossServerOttManager ottManager = getCrossServerOttManager();
        if (ottManager == null) {
            requester.sendMessage(Component.text("Cross-server OTT is not available.", NamedTextColor.RED));
            cancelAcceptedRemoteArrival(targetServer, request);
            return;
        }

        this.grantedTimestamps.setValue(requester.getUniqueId(), OTT_UNAVAILABLE);
        ottManager.transfer(requester, targetServer);
        this.logger.info("Player[" + requester.getName() + "] has cross-server OTT-teleported to [" + targetName
            + "/" + targetId + "] on [" + targetServer + "]");
    }

    private void handleRemoteReject(final DataInputStream input) throws IOException {
        final UUID requestId = UUID.fromString(input.readUTF());
        final String targetName = input.readUTF();
        final RemoteOutgoingRequest request = removeOutgoingRequest(requestId);
        if (request == null) {
            return;
        }
        final Player requester = Bukkit.getPlayer(request.requesterId());
        if (requester != null) {
            requester.sendMessage(Component.text(targetName + " has denied your OTT request!", NamedTextColor.RED));
        }
    }

    private void handleRemoteCancel(final DataInputStream input) throws IOException {
        final UUID requestId = UUID.fromString(input.readUTF());
        final String requesterName = input.readUTF();
        final UUID requesterId = input.available() > 0 ? UUID.fromString(input.readUTF()) : null;
        if (requesterId != null) {
            final CrossServerOttManager ottManager = getCrossServerOttManager();
            if (ottManager != null) {
                ottManager.clearArrivalAsync(requesterId);
            }
        }
        final RemoteIncomingRequest request = this.remoteIncomingRequests.remove(requestId);
        if (request == null) {
            return;
        }
        final Player target = Bukkit.getPlayer(request.targetId());
        if (target != null) {
            target.sendMessage(Component.text(requesterName + "'s OTT request has been voided."));
        }
    }

    private RemoteOutgoingRequest findOutgoingRequest(final UUID requestId) {
        for (final RemoteOutgoingRequest request : this.remoteOutgoingRequests.values()) {
            if (request.requestId().equals(requestId)) {
                return request;
            }
        }
        return null;
    }

    private RemoteOutgoingRequest removeOutgoingRequest(final UUID requestId) {
        final RemoteOutgoingRequest request = findOutgoingRequest(requestId);
        if (request != null) {
            this.remoteOutgoingRequests.remove(request.requesterId());
        }
        return request;
    }

    private void cancelAcceptedRemoteArrival(final String targetServer, final RemoteOutgoingRequest request) {
        sendRemoteMessage(targetServer, output -> {
            output.writeUTF(MESSAGE_CANCEL);
            output.writeUTF(request.requestId().toString());
            output.writeUTF(request.requesterName());
            output.writeUTF(request.requesterId().toString());
        });
    }

    @EventHandler
    public void onFirstJoin(final PlayerJoinEvent event) {
        checkOTT(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player whoQuit = event.getPlayer();
        final RemoteOutgoingRequest outgoingRequest = this.remoteOutgoingRequests.remove(whoQuit.getUniqueId());
        if (outgoingRequest != null) {
            sendRemoteMessage(outgoingRequest.targetServer(), output -> {
                output.writeUTF(MESSAGE_CANCEL);
                output.writeUTF(outgoingRequest.requestId().toString());
                output.writeUTF(whoQuit.getName());
            });
        }

        this.remoteIncomingRequests.values().removeIf(request -> {
            if (!Objects.equals(request.targetId(), whoQuit.getUniqueId())) {
                return false;
            }
            sendRemoteMessage(request.sourceServer(), output -> {
                output.writeUTF(MESSAGE_REJECT);
                output.writeUTF(request.requestId().toString());
                output.writeUTF(whoQuit.getName());
            });
            return true;
        });

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
                return true;
            }
            return false;
        });
    }

    /**
     * All possible permissible states that requesting and accepting share!
     */
    private enum OttPermissible {OK, FAIL_NO_OTT, FAIL_IN_COMBAT, FAIL_IS_PEARLED, FAIL_DIFFERENT_WORLD}

    private @NotNull OttPermissible testPermissibility(
        final @NotNull Player requestingPlayer,
        final @NotNull Player destinationPlayer
    ) {
        if (!checkOTT(requestingPlayer.getUniqueId())) {
            return OttPermissible.FAIL_NO_OTT;
        }
        if (!isEligibleForCrossServerOtt(requestingPlayer)) {
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
