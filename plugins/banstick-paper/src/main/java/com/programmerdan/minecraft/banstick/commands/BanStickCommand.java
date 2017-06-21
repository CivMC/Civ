package com.programmerdan.minecraft.banstick.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.containers.BanResult;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.handler.BanHandler;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringException;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import vg.civcraft.mc.namelayer.NameAPI;

/**
 * BanStick! BanStick! Ban all the nerds by name, CIDR, IP, or some combo.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BanStickCommand implements CommandExecutor {

	public static String name = "banstick";
	
	/**
	 * Behavior: If given a name or uuid, bans that uuid with a new ban if none exists for that uuid.
	 *   Returns ban details.
	 *   
	 *   If name or uuid with a CIDR postfix, bans that uuid, AND issues a ban against their IP address / subnet.
	 *   
	 *   If IP, bans that IP and all players who have used it.
	 *   If IP/CIDR, bans that IP subnet and all players who have used it.
	 *   
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		/*
		 *    - /<command> [ip] [banend: mm/dd/yyyy [hh:mm:ss]] [message]
   - /<command> [ip]/[CIDR] [banend: mm/dd/yyyy [hh:mm:ss]] [message]
   - /<command> [name/uuid] [banend: mm/dd/yyyy [hh:mm:ss]] [message]
   - /<command> [name/uuid]/[CIDR] [banend: mm/dd/yyyy [hh:mm:ss]] [message]
		 */
		// Check if name. Check if uuid. Check if ip-ipv4 vs. ipv6.
		if (arguments.length < 1) return false;
		
		String preBan = arguments[0];
		int locCIDR = preBan.indexOf('/');
		Boolean hasCIDR = locCIDR > -1; 
		Integer CIDR = (hasCIDR) ? Integer.valueOf(preBan.substring(locCIDR + 1)) : null;
		String toBan = (hasCIDR) ? preBan.substring(0, locCIDR) : preBan;
		String endDate = (arguments.length >= 2 ? arguments[1] : null);
		String endTime = (arguments.length >= 3 ? arguments[2] : null);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat combinedFormat = new SimpleDateFormat("MM/dd/yyyy HH:mms:ss");
		Date banEndDate = null;
		Date banEndTime = null;
		Date banEnd = null;
		int mStart = 1;

		BanStick.getPlugin().debug("preBan: {0}, CIDR? {1}, toBan: {2}, endDate: {3}, endTime: {4}", 
				preBan, CIDR, toBan, endDate, endTime);
		
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
		
		try {
			IPAddress ipcheck = new IPAddressString(toBan).toAddress();
			if (!sender.hasPermission("banstick.ips")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use / view IPs");
				return true;
			}
						
			BSIP exact = !hasCIDR ? BSIP.byIPAddress(ipcheck) : BSIP.byCIDR(ipcheck.toString(), CIDR);
			if (exact == null) {
				// new IP record.
				exact = hasCIDR ? BSIP.create(ipcheck, CIDR) : BSIP.create(ipcheck);
			}
			
			BanResult result = hasCIDR ? BanHandler.doCIDRBan(exact, message, banEnd, true, false) : 
					BanHandler.doIPBan(exact, message, banEnd, true, false);
			
			result.informCommandSender(sender);
			
			return true;
		} catch (IPAddressStringException e) {
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
				}
			} else if (toBan.length() == 36) {
				try {
					playerId = UUID.fromString(toBan);
				} catch (IllegalArgumentException iae) {
					sender.sendMessage(ChatColor.RED + "Unable to process uuid " + ChatColor.DARK_RED + toBan);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to interpret " + ChatColor.DARK_RED + toBan);
			}
			
			if (playerId != null) {
				BanResult result = null;
				
				if (hasCIDR) {
					Player target = Bukkit.getPlayer(playerId);
					
					if (target != null) {
						InetAddress na = target.getAddress().getAddress();
						
						BSIP exact = hasCIDR ? BSIP.byCIDR(na, CIDR) : BSIP.byInetAddress(na);
						if (exact == null) {
							// new IP record.
							exact = hasCIDR ? BSIP.create(na, CIDR) : BSIP.create(na);
						}
						
						result = hasCIDR ? BanHandler.doCIDRBan(exact, message, banEnd, true, false) : 
								BanHandler.doIPBan(exact, message, banEnd, true, false);
						result.informCommandSender(sender);
					}
				}
				
				result = BanHandler.doUUIDBan(playerId, message, banEnd, true);
				result.informCommandSender(sender);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to find " + ChatColor.DARK_RED + toBan);
			}
		}
		return false;
	}
}
