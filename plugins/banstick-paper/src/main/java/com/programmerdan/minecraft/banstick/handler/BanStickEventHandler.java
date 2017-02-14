package com.programmerdan.minecraft.banstick.handler;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
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
import com.programmerdan.minecraft.banstick.data.BSPlayer;

/**
 * Base handler for setting up event captures. Like people logging in who are about to get BanSticked.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BanStickEventHandler implements Listener {
	public BanStickEventHandler(FileConfiguration config) {
		// setup.
		
		registerEvents();
	}
	
	public void registerEvents() {
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
				if (ban.getBanEndTime().before(new Date())) { // ban has ended.
					player.setBan(null);
				} else {
					asyncEvent.disallow(Result.KICK_BANNED, ban.getMessage());
					return;
				}
			}
		}
		
		// Second, trigger an exact IP based lookup.
		BSIP ip = BSIP.byInetAddress(preJoinAddress);
		if (ip != null) {
			List<BSBan> ipBans = BSBan.byIP(ip, false);
			if (!ipBans.isEmpty()) {
				//TODO: Can I have better selectivity here? What are the rules?
				BSBan pickOne = ipBans.get(ipBans.size() - 1);
				if (player != null) {
					// associate! 
					player.setBan(pickOne); // get most recent matching IP ban and use it.
				}
				asyncEvent.disallow(Result.KICK_BANNED, pickOne.getMessage());
				return;
			}
		}
		
		// Finally, trigger a CIDR lookup. This will continue until done; it does not tie into login or async join events.
		List<BSIP> subnets = BSIP.allMatching(preJoinAddress);
		for (BSIP sip : subnets) {
			List<BSBan> sipBans = BSBan.byIP(sip, false);
			if (!sipBans.isEmpty()) {
				//TODO: Can I have better selectivity here? What are the rules?
				BSBan pickOne = sipBans.get(sipBans.size() - 1);
				if (player != null) {
					// associate! 
					player.setBan(pickOne); // get most recent matching subnet ban and use it.
				}
				asyncEvent.disallow(Result.KICK_BANNED, pickOne.getMessage());
				return;
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
				// if (!bsPlayer.vpnPardon) {
				// 		BSVPN = check check
				// }
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
