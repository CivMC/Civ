package com.programmerdan.minecraft.banstick.handler;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSShare;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.namelayer.NameAPI;

/**
 * Base handler for setting up event captures. Like people logging in who are about to get BanSticked.
 *
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BanStickEventHandler implements Listener {
	private float proxyThreshold = 2.0f;
	private boolean enableIPBans = true;
	private boolean enableSubnetBans = true;
	private boolean enableProxyKicks = true;
	private boolean enableProxyBans = false;
	private boolean enableNewProxyBans = false;
	private boolean enableShareBans = false;
	private String proxyBanMessage = null;
	private int shareThreshold = 0;
	private String shareBanMessage = null;
	private boolean transitiveBans = false;

	public BanStickEventHandler(FileConfiguration config) {
		// setup.
		configureEvents(config.getConfigurationSection("events"));
		registerEvents();
	}

	private void configureEvents(ConfigurationSection config) {
		this.proxyThreshold = (float) config.getDouble("proxy.threshold", proxyThreshold);
		this.enableIPBans = config.getBoolean("enable.ipBans", true);
		this.enableSubnetBans = config.getBoolean("enable.subnetBans", true);
		this.enableProxyKicks = config.getBoolean("enable.proxyKicks", true);
		this.enableProxyBans = config.getBoolean("enable.proxyBans", false);
		this.enableNewProxyBans = config.getBoolean("enable.newProxyBans", false);
		this.enableShareBans = config.getBoolean("enable.shareBans", false);
		this.proxyBanMessage = config.getString("proxy.banMessage", null);
		this.shareThreshold = config.getInt("share.threshold", shareThreshold);
		this.shareBanMessage = config.getString("share.banMessage", null);
		this.transitiveBans = config.getBoolean("enable.transitiveBans");
	}

	private void registerEvents() {
		Bukkit.getPluginManager().registerEvents(this, BanStick.getPlugin());
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void asyncPreJoinLowest(AsyncPlayerPreLoginEvent asyncEvent) {
		final InetAddress preJoinAddress = asyncEvent.getAddress();
		final UUID preJoinUUID = asyncEvent.getUniqueId();
		final String preJoinName = asyncEvent.getName();
		// let other prejoins do their thing, we'll trigger a number of tasks now.

		// First, trigger a UUID based lookup. TODO: Use Async Handler
		BSPlayer player = BSPlayer.byUUID(preJoinUUID);
		if (player == null) { // create and attempt to name -- review if this is safe.
			String name = null;
			try {
				name = NameAPI.getCurrentName(preJoinUUID);
			} catch (NoClassDefFoundError ncde) { } // no namelayer
			if (name == null) {
				name = preJoinName;
			}
			BanStick.getPlugin().debug("New player {0}, creating record. Best guess at name: {1}", preJoinUUID, name);
			player = BSPlayer.create(preJoinUUID, name);
		}
		BSIP ip = BanStickDatabaseHandler.getInstance().getOrCreateIP(preJoinAddress);
		if (player != null) {
			BSBan ban = player.getBan();
			if (ban != null) {
				if (ban.getBanEndTime() != null && ban.getBanEndTime().before(new Date())) { // ban has ended.
					player.setBan(null);
				} else {
					BanStick.getPlugin().info("Preventing login by " + player.getName() + " due to " + ban.toString());
					Bukkit.broadcast("Preventing login by " + player.getName() + " due to " + ban.toString(), "banstick.ips");
					asyncEvent.disallow(Result.KICK_BANNED, ban.getMessage());
					return;
				}
			}
			if (transitiveBans) {
			    for(BSPlayer alt : player.getTransitiveSharedPlayers(true)) {
			        ban = alt.getBan();
		            if (ban != null) {
		                if (ban.getBanEndTime() != null && ban.getBanEndTime().before(new Date())) { // ban has ended.
		                    alt.setBan(null);
		                } else {
		                    BanStick.getPlugin().info("Preventing login by " + player.getName() + " due to " + ban.toString() + " transitively applied from " + alt.getName());
		                    Bukkit.broadcast("Preventing login by " + player.getName() + " due to " + ban.toString() + " on " + alt.getName(), "banstick.ips");
		                    asyncEvent.disallow(Result.KICK_BANNED, ban.getMessage());
		                    return;
		                }
		            }
			    }
			}
			if (player.getIPPardonTime() != null) {
				BanStick.getPlugin().info("Skipping IP checks due to pardon for player " + player.getName());
				return;
			}
		}

		if (this.enableIPBans) {
			// Second, trigger an exact IP based lookup.
			if (ip != null) {
				List<BSBan> ipBans = BSBan.byIP(ip, false);
				for (int i = ipBans.size() - 1 ; i >= 0; i-- ) {
					//TODO: Can I have better selectivity here? What are the rules?
					BSBan pickOne = ipBans.get(i);
					if (pickOne.getBanEndTime() != null && pickOne.getBanEndTime().before(new Date())) {
						continue; // skip expired ban.
					}
					if (player != null) {
						// associate!
						player.setBan(pickOne); // get most recent matching IP ban and use it.
					}
					BanStick.getPlugin().info("Preventing login by " + player.getName() + " due to " + pickOne.toString());
					Bukkit.broadcast("Preventing login by " + player.getName() + " due to " + pickOne.toString(), "banstick.ips");
					asyncEvent.disallow(Result.KICK_BANNED, pickOne.getMessage());
					return;
				}
			}
		}

		if (this.enableSubnetBans) {
			// Third, trigger a CIDR lookup. This will continue until done; it does not tie into login or async join events.
			List<BSIP> subnets = BSIP.allMatching(preJoinAddress);
			for (BSIP sip : subnets) {
				BanStick.getPlugin().debug("Check for bans on IP: {0}", sip.getId());
				List<BSBan> sipBans = BSBan.byIP(sip, false);
				for (int i = sipBans.size() - 1 ; i >= 0; i-- ) {
					//TODO: Can I have better selectivity here? What are the rules?
					BSBan pickOne = sipBans.get(i);
					if (pickOne.getBanEndTime() != null && pickOne.getBanEndTime().before(new Date())) {
						continue; // skip expired ban.
					}
					if (player != null) {
						// associate!
						player.setBan(pickOne); // get most recent matching subnet ban and use it.
					}
					BanStick.getPlugin().info("Preventing login by " + player.getName() + " due to " + pickOne.toString());
					Bukkit.broadcast("Preventing login by " + player.getName() + " due to " + pickOne.toString(), "banstick.ips");
					asyncEvent.disallow(Result.KICK_BANNED, pickOne.getMessage());
					return;
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void asyncPreJoinHighest(AsyncPlayerPreLoginEvent asyncEvent) {
		// TODO: idea is we'd poll futures for results here
	}


	@EventHandler(priority=EventPriority.HIGHEST)
	public void loginHighest(PlayerLoginEvent loginEvent) {

	}

	/**
	 * This handler deals with registering the player if they are new, starting their session, and triggering
	 * session sharing and vpn warning checks.
	 *
	 * @param joinEvent
	 * 	The PlayerJoin event.
	 */
	@EventHandler(priority=EventPriority.MONITOR)
	public void joinMonitor(PlayerJoinEvent joinEvent) {
		final Player player = joinEvent.getPlayer();
		final Date playerNow = new Date();
		Bukkit.getScheduler().runTaskAsynchronously(BanStick.getPlugin(), new Runnable() {

			@Override
			public void run() {
				// Get or create player.
				if (player == null) {
					BanStick.getPlugin().debug("A player check event was scheduled, but that player is already gone?");
					return;
				}

				BSPlayer bsPlayer = BSPlayer.byUUID(player.getUniqueId());

				String nowName = null;
				try {
					nowName = NameAPI.getCurrentName(player.getUniqueId());
				} catch (NoClassDefFoundError cnfe) {} // no namelayer

				if (nowName == null) {
					nowName = player.getDisplayName();
				}
				if (bsPlayer.getName() == null || !bsPlayer.getName().equals(nowName)) {
					bsPlayer.setName(nowName);
				}
				bsPlayer.startSession(player, playerNow);
				// The above does all the Shared Session checks, so check result here:
				if (enableShareBans && bsPlayer.getSharedPardonTime() == null ) { // no blank check
					try {
						// New approach: unpardoned shares count against your shareThreshold. If you're above shareThreshold,
						// order accounts by join date and ban all unbanned accounts using Share Ban assignments that are newest
						// and that exceed the shareThreshold.

						// Don't apply share bans bidirectionally / automatically.
						List<BSShare> shares = bsPlayer.getUnpardonedShares();

						int cardinality = shares.size();
						if (cardinality > shareThreshold && shareThreshold > -1) { // are we multiaccount banning & are we above threshold?
							// Find the Shares above the threshold for newest accounts by create age. Ban them (might be this one).
							// For each you ban, check if online / kick.
							BanStick.getPlugin().info("Player {0} has exceeding the shared account threshold. Banning the newest accounts that exceed the Threshold.", bsPlayer.getName());
							int bansIssued = 0;

							Set<Long> joinNoted = new HashSet<>();
							TreeMap<Long, BSPlayer> joinTimes = new TreeMap<>();
							for (BSShare latest : shares) {
								BSPlayer one = latest.getFirstPlayer();
								if (!joinNoted.contains(one.getId())) {
									joinNoted.add(one.getId());
									joinTimes.put(one.getFirstAdd().getTime(), one);
								}
								BSPlayer two = latest.getSecondPlayer();
								if (!joinNoted.contains(two.getId())) {
									joinNoted.add(two.getId());
									joinTimes.put(two.getFirstAdd().getTime(), two);
								}

							}

							// A shareThreshold of 0 means you can't Alt; so we ban all but the oldest account.
							// Similarly a shareThreshold of 2 means you can have 2 alts; we ban all but the 3 oldest accounts.
							// TreeMaps naturally order smallest to largest, so:
							int skips = shareThreshold + 1;
							for (BSPlayer banPlayer : joinTimes.values()) {
								if (skips > 0) {
									skips --;
									continue;
								}
								if (banPlayer.getBan() != null || banPlayer.getSharedPardonTime() != null) {
									continue; // already banned, or spared from Share bans.
								}
								// Bannerino using latest unpardoned.
								BSShare useForBan = null;
								if (banPlayer.getId() == bsPlayer.getId()) { // ban person joining.
									useForBan = bsPlayer.getLatestShare();
								} else {
									List<BSShare> banShares = bsPlayer.sharesWith(banPlayer);
									if (banShares == null) continue;
									Collections.reverse(banShares); // by default list is oldest to newest
									for (BSShare testShare : banShares) { // we want newest to oldest
										if (!testShare.isPardoned()) {
											useForBan = testShare;
											break;
										}
									}
									if (useForBan == null) {
										BanStick.getPlugin().warning("Something went wrong! Claim was that the connection of {0} shared with {1} unpardoned, yet only pardoned shares found.", banPlayer.getName(), bsPlayer.getName());
										continue;
									}
								}
								BSBan doTheBan = BSBan.create(useForBan, shareBanMessage, null, false);
								banPlayer.setBan(doTheBan);
								bansIssued++;

								doKickWithCheckup(banPlayer.getUUID(), doTheBan);
							}
							BanStick.getPlugin().info("Player {0} exceeding the shared account threshold resulted in {1} bans.", bsPlayer.getName(), bansIssued);
						}
					} catch (Exception e) {
						BanStick.getPlugin().warning("Failure during Share checks: ", e);
					}
				}

				if (bsPlayer.getBan() != null) {
					BanStick.getPlugin().debug("Player {0} is now banned, skipping proxy checks.", bsPlayer.getName());
					return;
				}

				// Then do VPN checks
				if (enableProxyBans || enableProxyKicks) {
					// Inject IP Hub handler.
					BanStick.getPlugin().getIPHubHandler().offer(bsPlayer.getLatestSession().getIP());

					try {
						if (bsPlayer.getProxyPardonTime() == null) {
							if (bsPlayer.getLatestSession().getIP() == null) {
								BanStick.getPlugin().warning("Weird failure, no ip for {0}", bsPlayer);
								return;
							}
							if (bsPlayer.getLatestSession().getIP().getIPAddress() == null) {
								BanStick.getPlugin().warning("Weird failure, no ip address for {0}", bsPlayer);
								return;
							}
							List<BSIPData> proxyChecks = BSIPData.allByIP(bsPlayer.getLatestSession().getIP());
							if (proxyChecks != null) {
								for (BSIPData proxyCheck : proxyChecks) {
									BanStick.getPlugin().debug("Check for bans on Proxy: {0}", proxyCheck.getId());
									List<BSBan> proxyBans = BSBan.byProxy(proxyCheck, false);
									for (int i = proxyBans.size() - 1 ; i >= 0; i-- ) {
										BSBan pickOne = proxyBans.get(i);
										if (pickOne.getBanEndTime() != null && pickOne.getBanEndTime().before(new Date())) {
											continue; // skip expired ban.
										}

										if (enableProxyBans) {
											bsPlayer.setBan(pickOne); // get most recent matching proxy ban and use it.
										}

										if (enableProxyKicks) {
											doKickWithCheckup(player.getUniqueId(), pickOne);
										}

										return;
									}
									// no ban yet; check if proxy meets /exceeds threshold for banning and new proxy bans are enabled.
									if (proxyCheck.getProxy() >= proxyThreshold && enableNewProxyBans) {
										BSBan newBan = BSBan.create(proxyCheck, proxyBanMessage, null, false);

										if (enableProxyBans) {
											bsPlayer.setBan(newBan);
										}

										if (enableProxyKicks) {
											doKickWithCheckup(player.getUniqueId(), newBan);
										}

										return;
									}
								}
							}
						}
					} catch (Exception e) {
						BanStick.getPlugin().severe("Failed to check proxies: ", e);
					}
				}
				// etc.
			}

		});
	}

	public void manageDeferredProxyKick(final BSIP proxySource, final BSIPData proxyCheck) {
		BanStick.getPlugin().debug("Deferred check for bans on Proxy: {0}", proxyCheck.getId());

		// check if proxy meets /exceeds threshold for banning and new proxy bans are enabled.
		if (proxyCheck.getProxy() >= proxyThreshold && enableNewProxyBans) {
			List<BSBan> proxyBans = BSBan.byProxy(proxyCheck, false);
			BSBan newBan = null;
			if (proxyBans != null && proxyBans.size() > 0) {
				for (BSBan checkBan : proxyBans) {
					if (checkBan.getBanEndTime() != null && checkBan.getBanEndTime().after(new Date())) {
						newBan = checkBan;
						break;
					}
				}
			}
			if (newBan == null && (proxyBans == null || proxyBans.size() == 0)) {
				newBan = BSBan.create(proxyCheck, proxyBanMessage, null, false);
			}

			// now look for online players that match, and kickban them.
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player != null) {
					BSPlayer bsPlayer = BSPlayer.byUUID(player.getUniqueId());

					if (bsPlayer != null && bsPlayer.getProxyPardonTime() == null) {

						if (proxySource.getId() == bsPlayer.getLatestSession().getIP().getId()) { // match!
							if (enableProxyBans) {
								bsPlayer.setBan(newBan);
							}
							if (enableProxyKicks) {
								doKickWithCheckup(player.getUniqueId(), newBan);
							}
						}
					}
				}
			}


			return;
		}

	}

	private void doKickWithCheckup(final UUID puuid, final BSBan picked) {
		// now schedule a task to kick out the trash.
		Bukkit.getScheduler().runTask(BanStick.getPlugin(), new Runnable() {

			@Override
			public void run() {
				Player player = Bukkit.getPlayer(puuid);
				if (player != null) {
					player.kickPlayer(picked.getMessage());
					BanStick.getPlugin().info("Removing " + player.getDisplayName() + " due to " + picked.toString());
					Bukkit.broadcast("Removing " + player.getDisplayName() + " due to " + picked.toString(), "banstick.ips");

					new BukkitRunnable() {
						private int recheck = 0;
						@Override
						public void run() {
							// let's keep checking to make sure they are gone
							recheck ++;
							if (recheck % 10 == 9) {
								BanStick.getPlugin().warning("Trying to kick {0} due to {1}, on {2}th retry.", puuid, picked, recheck);
							}
							Player player = Bukkit.getPlayer(puuid);
							if (player != null) {
								player.kickPlayer(picked.getMessage());
							} else {
								this.cancel();
							}
						}
					}.runTaskTimer(BanStick.getPlugin(), 10l, 10l);
				} else {
					BanStick.getPlugin().info("On return, banning " + puuid + " due to " + picked.toString());
					Bukkit.broadcast("On return, banning " + puuid + " due to " + picked.toString(), "banstick.ips");
				}
			}

		});
	}
	/**
	 * Calls {@link #disconnectEvent(Player)}
	 * @param quitEvent
	 * 	The PlayerQuitEvent
	 */
	@EventHandler(priority=EventPriority.MONITOR)
	public void quitMonitor(PlayerQuitEvent quitEvent) {
		disconnectEvent(quitEvent.getPlayer());
	}

	/**
	 * Calls {@link #disconnectEvent(Player)}
	 * @param kickEvent
	 * 	The PlayerKickEvent
	 */
	@EventHandler(priority=EventPriority.MONITOR)
	public void kickMonitor(PlayerKickEvent kickEvent) {
		disconnectEvent(kickEvent.getPlayer());
	}

	/**
	 * Ends the player's session.
	 *
	 * @param player
	 */
	private void disconnectEvent(final Player player) {
		BSPlayer bsPlayer = BSPlayer.byUUID(player.getUniqueId());
		if (bsPlayer != null) {
			bsPlayer.endSession(new Date());
		}
	}

	/**
	 *
	 * Attempts to ensure that regardless of shutdown order, captures session end.
	 */
	public void shutdown() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			disconnectEvent(player); // ensure some kind of session end is captured on orderly shutdown.
		}
	}
}
