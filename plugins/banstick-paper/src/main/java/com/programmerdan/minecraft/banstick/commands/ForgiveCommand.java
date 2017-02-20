package com.programmerdan.minecraft.banstick.commands;

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
import com.programmerdan.minecraft.banstick.data.BSPlayer;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringException;
import vg.civcraft.mc.namelayer.NameAPI;

public class ForgiveCommand implements CommandExecutor {

	public static String name = "forgive";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		if (arguments.length < 1) return false;
		/*
		   /<command> [ip]
		   /<command> [ip]/[CIDR]
		   /<command> [name/uuid]
		   /<command> [name/uuid] [IP|VPN|SHARED]
		*/
		
		String preForgive = arguments[0];
		int locCIDR = preForgive.indexOf('/');
		Boolean hasCIDR = locCIDR > -1; 
		Integer CIDR = (hasCIDR) ? Integer.valueOf(preForgive.substring(locCIDR + 1)) : null;
		String toForgive = (hasCIDR) ? preForgive.substring(0, locCIDR) : preForgive;
		String pardon = (arguments.length >= 2 ? arguments[1] : null);

		BanStick.getPlugin().debug("preForgive: {0}, CIDR? {1}, toForgive: {2}, pardon: {3}", 
				preForgive, CIDR, toForgive, pardon);
				
		try {
			IPAddress ipcheck = new IPAddressString(toForgive).toAddress();
			
			BSIP exact = !hasCIDR ? BSIP.byIPAddress(ipcheck) : BSIP.byCIDR(ipcheck.toString(), CIDR);
			if (exact == null) {
				sender.sendMessage(ChatColor.RED + "Can't find " + (hasCIDR ? ipcheck.toString() + "/" + CIDR : ipcheck.toString()));
				return true;
			}
			
			List<BSBan> bans = BSBan.byIP(exact, false);
			
			int banLifted = 0;
			for (BSBan ban : bans) {
				ban.setBanEndTime(new Date());
				banLifted ++;
			}
			
			sender.sendMessage(ChatColor.GREEN + "Forgave " + banLifted + " active bans");
			
			return true;
		} catch (IPAddressStringException e) {
			// Not an IP address!
			UUID playerId = null;
			if (toForgive.length() <= 16) {
				try {
					playerId = NameAPI.getUUID(toForgive);
					
					if (playerId == null) {
						Player match = Bukkit.getPlayer(toForgive);
						if (match != null) {
							playerId = match.getUniqueId();
						}
					}
				} catch (Exception ee) {
					sender.sendMessage(ChatColor.RED + "Unable to find player " + ChatColor.DARK_RED + toForgive);
				}
			} else if (toForgive.length() == 36) {
				try {
					playerId = UUID.fromString(toForgive);
				} catch (IllegalArgumentException iae) {
					sender.sendMessage(ChatColor.RED + "Unable to process uuid " + ChatColor.DARK_RED + toForgive);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to interpret " + ChatColor.DARK_RED + toForgive);
			}
			
			if (playerId != null) {
				BSPlayer player = BSPlayer.byUUID(playerId);
				
				if (pardon == null) { // unban
					if (player.getBan() != null) {
						player.setBan(null);
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is unbanned.");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is not banned.");
					}
					return true;
				}
				
				if ("IP".equalsIgnoreCase(pardon)) {
					if (player.getIPPardonTime() == null) {
						player.setIPPardonTime(new Date());
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is pardoned from future IP bans. Existing bans aren't impacted.");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is already pardoned from IP bans.");
					}
					return true;
				}
				
				if ("VPN".equalsIgnoreCase(pardon)) {
					if (player.getVpnPardonTime() == null) {
						player.setVpnPardonTime(new Date());
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is pardoned from future Vpn warning. Existing warnings aren't impacted.");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is already pardoned from Vpn warnings.");
					}
					return true;
				}
				
				if ("SHARED".equalsIgnoreCase(pardon)) {
					if (player.getSharedPardonTime() == null) {
						player.setSharedPardonTime(new Date());
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is pardoned from future Share warnings/bans. Existing warning/bans aren't impacted.");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is already pardoned from Share warnings/bans.");
					}
					return true;
				}
				
				sender.sendMessage(ChatColor.RED + "Unrecognized forgiveness: " + pardon + ". Pleaes use IP, VPN, or SHARED. Or none to just unban.");
				
				return false;
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to find " + ChatColor.DARK_RED + toForgive);
			}
		}
		return false;
	}

}
