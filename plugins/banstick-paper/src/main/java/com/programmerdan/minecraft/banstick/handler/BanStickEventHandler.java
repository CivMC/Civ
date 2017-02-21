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
	
	public BanStickEventHandler(FileConfiguration config) {
		// setup.
		configureEvents(config.getConfigurationSection("events")); 
		registerEvents();
	}
	
	private void configureEvents(ConfigurationSection config) {
		this.proxyThreshold = (float) config.getDouble("proxyThreshold", proxyThreshold);
		this.enableIPBans = config.getBoolean("enable.ipBans", true);
		this.enableSubnetBans = config.getBoolean("enable.subnetBans", true);
		this.enableProxyBans = config.getBoolean("enable.proxyBans", false);
		this.enableNewProxyBans = config.getBoolean("enable.newProxyBans", false);
	}
	
	private void registerEvents() {
		Bukkit.getPluginManager().registerEvents(this, BanStick.getPlugin());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void asyncPreJoinLowest(AsyncPlayerPreLoginEvent asyncEvent) {
		final InetAddress preJoinAddress = asyncEvent.getAddress();
		final UUID preJoinUUID = asyncEvent.getUniqueId();
		// let other prejoins do their thing, we'll trigger a number of tasks now.
		
		// First, trigger a UUID based lookup. TODO: Use Async Handler
		BSPlayer player = BSPlayer.byUUID(preJoinUUID);
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
		
		if (this.enableIPBans) {
			// Second, trigger an exact IP based lookup.
			BSIP ip = BSIP.byInetAddress(preJoinAddress);
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
	 */
	@EventHandler(priority=EventPriority.MONITOR)
	public void joinMonitor(PlayerJoinEvent joinEvent) {
		final Player player = joinEvent.getPlayer();
		final Date playerNow = new Date();
		Bukkit.getScheduler().runTaskAsynchronously(BanStick.getPlugin(), new Runnable() {

			@Override
			public void run() {
				// Get or create player.
				if (player == null) return;
				
				BSPlayer bsPlayer = BanStickDatabaseHandler.getInstance().getOrCreatePlayer(player);
				bsPlayer.startSession(player, playerNow);
				// The above does all the Shared Session checks, so check result here:
				// if (!bsPlayer.sharedPardon) {
				// 		BSShare = bsPlayer.getShared();
				// }
				// etc.
				
				// Then do VPN checks
				if (enableProxyBans) {
					if (bsPlayer.getProxyPardonTime() == null) {
						List<BSIPData> proxyChecks = BSIPData.allByIP(bsPlayer.getLatestSession().getIP());
						for (BSIPData proxyCheck : proxyChecks) {
							BanStick.getPlugin().debug("Check for bans on Proxy: {0}", proxyCheck.getId());
							List<BSBan> proxyBans = BSBan.byProxy(proxyCheck, false);
							for (int i = proxyBans.size() - 1 ; i >= 0; i-- ) {
								BSBan pickOne = proxyBans.get(i);
								if (pickOne.getBanEndTime() != null && pickOne.getBanEndTime().before(new Date())) {
									continue; // skip expired ban.
								}
								if (player != null) {
									// associate! 
									player.kickPlayer(pickOne.getMessage());
									BanStick.getPlugin().info("Removing " + bsPlayer.getName() + " due to " + pickOne.toString());
								} else {
									BanStick.getPlugin().info("On return, banning " + bsPlayer.getName() + " due to " + pickOne.toString());
								}
								
								bsPlayer.setBan(pickOne); // get most recent matching proxy ban and use it.
								
								return;
							}
							// no ban yet; check if proxy meets /exceeds threshold for banning and new proxy bans are enabled.
							if (proxyCheck.getProxy() >= proxyThreshold && enableNewProxyBans) {
								BSBan newBan = BSBan.create(proxyCheck, "Use of Proxies to disguish connection source is prohibited.", null, false);
								if (player != null) {
									// associate! 
									player.kickPlayer(newBan.getMessage());
									BanStick.getPlugin().info("Removing " + bsPlayer.getName() + " due to " + newBan.toString());
								} else {
									BanStick.getPlugin().info("On return, banning " + bsPlayer.getName() + " due to " + newBan.toString());
								}
								
								bsPlayer.setBan(newBan);								
							}
						}
					}
				}
				// etc.
			}
			
		}); 
	}
	
	/**
	 * Calls {@link #disconnectEvent(Player)}
	 * @param quitEvent
	 */
	@EventHandler(priority=EventPriority.MONITOR) 
	public void quitMonitor(PlayerQuitEvent quitEvent) {
		disconnectEvent(quitEvent.getPlayer());
	}
	
	/**
	 * Calls {@link #disconnectEvent(Player)}
	 * @param kickEvent
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
		bsPlayer.endSession(new Date());
	}
}
