package com.programmerdan.minecraft.banstick.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSSession;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class DrillDownCommand  implements CommandExecutor {

	public static enum Action {
		PLAYER,
		IPDATASUMMARY,
		SUMMARY,
		IPDATA,
		IP;
		
		public static Action match(String value) {
			if (value != null && value.length() > 0) {
				for (Action action : Action.values()) {
					if (value.toLowerCase().contains(action.name().toLowerCase()))
						return action;
				}
			}
			throw new IllegalArgumentException("Could not find Action match with " + value);
		}
	}
	
	public static String name = "drilldown";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		if (arguments.length < 3) return false;
		/*
		 * /drilldown [SUMMARY|PLAYER|IP|IPDATA|IPDATASUMMARY] [[CONTINENT|COUNTRY|REGION|STATE|CITY|POSTAL|ZIP|DOMAIN|PROVIDER|REGISTEREDAS|CONNECTION] "value"]x3
		 * up to three of the above; unpaginated, so this could get very long
		 * 
		 * 
					" continent TEXT," + 
					" country TEXT," +
					" region TEXT," +
					" city TEXT," +
					" postal TEXT," +
					" lat DOUBLE DEFAULT NULL," +
					" lon DOUBLE DEFAULT NULL," +
					" domain TEXT," +
					" provider TEXT," +
					" registered_as TEXT," +
					" connection TEXT," +
					" proxy FLOAT," +
					" source TEXT," +
					" comment TEXT," + 

		 */
		
		//int page = 0;
		//int perpage = 20;
		ArrayList<String> values = new ArrayList<String>();
		StringBuilder nextValue = new StringBuilder();
		//boolean hasPage = false;
		boolean foundAnchor = false;
		Action preaction = Action.SUMMARY;
		try {
			preaction = Action.match(arguments[0]);
		} catch (IllegalArgumentException iae) {
			return false;
		}
		final Action action = preaction;
		
		for (String arg : Arrays.copyOfRange(arguments, 1, arguments.length)) {
			String toAdd = null;
			if (arg.equals("continent")) {
				if (foundAnchor) { 
					values.add(nextValue.toString());
					nextValue = new StringBuilder();
				}
				toAdd = "continent"; foundAnchor = true;
			} else if (arg.equals("country")) {
				if (foundAnchor) { 
					values.add(nextValue.toString());
					nextValue = new StringBuilder();
				}
				toAdd = "country"; foundAnchor = true;
			} else if (arg.equalsIgnoreCase("region")) {
				if (foundAnchor) { 
					values.add(nextValue.toString());
					nextValue = new StringBuilder();
				}
				toAdd = "region";  foundAnchor = true;
			} else if (arg.equalsIgnoreCase("city")) {
				if (foundAnchor) { 
					values.add(nextValue.toString());
					nextValue = new StringBuilder();
				}
				toAdd = "city"; foundAnchor = true;
			} else if (arg.equalsIgnoreCase("postal")) {
				if (foundAnchor) { 
					values.add(nextValue.toString());
					nextValue = new StringBuilder();
				}
				toAdd = "postal"; foundAnchor = true;
			} else if (arg.equalsIgnoreCase("domain")) {
				if (foundAnchor) { 
					values.add(nextValue.toString());
					nextValue = new StringBuilder();
				}
				toAdd = "domain"; foundAnchor = true;
			} else if (arg.equalsIgnoreCase("provider")) {
				if (foundAnchor) { 
					values.add(nextValue.toString());
					nextValue = new StringBuilder();
				}
				toAdd = "provider"; foundAnchor = true;
			} else if (arg.equalsIgnoreCase("registered_as")) {
				if (foundAnchor) { 
					values.add(nextValue.toString());
					nextValue = new StringBuilder();
				}
				toAdd = "registered_as"; foundAnchor = true;
			} else if (arg.equalsIgnoreCase("connection")) {
				if (foundAnchor) { 
					values.add(nextValue.toString());
					nextValue = new StringBuilder();
				}
				toAdd = "connection"; foundAnchor = true;
			} else {
				nextValue.append(" ").append(arg);
			}
			if (toAdd != null && values.size() < 6) {
				values.add(toAdd);
			}
		}
		if (values.isEmpty()) {
			return false;
		}
		if (foundAnchor) {
			values.add(nextValue.toString());
		}
		
		sender.sendMessage(ChatColor.GRAY + "  " + ChatColor.ITALIC + "Preparing drilldown query...");
		Bukkit.getScheduler().runTaskAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				StringBuilder subString = new StringBuilder();
				StringBuilder queryString = new StringBuilder();
				for (int i = 0; i < values.size(); i += 2) {
					if (values.get(i+1).contains("\"NULL\"")) {
						subString.append(values.get(i)).append(" IS NULL");
					} else {
						subString.append(values.get(i)).append(" = ?");
					}
					subString.append(" AND ");
				}
				queryString.append("SELECT idid, iid FROM bs_ip_data WHERE ").append(subString.toString())
					.append("valid = TRUE ORDER BY create_time DESC");
				
				BanStick.getPlugin().debug("Running query: " + queryString.toString());
				int found = 0;
				long requestLen = System.currentTimeMillis();
				try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection(); 
						PreparedStatement complex = connection.prepareStatement(queryString.toString());) {
					for (int i = 1, j = 1; i < values.size(); i += 2, j ++) {
						String searchTerm = values.get(i).trim();
						if (searchTerm.startsWith("\"") && searchTerm.endsWith("\"")) {
							searchTerm = searchTerm.substring(1, searchTerm.length() - 1);
						}
						if (searchTerm.equals("NULL")) {
							j--;
						} else {
							complex.setString(j, searchTerm);
						}
					}
					Set<BSIP> ips = null;
					if (Action.IP.equals(action) || Action.SUMMARY.equals(action)) {
						ips = new HashSet<BSIP>();
					}
					Set<BSPlayer> players = null;
					if (Action.PLAYER.equals(action) || Action.SUMMARY.equals(action)) {
						players = new HashSet<BSPlayer>();
					}
					Set<BSIPData> datas = null;
					if (Action.SUMMARY.equals(action) || Action.IPDATASUMMARY.equals(action)) {
						datas = new HashSet<BSIPData>();
					}
					Map<Long, Set<BSIP>> dataIPs = null;
					Map<Long, Set<BSPlayer>> dataPlayers = null;
					if (Action.IPDATASUMMARY.equals(action)) {
						dataIPs = new HashMap<Long, Set<BSIP>>();
						dataPlayers = new HashMap<Long, Set<BSPlayer>>();
					}
					try (ResultSet results = complex.executeQuery()) {
						while(results.next()) {
							BSIPData data = BSIPData.byId(results.getLong(1));
							BSIP ip = BSIP.byId(results.getLong(2));
							
							if (Action.IPDATA.equals(action)) {
								StringBuilder detailCmd = new StringBuilder("/lovetap ");
								detailCmd.append(ip.toString());
								
								TextComponent line = new TextComponent("  " + data.toFullString(sender.hasPermission("banstick.ips")));
								line.setColor(net.md_5.bungee.api.ChatColor.WHITE);
								line.setBold(true);
								if (sender instanceof Player && sender.hasPermission("banstick.ips")) {
									line.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, detailCmd.toString()));
									((Player) sender).spigot().sendMessage(line);
								} else {
									sender.sendMessage(line.toLegacyText());
								}
							}
							if (Action.IPDATASUMMARY.equals(action)) {
								datas.add(data);
								Set<BSIP> contained = dataIPs.get(data.getId());
								if (contained == null) {
									contained = new HashSet<BSIP>();
									dataIPs.put(data.getId(), contained);
								}
								contained.add(ip);
							} 
							if (Action.SUMMARY.equals(action)) {
								datas.add(data);
								ips.add(ip);
							} 
							if (Action.SUMMARY.equals(action) || Action.IPDATASUMMARY.equals(action) || Action.PLAYER.equals(action)) {
								List<BSSession> sessions = BSSession.byIP(ip);
								if (sessions != null) {
									for (BSSession session : sessions) {
										if (Action.PLAYER.equals(action) || Action.SUMMARY.equals(action)) {
											players.add(session.getPlayer());
										} else { // IPDATASUMMARY
											Set<BSPlayer> playerSet = dataPlayers.get(data.getId());
											if (playerSet == null) {
												playerSet = new HashSet<BSPlayer>();
												dataPlayers.put(data.getId(), playerSet);
											}
											playerSet.add(session.getPlayer());
										}
									}
								}
							}
							found++;
						}
					}
					if (Action.SUMMARY.equals(action)) {
						StringBuffer sb = new StringBuffer();
						for (int i = 1; i < arguments.length; i++) {
							sb.append(" ").append(arguments[i]);
						}
						TextComponent summary = new TextComponent("Found ");
							summary.setColor(net.md_5.bungee.api.ChatColor.AQUA);
						TextComponent pExt = new TextComponent(players.size() + " players, ");
							pExt.setColor(net.md_5.bungee.api.ChatColor.WHITE);
							pExt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/drilldown PLAYER" + sb.toString()));
						TextComponent dExt = new TextComponent(datas.size() + " subnet records, ");
							dExt.setColor(net.md_5.bungee.api.ChatColor.WHITE);
							dExt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/drilldown IPDATA" + sb.toString()));
						TextComponent iExt = new TextComponent(ips.size() + " ips ");
							iExt.setColor(net.md_5.bungee.api.ChatColor.WHITE);
							iExt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/drilldown IP" + sb.toString()));
						summary.addExtra(pExt);
						summary.addExtra(dExt);
						summary.addExtra(iExt);
						TextComponent orDetail = new TextComponent("  [get breakdown by subnet/proxy]");
							orDetail.setColor(net.md_5.bungee.api.ChatColor.GOLD);
							orDetail.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/drilldown IPDATASUMMARY" + sb.toString()));
						
						if (sender instanceof Player) {
							((Player) sender).spigot().sendMessage(summary, orDetail);
						} else {
							sender.sendMessage(summary.toLegacyText());
						}
					} else if (Action.IP.equals(action)) {
						for (BSIP ip : ips) {
							StringBuilder detailCmd = new StringBuilder("/lovetap ");
							detailCmd.append(ip.toString());
							
							TextComponent line = new TextComponent("  " + ip.toFullString(sender.hasPermission("banstick.ips")));
							line.setColor(net.md_5.bungee.api.ChatColor.WHITE);
							if (sender instanceof Player && sender.hasPermission("banstick.ips")) {
								line.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, detailCmd.toString()));
								((Player) sender).spigot().sendMessage(line);
							} else {
								sender.sendMessage(line.toLegacyText());
							}
						}
					} else if (Action.PLAYER.equals(action)) {
						for (BSPlayer player : players) {
							StringBuilder detailCmd = new StringBuilder("/lovetap ");
							detailCmd.append(player.getUUID().toString());
							
							TextComponent line = new TextComponent("  " + player.toString());
							line.setColor(net.md_5.bungee.api.ChatColor.WHITE);
							if (sender instanceof Player) {
								line.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, detailCmd.toString()));
								((Player) sender).spigot().sendMessage(line);
							} else {
								sender.sendMessage(line.toLegacyText());
							}
						}
					} else if (Action.IPDATASUMMARY.equals(action)) {
						for (BSIPData data : datas) {
							
							BaseComponent[] preline = TextComponent.fromLegacyText(data.toFullString(sender.hasPermission("banstick.ips")));
							TextComponent bline = new TextComponent("Subnet Record: ");
							bline.setColor(net.md_5.bungee.api.ChatColor.AQUA);
							bline.setBold(true);
							bline.setExtra(Arrays.asList(preline));
							if (sender instanceof Player) {
								((Player) sender).spigot().sendMessage(bline);
							} else {
								sender.sendMessage(bline.toLegacyText());
							}
							if (dataIPs.get(data.getId()) != null) {
								sender.sendMessage(ChatColor.AQUA + "IP Details:");
								for (BSIP ip : dataIPs.get(data.getId())) {
									StringBuilder detailCmd = new StringBuilder("/lovetap ");
									detailCmd.append(ip.toString());
									
									TextComponent line = new TextComponent("  " + ip.toFullString(sender.hasPermission("banstick.ips")));
									line.setColor(net.md_5.bungee.api.ChatColor.WHITE);
									if (sender instanceof Player && sender.hasPermission("banstick.ips")) {
										line.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, detailCmd.toString()));
										((Player) sender).spigot().sendMessage(line);
									} else {
										sender.sendMessage(line.toLegacyText());
									}
								}
							}
							if (dataPlayers.get(data.getId()) != null) {
								sender.sendMessage(ChatColor.AQUA + "Player Details:");
								for (BSPlayer player : dataPlayers.get(data.getId())) {
									StringBuilder detailCmd = new StringBuilder("/lovetap ");
									detailCmd.append(player.getUUID().toString());
									
									TextComponent line = new TextComponent("  " + player.toString());
									line.setColor(net.md_5.bungee.api.ChatColor.WHITE);
									if (sender instanceof Player) {
										line.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, detailCmd.toString()));
										((Player) sender).spigot().sendMessage(line);
									} else {
										sender.sendMessage(line.toLegacyText());
									}
								}
							}
						}
					} // IPDATA is done in-stream.
				} catch (SQLException e) {
					BanStick.getPlugin().severe("Failure to satisfy request at a DB level!", e);
					sender.sendMessage(ChatColor.RED + "There was a database failure, try again later.");
					return;
				} finally {
					requestLen = System.currentTimeMillis() - requestLen;
				}
				
				if (found == 0) {
					sender.sendMessage(ChatColor.YELLOW + "No results. " + ChatColor.DARK_GRAY + " took " + requestLen + " ms");
				} else {
					sender.sendMessage(ChatColor.DARK_GRAY + Integer.toString(found) + " results in " + requestLen + " ms");
				}
			}
		});
		return true;
	}
}
