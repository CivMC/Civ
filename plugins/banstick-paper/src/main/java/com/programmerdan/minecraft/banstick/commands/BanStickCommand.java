package com.programmerdan.minecraft.banstick.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.containers.BanResult;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSSession;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringException;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import vg.civcraft.mc.namelayer.NameAPI;

/**
 * BanStick! BanStick! Ban all the nerds by name, CIDR, IP, or some combo.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
 *
 */
public class BanStickCommand implements CommandExecutor {

	public static String name = "banstick";
	
	/**
	 * Behavior: If given a name or uuid, bans that uuid with a new ban if none exists for that uuid.
	 *   Returns ban details.
	 *   
	 *   TODO: allow message setting for ban.
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
		 *    - /<command> [ip]
   - /<command> [ip]/[CIDR]
   - /<command> [name/uuid]
   - /<command> [name/uuid]/[CIDR]
		 */
		// Check if name. Check if uuid. Check if ip-ipv4 vs. ipv6.
		if (arguments.length < 1) return false;
		
		String preBan = arguments[0];
		int locCIDR = preBan.indexOf('/');
		Boolean hasCIDR = locCIDR > -1; 
		Integer CIDR = (hasCIDR) ? Integer.valueOf(preBan.substring(locCIDR) + 1) : null;
		String toBan = (hasCIDR) ? preBan.substring(0, locCIDR) : preBan;
		
		try {
			IPAddress ipcheck = new IPAddressString(toBan).toAddress();
			
			BSIP exact = !hasCIDR ? BSIP.byIPAddress(ipcheck) : BSIP.byCIDR(ipcheck.toString(), CIDR);
			if (exact == null) {
				// new IP record.
				exact = hasCIDR ? BSIP.create(ipcheck, CIDR) : BSIP.create(ipcheck);
			}
			
			//BSBan ban = BSBan.createIPBan(exact);
			BanResult result = hasCIDR ? BanHandler.doCIDRBan(exact) : BanHandler.doIPBan(exact);
			
			//if (hasCIDR) {
				// look for other IPs in this subnet, within the db.
				// then find sessions that use then.
				// Ban those users.
			//	BanResult result = BanHandler.doCIDRBan(exact);
			//} else {
				// find sessions that use this IP. 
				// Ban those users.
			//	BanResult result = BanHandler.doIPBan(exact);
			//}
			
			result.informCommandSender(sender);
			
			return true;
		} catch (IPAddressStringException e) {
			// Not an IP address!
			UUID playerId = null;
			if (toBan.length() <= 16) {
				try {
					playerId = NameAPI.getUUID(toBan);
					
					if (playerId == null) {
						Player match = Bukkit.getPlayer(toBan);
						if (match != null) {
							playerId = match.getUniqueId();
						}
					}
				} catch (Exception ee) {
					sender.sendMessage(ChatColor.RED + "Unable to find player " + ChatColor.DARK_RED + toBan);
				}
			} if (toBan.length() == 36) {
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
						
						result = hasCIDR ? BanHandler.doCIDRBan(exact) : BanHandler.doIPBan(exact);
						result.informCommandSender(sender);
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Unable to find " + ChatColor.DARK_RED + toBan);
				}
				
				result = BanHandler.doUUIDBan(playerId);
				result.informCommandSender(sender);
				return true;
			}
		}
		return false;
	}
}
