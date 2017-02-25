package com.programmerdan.minecraft.banstick.commands;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSSession;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringException;
import vg.civcraft.mc.namelayer.NameAPI;

public class LoveTapCommand  implements CommandExecutor {

	public static String name = "lovetap";
	
	/*
	 *    /<command> [name/uuid]
   /<command> [ip]
   /<command> [ip]/[CIDR]
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		if (arguments.length < 1) return false;
		
		String preTap = arguments[0];
		int locCIDR = preTap.indexOf('/');
		Boolean hasCIDR = locCIDR > -1; 
		Integer CIDR = (hasCIDR) ? Integer.valueOf(preTap.substring(locCIDR + 1)) : null;
		String toTap = (hasCIDR) ? preTap.substring(0, locCIDR) : preTap;

		BanStick.getPlugin().debug("preTap: {0}, CIDR? {1}, toTap: {2}", 
				preTap, CIDR, toTap);
				
		try {
			IPAddress ipcheck = new IPAddressString(toTap).toAddress();
			
			BSIP exact = !hasCIDR ? BSIP.byIPAddress(ipcheck) : BSIP.byCIDR(ipcheck.toString(), CIDR);
			if (exact == null) {
				sender.sendMessage(ChatColor.RED + "Can't find " + (hasCIDR ? ipcheck.toString() + "/" + CIDR : ipcheck.toString()));
				return true;
			}
			
			List<BSBan> bans = BSBan.byIP(exact, false);
			List<BSSession> sessions = BSSession.byIP(exact);
			List<BSIPData> proxies = BSIPData.allByIP(exact);
			
			StringBuilder sb = new StringBuilder();
			sb.append(ChatColor.BLUE).append("Data for IP ").append(ChatColor.WHITE).append(exact.toString());
			sb.append('\n');
			sb.append(ChatColor.BLUE).append("\nProxies: \n");
			if (proxies == null || proxies.isEmpty()) {
				sb.append(ChatColor.AQUA).append("  None.\n");
			} else {
				for (BSIPData data : proxies) {
					sb.append(ChatColor.WHITE).append("  ").append(data.toString()).append('\n');
				}
			}
			
			sb.append('\n');
			sb.append(ChatColor.BLUE).append("Sessions: \n");
			if (sessions == null || sessions.isEmpty()) {
				sb.append(ChatColor.AQUA).append("  None.\n");
			} else {
				for (BSSession session : sessions) {
					sb.append(ChatColor.WHITE).append("  ").append(session.toString()).append('\n');
				}
			}
			
			sb.append('\n');
			sb.append(ChatColor.BLUE).append("Bans: \n");
			if (bans == null || bans.isEmpty()) {
				sb.append(ChatColor.AQUA).append("  None.\n");
			} else {
				for (BSBan ban : bans) {
					sb.append(ChatColor.WHITE).append("  ").append(ban.toString()).append('\n');
				}
			}
			sender.sendMessage(sb.toString());
			
			return true;
		} catch (IPAddressStringException e) {
			// Not an IP address!
			UUID playerId = null;
			if (toTap.length() <= 16) {
				try {
					playerId = NameAPI.getUUID(toTap);
					
					if (playerId == null) {
						Player match = Bukkit.getPlayer(toTap);
						if (match != null) {
							playerId = match.getUniqueId();
						}
					}
				} catch (Exception ee) {
					sender.sendMessage(ChatColor.RED + "Unable to find player " + ChatColor.DARK_RED + toTap);
				}
			} else if (toTap.length() == 36) {
				try {
					playerId = UUID.fromString(toTap);
				} catch (IllegalArgumentException iae) {
					sender.sendMessage(ChatColor.RED + "Unable to process uuid " + ChatColor.DARK_RED + toTap);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to interpret " + ChatColor.DARK_RED + toTap);
			}
			
			if (playerId != null) {
				BSPlayer player = BSPlayer.byUUID(playerId);
				if (player == null) {
					sender.sendMessage("No Player records for " + toTap);
				}
				
				BSBan ban = player.getBan();
				List<BSSession> history = player.getAllSessions();
				BSSession latest = player.getLatestSession();
				
				BSIPData latestProxy = BSIPData.byContainsIP(latest.getIP());
				
				StringBuffer sb = new StringBuffer();
				sb.append(ChatColor.BLUE + "Session History: " + ChatColor.DARK_BLUE + "(First Join: " + ChatColor.WHITE + player.getFirstAdd() + ")\n");
				for (BSSession histRecord : history) {
					sb.append(ChatColor.WHITE + "  " + histRecord.toFullDisplayString(true) + "\n");
				}
				sb.append("\n");
				sb.append(ChatColor.GREEN + "Most Recent Session: \n");
				sb.append(ChatColor.WHITE + "  " + latest.toFullDisplayString(true) + "\n");
				if (latestProxy != null) {
					sb.append(ChatColor.GRAY + "  Network: " + ChatColor.WHITE + latestProxy.toString() + "\n");
				}
				sb.append("\n");
				if (ban != null) {
					sb.append(ChatColor.RED + "Active Ban: \n");
					sb.append(ChatColor.WHITE + "  " + ban.toString() + "\n");
				}
				sb.append("\n");
				sb.append(ChatColor.GREEN + "Pardoned from future:\n");
				if (player.getIPPardonTime() != null) {
					sb.append(ChatColor.GREEN + "  IP Bans\n");
				}
				if (player.getProxyPardonTime() != null) {
					sb.append(ChatColor.GREEN + "  Proxy Bans\n");
				}
				if (player.getSharedPardonTime() != null) {
					sb.append(ChatColor.GREEN + "  Shared Connection Bans\n");
				}
				if (player.getIPPardonTime() == null && player.getProxyPardonTime() == null && player.getSharedPardonTime() == null) {
					sb.append(ChatColor.RED + "  Nothing\n");
				}
				
				sb.append("\n");
				sb.append(ChatColor.WHITE + player.getName() + " [" + player.getUUID() + "]");
				
				sender.sendMessage(sb.toString());
				
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to find " + ChatColor.DARK_RED + toTap);
			}
		}

		return false;
	}
}
