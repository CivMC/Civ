package com.programmerdan.minecraft.banstick.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.containers.BanResult;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSSession;
import com.programmerdan.minecraft.banstick.data.BSShare;
import com.programmerdan.minecraft.banstick.handler.BanHandler;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringException;
import inet.ipaddr.IPAddressTypeException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import vg.civcraft.mc.namelayer.NameAPI;

/**
 * Always finish with a DoubleTap to the head -- handles manually unpardoning and perhaps banning nerds who multiaccount
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class DoubleTapCommand implements CommandExecutor {

	public static String name = "doubletap";
	
	/**
     * <b>doubletap [+][ip] [banend: mm/dd/yyyy [hh:mm:ss]] [message]</b>
     * Finds all sessions that use this IP, finds all Shares connected to those Sessions, unpardons any pardoned shares.
     * If [+] before the IP, also Share-bans all non-pardoned players.
     * <br>
     * <b>doubletap [+][name/uuid] [+][name/uuid] [banend: mm/dd/yyyy [hh:mm:ss]] [message]</b>
     * Finds all Shares between the two named players, unpardons them if pardoned.
     * If [+] before a name, also Share-bans that player, removing any Share pardons if they existed.
	 *   
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		// Check if name. Check if uuid. Check if ip-ipv4 vs. ipv6.
		if (arguments.length < 1) return false;
		
		String preBan = arguments[0];
		boolean doBan = preBan.indexOf('+') > -1;
		String toBan = doBan ? preBan.substring(1) : preBan;
		boolean secondDoBan = false;
		String secondBan = null;
		IPAddress ipcheck = null;
		int offset = 0;
		
		// check if IP path
		try {
			ipcheck = new IPAddressString(toBan).toAddress();
		} catch (IPAddressStringException | IPAddressTypeException e) {
			ipcheck = null;

			if (arguments.length < 2) return false;

			offset = 1;
			secondDoBan = arguments[1].indexOf('+') > -1;
			secondBan = secondDoBan ? arguments[1].substring(1) : arguments[1];
		}
		
		String endDate = (arguments.length >= (2 + offset) ? arguments[1 + offset] : null);
		String endTime = (arguments.length >= (3 + offset) ? arguments[2 + offset] : null);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat combinedFormat = new SimpleDateFormat("MM/dd/yyyy HH:mms:ss");
		Date banEndDate = null;
		Date banEndTime = null;
		Date banEnd = null;
		int mStart = 1 + offset;

		BanStick.getPlugin().debug("preBan: {0}, doBan? {1}, toBan: {2}, sDoBan? {3}, sBan: {4}, endDate: {5}, endTime: {6}", 
				preBan, doBan, toBan, secondDoBan, secondBan, endDate, endTime);
		
		if (endDate != null) {
			try {
				banEndDate = dateFormat.parse(endDate); 
				banEnd = banEndDate;
				mStart ++;
			} catch (ParseException pe) {
				banEndDate = null;
			}

			if (banEndDate != null && endTime != null) {
				try {
					banEndTime = combinedFormat.parse(endDate + " " + endTime); 
					banEnd = banEndTime;
					mStart ++;
				} catch (ParseException pe) {
					banEndTime = null;
				}
			}
		}
		
		String message = (arguments.length >= mStart ? String.join(" ", Arrays.copyOfRange(arguments, mStart, arguments.length)) : null);
		
		BanStick.getPlugin().debug("message: {0}", message);
		
		if (ipcheck != null) {
			if (!sender.hasPermission("banstick.ips")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use / view IPs");
				return true;
			}
						
			BSIP exact = BSIP.byIPAddress(ipcheck);
			if (exact == null) {
				sender.sendMessage(ChatColor.YELLOW + "That IP address not found, no shares modified.");
				return true;
			}
			
			List<BSSession> sessions = BSSession.byIP(exact);
			
			if (sessions == null || sessions.isEmpty()) {
				sender.sendMessage(ChatColor.YELLOW + "That IP address does not connect to any sessions.");
				return true;
			}
			
			int unpardoned = 0;
			int banned = 0;
			for (BSSession session : sessions) {
				List<BSShare> shares = BSShare.bySession(session);
				for (BSShare share : shares) {
					if (share.isPardoned()) {
						share.setPardonTime(null);
						unpardoned ++;
						sender.sendMessage(ChatColor.GREEN + "Unpardoned Shared session: " + share.toFullString(sender.hasPermission("banstick.ips")));
					}
					if (doBan) {
						BanResult result = BanHandler.doShareBan(share, null, message, banEnd, true);
						result.informCommandSender(sender);
						banned ++;
					}
				}
			}
			sender.sendMessage(ChatColor.GREEN + "Unpardoned " + unpardoned + " shared and attempted " + banned + " share bans.");

			return true;
		} else {
			// Not an IP address!
			UUID playerId = null;
			if (toBan.length() <= 16) {
				try {
					playerId = null; 
					
					try {
						playerId = NameAPI.getUUID(toBan);
					} catch (NoClassDefFoundError ncde) { }
					
					if (playerId == null) {
						Player match = Bukkit.getPlayer(toBan);
						if (match != null) {
							playerId = match.getUniqueId();
						}
					}
				} catch (Exception ee) {
					sender.sendMessage(ChatColor.RED + "Unable to find player " + ChatColor.DARK_RED + toBan);
					return true;
				}
			} else if (toBan.length() == 36) {
				try {
					playerId = UUID.fromString(toBan);
				} catch (IllegalArgumentException iae) {
					sender.sendMessage(ChatColor.RED + "Unable to process uuid " + ChatColor.DARK_RED + toBan);
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to interpret " + ChatColor.DARK_RED + toBan);
				return true;
			}
			
			UUID secondPlayerId = null;
			if (secondBan.length() <= 16) {
				try {
					secondPlayerId = null;
					
					try {
						secondPlayerId = NameAPI.getUUID(secondBan);
					} catch (NoClassDefFoundError ncde) { }
					
					if (secondPlayerId == null) {
						Player match = Bukkit.getPlayer(secondBan);
						if (match != null) {
							secondPlayerId = match.getUniqueId();
						}
					}
				} catch (Exception ee) {
					sender.sendMessage(ChatColor.RED + "Unable to find player " + ChatColor.DARK_RED + secondBan);
					return true;
				}
			} else if (secondBan.length() == 36) {
				try {
					secondPlayerId = UUID.fromString(secondBan);
				} catch (IllegalArgumentException iae) {
					sender.sendMessage(ChatColor.RED + "Unable to process uuid " + ChatColor.DARK_RED + secondBan);
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to interpret " + ChatColor.DARK_RED + secondBan);
				return true;
			}
			
			BSPlayer player1 = BSPlayer.byUUID(playerId);
			BSPlayer player2 = BSPlayer.byUUID(secondPlayerId);
			
			if (player1 != null && player2 != null) {
				List<BSShare> shares = player1.sharesWith(player2);
				if (shares != null && !shares.isEmpty()) {
					for (BSShare share : shares) {
						if (share.isPardoned()) {
							share.setPardonTime(null);
							sender.sendMessage(ChatColor.GREEN + "Unpardoned Shared session: " + share.toFullString(sender.hasPermission("banstick.ips")));
						}	
					}
					if (doBan || secondDoBan) {
						BanResult result = BanHandler.doShareBan(shares.get(shares.size() - 1),
								doBan && !secondDoBan ? player1 : !doBan && secondDoBan ? player2 : null,
								message, banEnd, true);
						result.informCommandSender(sender);						
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Those players have not shared a connection.");
				}
				
				return true;
			} else {
				if (player1 == null) {
					sender.sendMessage(ChatColor.RED + "Unable to find " + ChatColor.DARK_RED + toBan);
				} 
				if (player2 == null) {
					sender.sendMessage(ChatColor.RED + "Unable to find " + ChatColor.DARK_RED + secondBan);
				}
			}
		}
		return false;
	}
}
