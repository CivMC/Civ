package com.programmerdan.minecraft.banstick.handler;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSShare;

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
	private boolean enableProxyBans = false;
	private boolean enableNewProxyBans = false;
	private boolean enableShareBans = false;
	private String proxyBanMessage = null;
	private int shareThreshold = 0;
	private String shareBanMessage = null;
	
	public BanStickEventHandler(FileConfiguration config) {
		// setup.
		configureEvents(config.getConfigurationSection("events")); 
		registerEvents();
	}
	
	private void configureEvents(ConfigurationSection config) {
		this.proxyThreshold = (float) config.getDouble("proxy.threshold", proxyThreshold);
		this.enableIPBans = config.getBoolean("enable.ipBans", true);
		this.enableSubnetBans = config.getBoolean("enable.subnetBans", true);
		this.enableProxyBans = config.getBoolean("enable.proxyBans", false);
		this.enableNewProxyBans = config.getBoolean("enable.newProxyBans", false);
		this.enableShareBans = config.getBoolean("enable.shareBans", false);
		this.proxyBanMessage = config.getString("proxy.banMessage", null);
		this.shareThreshold = config.getInt("share.threshold", shareThreshold);
		this.shareBanMessage = config.getString("share.banMessage", null);
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
			String name = NameAPI.getCurrentName(preJoinUUID);
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
					asyncEvent.disallow(Result.KICK_BANNED, ban.getMessage());
					return;
				}
			}
			if (player.getIPPardonTime() != null) {
				BanStick.getPlugin().info("Skipping IP checks due to pardon for player " + player.getName());
				return;
			}
		}
		if (ip != null) {
			BanStick.getPlugin().info("Registering future retrieval of IPData for {0}", ip.toString());
			BanStick.getPlugin().getIPDataHandler().offer(ip);
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
				String nowName = NameAPI.getCurrentName(player.getUniqueId());
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
						// Check if any shares have an active ban.
						List<BSShare> shares = bsPlayer.getUnpardonedShares();
						BSBan pickOne = null;
						for (BSShare activeShare: shares) {
							List<BSBan> findBan = BSBan.byShare(activeShare, false);
							if (findBan != null && !findBan.isEmpty()) {
								pickOne = findBan.get(findBan.size() - 1);
								break;
							}
						}
						if (pickOne == null) { // this means that no share has an active ban.
							// Check if newly above threshold (or pardon has been removed and above threshold) and
							// create a ban for every Share that isn't pardoned.
							int cardinality = shares.size();
							if (cardinality > shareThreshold && shareThreshold > 0) { // are we multiaccount banning & are we above threshold?
								// Issue share bans for _all_ shares unpardoned.
								for (BSShare latest : shares) {
									BSBan pickTwo = BSBan.create(latest, shareBanMessage, null, false);
									if (pickTwo != null) {
										pickOne = pickTwo;
									}
								}
							}
						}
						
						if (pickOne != null) {
							bsPlayer.setBan(pickOne);
	
							final BSBan picked = pickOne;
							final UUID puuid = player.getUniqueId();
							Bukkit.getScheduler().runTaskLater(BanStick.getPlugin(), new Runnable() {

								@Override
								public void run() {
									Player player = Bukkit.getPlayer(puuid);
									if (player != null) {
										player.kickPlayer(picked.getMessage());
										BanStick.getPlugin().info("Removing " + player.getDisplayName() + " due to " + picked.toString());
									} else {
										BanStick.getPlugin().info("On return, banning " + puuid + " due to " + picked.toString());
									}
								}
								
							}, 1L);
	
							return;
						}
					} catch (Exception e) {
						BanStick.getPlugin().warning("Failure during Share checks: ", e);
					}
				}
				
				
				// Then do VPN checks
				if (enableProxyBans) {
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
										
										bsPlayer.setBan(pickOne); // get most recent matching proxy ban and use it.
										
										final BSBan picked = pickOne;
										final UUID puuid = player.getUniqueId();										
										Bukkit.getScheduler().runTaskLater(BanStick.getPlugin(), new Runnable() {

											@Override
											public void run() {
												Player player = Bukkit.getPlayer(puuid);
												if (player != null) {
													player.kickPlayer(picked.getMessage());
													BanStick.getPlugin().info("Removing " + player.getDisplayName() + " due to " + picked.toString());
												} else {
													BanStick.getPlugin().info("On return, banning " + puuid + " due to " + picked.toString());
												}
											}
											
										}, 1l);
																		
										return;
									}
									// no ban yet; check if proxy meets /exceeds threshold for banning and new proxy bans are enabled.
									if (proxyCheck.getProxy() >= proxyThreshold && enableNewProxyBans) {
										BSBan newBan = BSBan.create(proxyCheck, proxyBanMessage, null, false);
										
										bsPlayer.setBan(newBan);
										
										final BSBan picked = newBan;
										final UUID puuid = player.getUniqueId();
										Bukkit.getScheduler().runTaskLater(BanStick.getPlugin(), new Runnable() {

											@Override
											public void run() {
												Player player = Bukkit.getPlayer(puuid);
												if (player != null) {
													player.kickPlayer(picked.getMessage());
													BanStick.getPlugin().info("Removing " + player.getDisplayName() + " due to " + picked.toString());
												} else {
													BanStick.getPlugin().info("On return, banning " + puuid + " due to " + picked.toString());
												}
											}
											
										}, 1L);
									
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
