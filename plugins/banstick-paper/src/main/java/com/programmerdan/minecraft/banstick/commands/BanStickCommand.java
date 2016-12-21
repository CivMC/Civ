package com.programmerdan.minecraft.banstick.commands;

import org.bukkit.command.CommandSender;

import inet.ipaddr.IPAddressString;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
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
			
			
			return true;
		} catch (IPAddressStringException e) {
			if (toBan.length() < 16) {
				// user ID
			} if (toBan.length() = 36) {
				// uuid
			}
		}
		return false;
	}

	

}
