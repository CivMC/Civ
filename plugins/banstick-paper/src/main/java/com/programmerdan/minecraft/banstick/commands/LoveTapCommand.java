package com.programmerdan.minecraft.banstick.commands;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSSession;
import com.programmerdan.minecraft.banstick.data.BSShare;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;

/**
 * Sometimes you need some love, so lovetap
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class LoveTapCommand  implements CommandExecutor {

	public static String name = "lovetap";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		if (arguments.length < 1) {
			return false;
		}
		
		String preTap = arguments[0];
		int locCIDR = preTap.indexOf('/');
		Boolean hasCIDR = locCIDR > -1; 
		Integer cidr = (hasCIDR) ? Integer.valueOf(preTap.substring(locCIDR + 1)) : null;
		String toTap = (hasCIDR) ? preTap.substring(0, locCIDR) : preTap;
		Integer sessionLimit = Integer.MAX_VALUE;
		try {
			sessionLimit = Integer.valueOf(arguments[1]);
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
			sender.sendMessage(ChatColor.RED + "You didn't enter a number of sessions properly, defaulting to all");
		}

		BanStick.getPlugin().debug("preTap: {0}, CIDR? {1}, toTap: {2}", 
				preTap, cidr, toTap);
				
		try {
			IPAddress ipcheck = new IPAddressString(toTap).toAddress();
			if (ipcheck == null) {
				throw new IPAddressStringException("Null -- but no error?");
			}
			if (!sender.hasPermission("banstick.ips")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use / view IPs");
				return true;
			}
			if (hasCIDR) { // MOAR, but aggregates.
				sender.sendMessage(ChatColor.GREEN + "Please wait, searching for all contained IP records");
				Bukkit.getScheduler().runTaskAsynchronously(BanStick.getPlugin(), new Runnable() {
					@Override
					public void run() {
						List<BSIP> contains = BSIP.allContained(ipcheck, cidr);
						if (contains != null && !contains.isEmpty()) {
							sender.sendMessage(ChatColor.GREEN + "Found " + contains.size() 
									+ " contained by " + ChatColor.WHITE + ipcheck.toString() + "/" + cidr);
							for (BSIP bsip : contains) {
								List<BSBan> bans = BSBan.byIP(bsip, false);
								List<BSSession> sessions = BSSession.byIP(bsip);
								Set<Long> playerIds = new HashSet<>();
								List<BSPlayer> players = new ArrayList<>();
								List<BSBan> playerBans = new ArrayList<>();
								StringBuilder playerList = new StringBuilder();
								StringBuilder playerBanList = new StringBuilder();
								for (BSSession session : sessions) {
									BSPlayer player = session.getPlayer();
									if (playerIds.contains(player.getId())) {
										continue;
									}
									playerIds.add(player.getId());
									playerList.append(player.getName()).append(", ");
									players.add(player);
									if (player.getBan() != null) {
										playerBans.add(player.getBan());
										playerBanList.append(player.getName()).append(", ");
									}
								}
								BSIPData proxy = BSIPData.byExactIP(bsip);
								
								TextComponent ipBase = new TextComponent("IP ");
									ipBase.setColor(net.md_5.bungee.api.ChatColor.BLUE);
								TextComponent ipStr = new TextComponent(bsip.toString());
									ipStr.setColor(net.md_5.bungee.api.ChatColor.WHITE);
									ipStr.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
											new Text("Click to lovetap this IP")));
									ipStr.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
											"/lovetap " + bsip.toString()));
								ipBase.addExtra(ipStr);
								// TODO: Add a hover and clickable that issues a lovetap for this IP specifically. 
								//sb.append(ChatColor.BLUE).append("IP ").append(ChatColor.WHITE).append(bsip.toString()).toString());
								
								TextComponent ipBanBase = new TextComponent(" IPBans: ");
									ipBanBase.setColor(net.md_5.bungee.api.ChatColor.AQUA);
								TextComponent ipBanStr = new TextComponent(Integer.toString(bans == null ? 0
										: bans.size()));
									ipBanStr.setColor(net.md_5.bungee.api.ChatColor.WHITE);
								ipBanBase.addExtra(ipBanStr);
								ipBase.addExtra(ipBanBase);
								//sb.append(ChatColor.AQUA).append(" IPBans: ").append(ChatColor.WHITE).append(bans == null ? 0 : bans.size());
								
								TextComponent sessionBase = new TextComponent(" Sessions: ");
									sessionBase.setColor(net.md_5.bungee.api.ChatColor.AQUA);
								TextComponent sessionStr = new TextComponent(Integer.toString(sessions == null ? 0 
										: sessions.size()));
									sessionStr.setColor(net.md_5.bungee.api.ChatColor.WHITE);
								sessionBase.addExtra(sessionStr);
								ipBase.addExtra(sessionBase);
								//sb.append(ChatColor.AQUA).append(" Sessions: ").append(ChatColor.WHITE).append(sessions == null ? 0 : sessions.size());
								
								// TODO: Add a hover showing all the player's names
								TextComponent playerBase = new TextComponent(" Players: ");
									playerBase.setColor(net.md_5.bungee.api.ChatColor.AQUA);
								TextComponent playerStr = new TextComponent(Integer.toString(players == null ? 0 
										: players.size()));
									playerStr.setColor(net.md_5.bungee.api.ChatColor.WHITE);
									if (!players.isEmpty()) {
										playerStr.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
												new Text((playerList.substring(0, playerList.length() - 2)))));
									}
								playerBase.addExtra(playerStr);
								ipBase.addExtra(playerBase);
								//sb.append(ChatColor.AQUA).append(" Players: ").append(ChatColor.WHITE).append(players == null ? 0 : players.size());

								TextComponent plBanBase = new TextComponent(" PlayerBans: ");
									plBanBase.setColor(net.md_5.bungee.api.ChatColor.AQUA);
								TextComponent plBanStr = new TextComponent(Integer.toString(playerBans == null ? 0 
										: playerBans.size()));
									plBanStr.setColor(net.md_5.bungee.api.ChatColor.WHITE);
									if (!playerBans.isEmpty()) {
										plBanStr.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
												new Text((playerBanList.substring(0,  playerBanList.length() - 2)))));
									}
								plBanBase.addExtra(plBanStr);
								ipBase.addExtra(plBanBase);
								//sb.append(ChatColor.AQUA).append(" PlayerBans: ").append(ChatColor.WHITE).append(playerBans == null ? 0 : playerBans.size());
								
								if (proxy != null) {
									TextComponent proxyBase = new TextComponent("\n   " + proxy.toString());
										proxyBase.setColor(net.md_5.bungee.api.ChatColor.WHITE);
										proxyBase.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
												new Text(("View other players in same city"))));
										proxyBase.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
												"/drilldown PLAYER country \"" + proxy.getCountry() 
												+ "\" region \"" + proxy.getRegion() + "\" city \"" 
												+ proxy.getCity() + "\""));
									ipBase.addExtra(proxyBase);
								}
								//if (proxy != null) sb.append("\n   ").append(proxy.toString());
								
								if (sender instanceof Player) {
									((Player) sender).spigot().sendMessage(ipBase);
								} else {
									sender.sendMessage(ipBase.toLegacyText());
								}
								//sender.sendMessage(sb.toString());
							}
						} else {
							sender.sendMessage(ChatColor.RED + "No IPs found contained by " 
									+ ChatColor.WHITE + ipcheck.toString() + "/" + cidr);
						}
					}
				});
			}
			// LESS, but details
			
			BSIP exact = !hasCIDR ? BSIP.byIPAddress(ipcheck) : BSIP.byCIDR(ipcheck.toString(), cidr);
			if (exact == null) {
				sender.sendMessage(ChatColor.RED + "Can't find exact " + (hasCIDR ? ipcheck.toString() 
						+ "/" + cidr : ipcheck.toString()));
				return true;
			}
			
			List<BSBan> bans = BSBan.byIP(exact, false);
			List<BSSession> sessions = BSSession.byIP(exact);
			List<BSIPData> proxies = BSIPData.allByIP(exact);
			
			StringBuilder sb = new StringBuilder();
			sb.append(ChatColor.BLUE).append("Data for IP ").append(ChatColor.WHITE).append(exact.toString());
			sb.append('\n');
			sb.append(ChatColor.BLUE).append("\nDetail Data: \n");
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
				for (int i = 0; i <= sessionLimit - 1; i++) {
					try {
						sb.append(ChatColor.WHITE).append("  ").append(sessions.get(i).toString()).append('\n');
					} catch (IndexOutOfBoundsException exception) {
						break;
					}
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
					playerId = null;
					try {
						playerId = NameAPI.getUUID(toTap);
					} catch (NoClassDefFoundError ncde) { }
					
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

				if (hasCIDR) {
					if (!sender.hasPermission("banstick.ips")) {
						sender.sendMessage(ChatColor.RED + "You don't have permission to use / view IPs");
						return true;
					}
					
					BSSession latest = player.getLatestSession();
					if (latest != null) {
						BSIP latestIP = latest.getIP();
						if (latestIP == null) {
							sender.sendMessage("CIDR " + cidr + " of latest IP requested but player has no latest IP");
							return true;
						}
						String ipRequest = latestIP.toString();
						if (ipRequest.indexOf('/') > -1) {
							ipRequest = ipRequest.substring(0, ipRequest.indexOf('/'));
						}
						return onCommand(sender, cmd, cmdString, new String[] {ipRequest + "/" + cidr});
					}
				}
				
				BSBan ban = player.getBan();
				List<BSSession> history = player.getAllSessions();
				BSSession latest = player.getLatestSession();
				
				BSIPData latestProxy = latest != null ? BSIPData.byContainsIP(latest.getIP()) : null;
				
				List<BSShare> shares = player.getAllShares();
				
				StringBuilder sb = new StringBuilder();
				if (history != null) {
					Collections.reverse(history);
					sb.append(ChatColor.BLUE).append("Session History: ").append(ChatColor.DARK_AQUA)
						.append("(First Join: ").append(ChatColor.WHITE).append(player.getFirstAdd())
						.append(ChatColor.DARK_AQUA).append(")\n");
					for (int i = 0; i <= sessionLimit - 1; i++) {
						try {
							sb.append(ChatColor.WHITE + "  " + history.get(i).toFullString(
									sender.hasPermission("banstick.ips")) + "\n");
						} catch (IndexOutOfBoundsException exception) {
							break;
						}
					}
					sb.append("\n");
				}
				if (latest != null) {
					sb.append(ChatColor.GREEN + "Most Recent Session: \n");
					sb.append(ChatColor.WHITE + "  " + latest.toFullString(
							sender.hasPermission("banstick.ips")) + "\n");
				}
				
				if (shares != null && !shares.isEmpty()) {
					sb.append(ChatColor.BLUE).append("Share History: ").append("\n");
					for (BSShare histShare : shares) {
						sb.append(ChatColor.WHITE + "  " 
								+ histShare.toFullString(sender.hasPermission("banstick.ips")) + "\n");
					}
					sb.append("\n");					
				}
				
				if (latestProxy != null && sender.hasPermission("banstick.ips")) {
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
				if (player.getIPPardonTime() == null && player.getProxyPardonTime() == null 
						&& player.getSharedPardonTime() == null) {
					sb.append(ChatColor.RED + "  Nothing\n");
				}
				
				sb.append("\n");
				sb.append(ChatColor.WHITE + player.getName() + " [" + player.getUUID() + "]");
				
				sender.sendMessage(sb.toString());
				if (latestProxy != null && sender instanceof Player) {
					TextComponent proxyBase = new TextComponent("\n   View other players in same city");
					proxyBase.setColor(net.md_5.bungee.api.ChatColor.GOLD);
					proxyBase.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
							"/drilldown PLAYER country \"" + latestProxy.getCountry() + "\" region \""
							+ latestProxy.getRegion() + "\" city \"" + latestProxy.getCity() + "\""));
					((Player) sender).spigot().sendMessage(proxyBase);
				}
				
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to find " + ChatColor.DARK_RED + toTap);
			}
		}

		return false;
	}
}
