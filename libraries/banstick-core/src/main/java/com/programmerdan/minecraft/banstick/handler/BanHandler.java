package com.programmerdan.minecraft.banstick.handler;

import com.programmerdan.minecraft.banstick.containers.BanResult;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSSession;
import com.programmerdan.minecraft.banstick.data.BSShare;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.huskydog.banstickCore.BanstickCore;
import xyz.huskydog.banstickCore.cmc.utils.DateUtils;
import xyz.huskydog.banstickCore.cmc.utils.PluginPlayer;
import vg.civcraft.mc.namelayer.NameLayerAPI;

/**
 * A series of static utility classes to facilitate issuing bans.
 *
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 */
public final class BanHandler {

    private static final @NotNull BanstickCore CORE = Objects.requireNonNull(BanstickCore.getInstance());
    private static final String AUTO_BAN = "Automatic Ban";
    private static final String ADMIN_BAN = "Administrative Ban";

    private BanHandler() {
    }

    /**
     * Issues a ban against a specific UUID.
     *
     * <p>If the player is online, kicks them.
     *
     * <p>This uses the default message. See {@link #doUUIDBan(UUID, String, boolean)} for custom message,
     * or {@link #doUUIDBan(UUID, Date, boolean)} for end time, or {@link #doUUIDBan(UUID, String, Date, boolean)} for
     * both.
     *
     * @param playerId The UUID of the player to ban.
     * @param adminBan Was this admin ban or automatic?
     * @return A summary of who was banned.
     */
    public static BanResult doUUIDBan(UUID playerId, boolean adminBan) {
        return doUUIDBan(playerId, null, null, adminBan);
    }

    /**
     * Issues a ban against a specific UUID.
     *
     * <p>If the player is online, kicks them.
     *
     * <p>This uses the default message but a custom end time.
     *
     * @param playerId The UUID of the player to ban.
     * @param banEnd   The time the ban should end.
     * @param adminBan Was this admin ban or automatic?
     * @return A summary of who was banned.
     */
    public static BanResult doUUIDBan(UUID playerId, Date banEnd, boolean adminBan) {
        return doUUIDBan(playerId, null, banEnd, adminBan);
    }

    /**
     * Issues a ban against a specific UUID.
     *
     * <p>If the player is online, kicks them.
     *
     * <p>This uses a custom message.
     *
     * @param playerId The UUID of the player to ban.
     * @param message  The message to display when a player attempts to rejoin.
     * @param adminBan Was this admin ban or automatic?
     * @return A summary of who was banned.
     */
    public static BanResult doUUIDBan(UUID playerId, String message, boolean adminBan) {
        return doUUIDBan(playerId, message, null, adminBan);
    }

    /**
     * Issues a ban against a specific UUID.
     *
     * <p>If the player is online, kicks them.
     *
     * <p>This uses a custom message and end time.
     *
     * @param playerId The UUID of the player to ban.
     * @param rawMsg   The message to display when a player attempts to rejoin.
     * @param banEnd   The time the ban should end.
     * @param adminBan Was this admin ban or automatic?
     * @return A summary of who was banned.
     */
    public static BanResult doUUIDBan(UUID playerId, String rawMsg, Date banEnd, boolean adminBan) {
        try {
            final String message = processMessage(adminBan, rawMsg);
            PluginPlayer pluginPlayer = CORE.getPlugin().getPlayer(playerId);
            BSPlayer player = BSPlayer.byUUID(playerId);
            if (player == null) {
                if (pluginPlayer != null) {
                    player = BSPlayer.create(pluginPlayer);
                } else {
                    String playerName = null;
                    try {
                        playerName = NameLayerAPI.getCurrentName(playerId);
                    } catch (NoClassDefFoundError ncde) {
                    }

                    player = BSPlayer.create(playerId, playerName);
                }
            }
            BSBan ban = BSBan.create(message, banEnd, adminBan); // general ban.
            player.setBan(ban);

            if (pluginPlayer != null) {
                CORE.getPlugin().kickPlayer(playerId, generateKickMessage(message, banEnd));
            }

            BanResult result = new BanResult();
            result.addPlayer(player);
            return result;
        } catch (Exception e) {
            CORE.getLogger().error("Failed to issue UUID ban: ", e);
            return new BanResult();
        }
    }

