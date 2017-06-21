package com.programmerdan.minecraft.banstick.commands;

import java.util.Arrays;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSShare;

import vg.civcraft.mc.namelayer.NameAPI;

public class TakeItBackCommand implements CommandExecutor {

	public static String name = "takeitback";
	
	/**
	 *  <b>takeitback [name/uuid] [IP] [PROXY] [SHARED]</b>
	 *    IP - Allows new bans on this player due to IP matches
	 *    PROXY - Allows new bans on this player due to IPData ban matches
	 *    SHARED - Allows new bans on this player due to Share connection ban matches
	 *   <br>
	 *   <b>takeitback [name/uuid] [name/uuid]</b>
	 *    Immediately unpardons all shares between these two players.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		if (arguments.length < 2) return false;
		
		String toRevoke = arguments[0];
		String secRevoke = arguments[1];
		List<String> revokes = (arguments.length > 1) ? Arrays.asList(Arrays.copyOfRange(arguments, 1, arguments.length)) : null;

		BanStick.getPlugin().debug("toRevoke: {0}, secRevoke: {1}, revokes: {2}", 
				toRevoke, secRevoke, revokes);
		
		UUID playerId = null;
		if (toRevoke.length() <= 16) {
			try {
				playerId = null;
				try {
					playerId = NameAPI.getUUID(toRevoke);
				} catch (NoClassDefFoundError ncde) { }
				
				if (playerId == null) {
					Player match = Bukkit.getPlayer(toRevoke);
					if (match != null) {
						playerId = match.getUniqueId();
					}
				}
			} catch (Exception ee) {
				sender.sendMessage(ChatColor.RED + "Unable to find player " + ChatColor.DARK_RED + toRevoke);
				return true;
			}
		} else if (toRevoke.length() == 36) {
			try {
				playerId = UUID.fromString(toRevoke);
			} catch (IllegalArgumentException iae) {
				sender.sendMessage(ChatColor.RED + "Unable to process uuid " + ChatColor.DARK_RED + toRevoke);
				return true;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Unable to interpret " + ChatColor.DARK_RED + toRevoke);
			return true;
		}
		
		UUID secondPlayerId = null;
		if (secRevoke.length() <= 16) {
			try {
				secondPlayerId = null;
				try {
					secondPlayerId = NameAPI.getUUID(secRevoke);
				} catch (NoClassDefFoundError ncde) { }
				
				if (secondPlayerId == null) {
					Player match = Bukkit.getPlayer(secRevoke);
					if (match != null) {
						secondPlayerId = match.getUniqueId();
					}
				}
			} catch (Exception ee) {
				// not a player, but might be a pardon
			}
		} else if (secRevoke.length() == 36) {
			try {
				secondPlayerId = UUID.fromString(secRevoke);
			} catch (IllegalArgumentException iae) {
				sender.sendMessage(ChatColor.RED + "Unable to process uuid " + ChatColor.DARK_RED + secRevoke);
				return true;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Unable to interpret " + ChatColor.DARK_RED + secRevoke);
			return true;
		}
		
		BSPlayer player = BSPlayer.byUUID(playerId);
		if (secondPlayerId == null) { // single player unpardon.
			boolean match = false;
			for (String pardon : revokes) {
				if ("IP".equalsIgnoreCase(pardon)) {
					if (player.getIPPardonTime() != null) {
						player.setIPPardonTime(null);
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is exposed to future IP bans. Existing bans aren't impacted.");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is already exposed to IP bans.");
					}
					match = true;
				}
				
				if ("PROXY".equalsIgnoreCase(pardon)) {
					if (player.getProxyPardonTime() != null) {
						player.setProxyPardonTime(null);
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is exposed to future Proxy bans. Existing warnings aren't impacted.");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is already exposed to Proxy bans.");
					}
					match = true;
				}
				
				if ("SHARED".equalsIgnoreCase(pardon)) {
					if (player.getSharedPardonTime() != null) {
						player.setSharedPardonTime(null);
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is exposed to future Share warnings/bans. Existing warning/bans aren't impacted.");
					} else {
						sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " is already exposed to Share warnings/bans.");
					}
					match = true;
				}
			}

			if (match) {
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Could not determine what to do.");
				return false;
			}
		} else {
			// unpardon shares between two people
			BSPlayer player2 = BSPlayer.byUUID(secondPlayerId);
			List<BSShare> shares = player.sharesWith(player2);
			int pardonsRevoked = 0;
			if (shares != null && shares.size() > 0) {
				for (BSShare share : shares) {
					if (share.isPardoned()) {
						share.setPardonTime(null);
						pardonsRevoked++;
					}
				}
				if (pardonsRevoked > 0) {
					sender.sendMessage(ChatColor.GREEN + "Revoked pardons for " + pardonsRevoked + " shared sessions");
				} else {
					sender.sendMessage(ChatColor.YELLOW + "Found no shared sessions needing pardon revocation");
				}
			} else {
				sender.sendMessage(ChatColor.YELLOW + "Player " + player.getName() + " does not share any connections with " + player2.getName());
			}
			return true;
		}
	}

}
