package com.programmerdan.minecraft.banstick.commands;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSShare;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringException;
import vg.civcraft.mc.namelayer.NameAPI;

public class ForgiveCommand implements CommandExecutor {

	public static String name = "forgive";
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		if (arguments.length < 1) return false;
		/*
		   /<command> [ip]
		   /<command> [ip]/[CIDR]
		   /<command> [name/uuid]
		   /<command> [name/uuid] [BAN] [IP] [PROXY] [SHARED]
		*/
		
		String preForgive = arguments[0];
		int locCIDR = preForgive.indexOf('/');
		Boolean hasCIDR = locCIDR > -1; 
		Integer CIDR = (hasCIDR) ? Integer.valueOf(preForgive.substring(locCIDR + 1)) : null;
		String toForgive = (hasCIDR) ? preForgive.substring(0, locCIDR) : preForgive;
		List<String> pardons = (arguments.length > 1) ? Arrays.asList(Arrays.copyOfRange(arguments, 1, arguments.length)) : null;

		BanStick.getPlugin().debug("preForgive: {0}, CIDR? {1}, toForgive: {2}, pardons: {3}", 
				preForgive, CIDR, toForgive, pardons);
				
		try {
			IPAddress ipcheck = new IPAddressString(toForgive).toAddress();
			if (!sender.hasPermission("banstick.ips")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use / view IPs");
				return true;
			}
			
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
			
			try {
				Bukkit.unbanIP(toForgive);
				BanStick.getPlugin().debug("Also forgave any underlying bukkit ban on IP");
			} catch (Exception e) {
				BanStick.getPlugin().debug("Failed to forgive any underlying bukkit ban on IP");
			}
			
			return true;
		} catch (IPAddressStringException e) {
			// Not an IP address!
			UUID playerId = null;
			if (toForgive.length() <= 16) {
				try {
					playerId = null;
					
					try {
						playerId = NameAPI.getUUID(toForgive);
					} catch (NoClassDefFoundError ncde) { }
					
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
				
				if (pardons == null) { // unban
					if (player.getBan() != null) {
						player.setBan(null);
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is unbanned.");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is not banned.");
					}
					
					try {
						Player underlyingUnban = Bukkit.getPlayer(playerId);
						if (underlyingUnban.isBanned()) {
							//underlyingUnban.setBanned(false); // REMOVED 1.12
							BanList legacyBans = Bukkit.getBanList(Type.NAME);
							legacyBans.pardon(playerId.toString());
							legacyBans.pardon(player.getName());
						}
						BanStick.getPlugin().debug("Also forgave any underlying bukkit ban on uuid / player name");
					} catch (Exception q) {
						BanStick.getPlugin().debug("Failed to forgive any underlying bukkit ban on uuid / player name");
					}

					return true;
				} else {
					boolean match = false;
					for (String pardon : pardons) {
						if ("BAN".equalsIgnoreCase(pardon)) {
							if (player.getBan() != null) {
								player.setBan(null);
								sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is unbanned.");
							} else {
								sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is not banned.");
							}

							try {
								Player underlyingUnban = Bukkit.getPlayer(playerId);
								if (underlyingUnban.isBanned()) {
									//underlyingUnban.setBanned(false); // REMOVED 1.12
									BanList legacyBans = Bukkit.getBanList(Type.NAME);
									legacyBans.pardon(playerId.toString());
									legacyBans.pardon(player.getName());
								}
								BanStick.getPlugin().debug("Also forgave any underlying bukkit ban on uuid / player name");
							} catch (Exception q) {
								BanStick.getPlugin().debug("Failed to forgive any underlying bukkit ban on uuid / player name");
							}
							match = true;
						}
						
						if ("IP".equalsIgnoreCase(pardon)) {
							if (player.getIPPardonTime() == null) {
								player.setIPPardonTime(new Date());
								sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is pardoned from future IP bans. Existing bans aren't impacted.");
							} else {
								sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is already pardoned from IP bans.");
							}
							match = true;
						}
						
						if ("PROXY".equalsIgnoreCase(pardon)) {
							if (player.getProxyPardonTime() == null) {
								player.setProxyPardonTime(new Date());
								sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is pardoned from future Proxy bans. Existing warnings aren't impacted.");
							} else {
								sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is already pardoned from Proxy bans.");
							}
							match = true;
						}
						
						if ("SHARED".equalsIgnoreCase(pardon)) {
							if (player.getSharedPardonTime() == null) {
								player.setSharedPardonTime(new Date());
								sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is pardoned from future Share warnings/bans. Existing warning/bans aren't impacted.");
							} else {
								sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is already pardoned from Share warnings/bans.");
							}
							match = true;
						}
						
					}
					if (match) {
						return true;
					} else if (pardons.size() > 0) {
						toForgive = pardons.get(0);
						UUID playerId2 = null;
						if (toForgive.length() <= 16) {
							try {
								playerId2 = null; 
								
								try {
									playerId2 = NameAPI.getUUID(toForgive);
								} catch (NoClassDefFoundError ncde) { }
								
								if (playerId2 == null) {
									Player mcatch = Bukkit.getPlayer(toForgive);
									if (mcatch != null) {
										playerId2 = mcatch.getUniqueId();
									}
								}
							} catch (Exception ee) {
								sender.sendMessage(ChatColor.RED + "Unable to find player " + ChatColor.DARK_RED + toForgive);
							}
						} else if (toForgive.length() == 36) {
							try {
								playerId2 = UUID.fromString(toForgive);
							} catch (IllegalArgumentException iae) {
								sender.sendMessage(ChatColor.RED + "Unable to process uuid " + ChatColor.DARK_RED + toForgive);
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Unable to interpret " + ChatColor.DARK_RED + toForgive);
						}
						if (playerId2 != null) {
							BSPlayer player2 = BSPlayer.byUUID(playerId2);
							List<BSShare> shares = player.sharesWith(player2);
							int banLifted = 0;
							int pardonsGranted = 0;
							if (shares != null && shares.size() > 0) {
								sender.sendMessage(ChatColor.GREEN + "Checking " + shares.size() + " shared sessions for ones needing pardon");
								boolean alsoUnban = pardons.size() > 1 && "ALL".equalsIgnoreCase(pardons.get(1));
								for (BSShare share : shares) {
									if (alsoUnban) {
										List<BSBan> bans = BSBan.byShare(share, false);
										for (BSBan ban : bans) {
											ban.setBanEndTime(new Date());
											banLifted ++;
										}
									}
									
									if (!share.isPardoned()) {
										share.setPardonTime(new Date());
										pardonsGranted++;
									}
								}
								if (alsoUnban && banLifted > 0) {
									sender.sendMessage(ChatColor.GREEN + "Forgave " + banLifted + " active bans");
								} else if (alsoUnban) {
									sender.sendMessage(ChatColor.YELLOW + "Found no bans due to shared sessions to forgive");
								}
								if (pardonsGranted > 0) {
									sender.sendMessage(ChatColor.GREEN + "Pardoned " + pardonsGranted + " shared sessions");
								} else {
									sender.sendMessage(ChatColor.YELLOW + "Found no shared sessions still needing pardon");
								}
							} else {
								sender.sendMessage(ChatColor.YELLOW + "Player " + player.getName() + " does not share any connections with " + player2.getName());
							}
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "Unrecognized forgiveness: " + pardons + ". Please use BAN, IP, PROXY, or SHARED. Or, another user / BAN. Or none to just unban.");
				}
				return false;
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to find " + ChatColor.DARK_RED + toForgive);
			}
		}
		return false;
	}

}