    /**
     * Issues a ban against an IP address.
     * After the ban is created, finds all accounts that are using the IP address and bans them, unless
     * already banned or pardoned.
     *
     * @param exactIP         The IP address to ban.
     * @param rawMsg          The message to use as a ban message; is also sent to all players who are
     *                        online and caught in the ban.
     * @param banEnd          When does the ban end?
     * @param adminBan        Was this an administrative ban?
     * @param includeHistoric Ban everyone who has ever used this IP address?
     * @return A BanResult object describing who was banned.
     */
    public static BanResult doIPBan(BSIP exactIP, @Nullable String rawMsg, Date banEnd,
                                    boolean adminBan, boolean includeHistoric) {
        try {
            final String message = processMessage(adminBan, rawMsg);
            // TODO: match with existing ban for this IP.
            BSBan ban = BSBan.create(exactIP, message, banEnd, adminBan); // general ban.
            BanResult result = new BanResult();
            result.addBan(ban);

            CORE.getPlugin().getOnlinePlayers().forEach(player -> {
                BSPlayer banPlayer = BSPlayer.byUUID(player.getUniqueId());
                if (banPlayer.getIPPardonTime() != null) {
                    return; // pardoned from IP match bans.
                }
                BSSession active = banPlayer.getLatestSession();
                if (active.getIP().getId() == exactIP.getId() && banPlayer.getBan() == null) {
                    // TODO replace with equality check.
                    banPlayer.setBan(ban);
                    result.addPlayer(banPlayer);
                    CORE.getPlugin().kickPlayer(player.getUniqueId(), generateKickMessage(message, banEnd));
                }
            });

            if (includeHistoric) {
                List<BSSession> sessions = BSSession.byIP(exactIP);
                for (BSSession session : sessions) {
                    BSPlayer banPlayer = session.getPlayer();
                    if (banPlayer.getIPPardonTime() != null) {
                        continue; // pardoned from IP match bans.
                    }
                    if (session.getPlayer().getBan() == null) {
                        banPlayer.setBan(ban);
                        result.addPlayer(banPlayer);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            CORE.getLogger().warn("Failed to issue IP ban: ", e);
            return new BanResult();
        }
    }

    /**
     * Does a ban against a CIDR range.
     *
     * @param cidrIP          cidr IP range to ban
     * @param rawMsg          Message to record as ban reason
     * @param banEnd          The time to end the ban
     * @param adminBan        Is this an administrative ban?
     * @param includeHistoric Should we include all historic occurrences of this IP in the ban?
     * @return A BanResult with the bans issued, if any
     */
    public static BanResult doCIDRBan(BSIP cidrIP, String rawMsg, Date banEnd,
                                      boolean adminBan, boolean includeHistoric) {
        try {
            final String message = processMessage(adminBan, rawMsg);
            BSBan ban = BSBan.create(cidrIP, message, banEnd, adminBan); // general ban.
            BanResult result = new BanResult();
            result.addBan(ban);

            CORE.getPlugin().getOnlinePlayers().forEach(player -> {
                BSPlayer banPlayer = BSPlayer.byUUID(player.getUniqueId());
                if (banPlayer.getBan() != null) {
                    return; // already banned.
                }
                if (banPlayer.getIPPardonTime() != null) {
                    return; // pardoned from IP match bans.
                }

                BSSession active = banPlayer.getLatestSession();
                BSIP activeIP = active.getIP();
                boolean doBan = false;
                if (cidrIP.getIPv4Address() != null && activeIP.getIPv4Address() != null) {
                    // check IPv4
                    if (cidrIP.getIPv4Address().contains(activeIP.getIPv4Address())) {
                        doBan = true;
                    }
                } else if (cidrIP.getIPv6Address() != null && activeIP.getIPv6Address() != null) {
                    if (cidrIP.getIPv6Address().contains(activeIP.getIPv6Address())) {
                        doBan = true;
                    }
                } // if mismatched, don't ban.

                if (doBan) {
                    banPlayer.setBan(ban);
                    result.addPlayer(banPlayer);
                    CORE.getPlugin().kickPlayer(player.getUniqueId(), generateKickMessage(message, banEnd));
                }
            });

            if (includeHistoric) {
                List<BSIP> ipsIn = BSIP.allContained(cidrIP.getIPAddress().getLower(),
                    cidrIP.getIPAddress().getNetworkPrefixLength());
                for (BSIP exactIP : ipsIn) {
                    List<BSSession> sessions = BSSession.byIP(exactIP);
                    for (BSSession session : sessions) {
                        BSPlayer banPlayer = session.getPlayer();
                        if (banPlayer.getIPPardonTime() != null) {
                            continue; // pardoned from IP match bans.
                        }
                        if (session.getPlayer().getBan() == null) {
                            banPlayer.setBan(ban);
                            result.addPlayer(banPlayer);
                        }
                    }
                }
            }

            return result;
        } catch (Exception e) {
            CORE.getLogger().warn("Failed to issue CIDR ban: ", e);
            return new BanResult();
        }
    }

    /**
     * Given a share, ban both (or if limitBanTo is set, just one) with a specified message / end / admin flag
     *
     * @param share      The share to ban
     * @param limitBanTo optional player to limit to
     * @param rawMsg     the ban message
     * @param banEnd     the end of the ban
     * @param adminBan   is this an admin ban?
     * @return the result of the ban as a BanResult
     */
    public static BanResult doShareBan(BSShare share, BSPlayer limitBanTo, String rawMsg,
                                       Date banEnd, boolean adminBan) {
        // TODO: figure out if this actually works.
        // does this actually kick anyone who is online?
        // also this doesn't seem to actually ban anyone?
        try {
            final String message = processMessage(adminBan, rawMsg);
            BSBan ban = BSBan.create(share, message, banEnd, adminBan); // share ban
            BanResult result = new BanResult();
            result.addBan(ban);
            if (limitBanTo == null) { // do both
                if (share.getFirstPlayer().getSharedPardonTime() == null && share.getFirstPlayer().getBan() == null) {
                    share.getFirstPlayer().setBan(ban);
                    result.addPlayer(share.getFirstPlayer());
                }
                if (share.getSecondPlayer().getSharedPardonTime() == null && share.getSecondPlayer().getBan() == null) {
                    share.getSecondPlayer().setBan(ban);
                    result.addPlayer(share.getSecondPlayer());
                }
            } else {
                if (share.getFirstPlayer().getId() == limitBanTo.getId()
                    && share.getFirstPlayer().getSharedPardonTime() == null
                    && share.getFirstPlayer().getBan() == null) {
                    share.getFirstPlayer().setBan(ban);
                    result.addPlayer(share.getFirstPlayer());
                }
                if (share.getSecondPlayer().getId() == limitBanTo.getId()
                    && share.getSecondPlayer().getSharedPardonTime() == null
                    && share.getSecondPlayer().getBan() == null) {
                    share.getSecondPlayer().setBan(ban);
                    result.addPlayer(share.getSecondPlayer());
                }
            }
            return result;
        } catch (Exception e) {
            CORE.getLogger().warn("Failed to issue Share ban: ", e);
            return new BanResult();
        }
    }

    /**
     * Checks whether a player is banned.
     *
     * @param player The player to check if banned.
     * @return Returns true if the player is banned.
     */
    public static boolean isPlayerBanned(final PluginPlayer player) {
        return isPlayerBanned(player.getUniqueId());
    }

    /**
     * Checks whether a player is banned.
     *
     * @param puuid The player UUID to check if banned.
     * @return Returns true if the player is banned.
     */
    public static boolean isPlayerBanned(final UUID puuid) {
        BSBan ban = getActivePlayerBanOrTransitive(puuid);
        return ban != null;
    }

    /**
     * Checks whether a player is banned, and returns the ban.
     *
     * @param puuid The player UUID to check if banned.
     * @return Returns true if the player is banned.
     */
    public static BSBan getActivePlayerBanOrTransitive(final UUID puuid) {
        final BSPlayer bsPlayer = BSPlayer.byUUID(puuid);
        if (bsPlayer == null) {
            return null;
        }
        final BSBan bsBan = bsPlayer.getBan();
        if (bsBan != null && !bsBan.hasBanExpired()) {
            return bsBan;
        }
        if (CORE.getPlugin().getConfig().areTransitiveBansEnabled()) {
            for (final BSPlayer alt : bsPlayer.getTransitiveSharedPlayers(true)) {
                final BSBan bsAltBan = alt.getBan();
                if (bsAltBan != null && !bsAltBan.hasBanExpired()) {
                    return bsAltBan;
                }
            }
        }
        return null;
    }

    /**
     * Ensures a message is present, defaulting to either the admin or auto ban message.
     *
     * @param adminBan Was this an admin ban?
     * @param rawMsg   The raw message to process.
     * @return The processed message.
     */
    private static String processMessage(boolean adminBan, @Nullable String rawMsg) {
        final String message;
        if (rawMsg == null || rawMsg.trim().equals("")) {
            message = adminBan ? ADMIN_BAN : AUTO_BAN; // TODO: config!
        } else {
            message = rawMsg.trim();
        }
        return message;
    }

    /**
     * Generates a kick message component from a message and the ban's end time if present.
     *
     * @param message The base message.
     * @param banEnd  The optional end time.
     * @return A Component representing the kick message.
     */
    private static Component generateKickMessage(@NotNull String message, @Nullable Date banEnd) {
        if (banEnd != null) {
            return Component.text(message + ". Ends " + DateUtils.getDateTimeFormat().format(banEnd));
        } else {
            return Component.text(message);
        }
    }
}
