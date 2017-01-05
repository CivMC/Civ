package com.programmerdan.minecraft.banstick.handler;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSPlayer;

/**
 * Base handler for setting up event captures. Like people logging in who are about to get BanSticked.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
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
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void joinHighest(PlayerJoinEvent joinEvent) {
		
	}
}
