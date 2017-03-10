package com.programmerdan.minecraft.banstick.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class DowsingRodCommand  implements CommandExecutor {

	public static String name = "dowsingrod";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		/*
		 * /dowsingrod [CONTINENT|COUNTRY|REGION|STATE|CITY|POSTAL|ZIP|DOMAIN|PROVIDER|REGISTEREDAS|CONNECTION] page# perpage
		 * up to three of the above; page#, perpage self-explain.
		 * if none, defaults to provider / 0 / 20
		 * always does largest >>> smallest in ordering.
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
		int page = 0;
		int perpage = 20;
		ArrayList<String> sorts = new ArrayList<String>();
		boolean hasPage = false;
		
		for (String arg : arguments) {
			String toAdd = null;
			if (arg.equalsIgnoreCase("CONTINENT")) {
				toAdd = "continent";
			} else if (arg.equalsIgnoreCase("COUNTRY")) {
				toAdd = "country";
			} else if (arg.equalsIgnoreCase("REGION") || arg.equalsIgnoreCase("STATE")) {
				toAdd = "region"; 
			} else if (arg.equalsIgnoreCase("CITY")) {
				toAdd = "city";
			} else if (arg.equalsIgnoreCase("POSTAL") || arg.equalsIgnoreCase("ZIP") || arg.equalsIgnoreCase("ZIPCODE")) {
				toAdd = "postal";
			} else if (arg.equalsIgnoreCase("DOMAIN")) {
				toAdd = "domain";
			} else if (arg.equalsIgnoreCase("PROVIDER")) {
				toAdd = "provider";
			} else if (arg.equalsIgnoreCase("REGISTEREDAS") || arg.equalsIgnoreCase("REGISTERED")) {
				toAdd = "registered_as";
			} else if (arg.equalsIgnoreCase("CONNECTION")) {
				toAdd = "connection";
			} else {
				try {
					int val = Integer.parseInt(arg);
					if (hasPage) {
						perpage = val;
					} else {
						page = val;
						hasPage = true;
					}
				} catch (NumberFormatException e) {
				}
			}
			if (toAdd != null && sorts.size() < 3) {
				sorts.add(toAdd);
			}
		}
		if (sorts.isEmpty()) {
			sorts.add("provider");
		}
		
		int[] widths = new int[sorts.size() + 1];
		Arrays.fill(widths, 20);
		ArrayList<String[]> toDisplay = new ArrayList<String[]>(perpage);
		
		StringBuilder subString = new StringBuilder();
		StringBuilder queryString = new StringBuilder();
		queryString.append("SELECT ");
		for (String sort : sorts) {
			subString.append(sort).append(", ");
		}
		queryString.append(subString).append("count(*) FROM bs_ip_data GROUP BY ").append(subString.substring(0, subString.length() - 2))
			.append(" ORDER BY count(*) DESC LIMIT ").append(page * perpage).append(',').append(perpage);
		BanStick.getPlugin().debug("Running query: " + queryString.toString());
		long requestLen = System.currentTimeMillis();
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection(); 
				PreparedStatement complex = connection.prepareStatement(queryString.toString());
				ResultSet results = complex.executeQuery()) {
			while(results.next()) {
				String[] storage = new String[sorts.size() + 2];
				StringBuilder detailCmd = new StringBuilder("/drilldown SUMMARY ");
				for (int i = 0; i <= sorts.size(); i++) {
					storage[i] = i < sorts.size() ? results.getString(i + 1) : Integer.toString(results.getInt(i + 1));
					if (results.wasNull()) {
						storage[i] = "NULL";
					}
					if (i < sorts.size()) {
						detailCmd.append(sorts.get(i)).append(" \"").append(storage[i]).append("\" ");
					}
					if (storage[i].length() > widths[i]) {
						widths[i] = storage[i].length();
					}
				}
				storage[sorts.size() + 1] = detailCmd.toString();
				toDisplay.add(storage);
			}
		} catch (SQLException e) {
			BanStick.getPlugin().severe("Failure to satisfy request at a DB level!", e);
			sender.sendMessage(ChatColor.RED + "There was a database failure, try again later.");
			return true;
		} finally {
			requestLen = System.currentTimeMillis() - requestLen;
		}
		
		if (toDisplay.size() == 0) {
			sender.sendMessage(ChatColor.YELLOW + "No results. " + ChatColor.DARK_GRAY + " took " + requestLen + " ms");
			return true;
		} else {
			StringBuilder formatString = new StringBuilder();
			for (int i = 0; i <= sorts.size(); i++) {
				formatString.append("%").append(widths[i]).append("s ");
			}
			@SuppressWarnings("unchecked")
			ArrayList<String> headers = (ArrayList<String>) sorts.clone();
			headers.add("Count");
			sender.sendMessage(ChatColor.BOLD.toString() + ChatColor.DARK_AQUA.toString() + 
					String.format(formatString.toString(), headers.toArray()) + ChatColor.RESET.toString());
			for (String[] data : toDisplay) {
				TextComponent line = new TextComponent(String.format(formatString.toString(), 
						(Object[]) Arrays.copyOfRange(data, 0, data.length - 1)));
				line.setColor(net.md_5.bungee.api.ChatColor.WHITE);
				line.setBold(true);
				if (sender instanceof Player) {
					line.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, data[data.length - 1]));
					((Player) sender).spigot().sendMessage(line);
				} else {
					sender.sendMessage(line.toLegacyText());
				}
			} 
			if (sender instanceof Player) {
				Player player = (Player) sender;
				TextComponent controls = new TextComponent("Controls ");
				controls.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
				if (page > 0) {
					TextComponent priorPage = new TextComponent("Prior ");
					@SuppressWarnings("unchecked")
					ArrayList<String> cmdH = (ArrayList<String>) sorts.clone();
					cmdH.add((page - 1) + " " + perpage);
					priorPage.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/dowsingrod " + String.format(formatString.toString(), cmdH.toArray())));
					priorPage.setColor(net.md_5.bungee.api.ChatColor.GOLD);
					controls.addExtra(priorPage);
				}
				if (toDisplay.size() == perpage) {
					TextComponent nextPage = new TextComponent("Next ");
					@SuppressWarnings("unchecked")
					ArrayList<String> cmdH = (ArrayList<String>) sorts.clone();
					cmdH.add((page + 1) + " " + perpage);
					nextPage.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/dowsingrod " + String.format(formatString.toString(), cmdH.toArray())));
					nextPage.setColor(net.md_5.bungee.api.ChatColor.GOLD);
					controls.addExtra(nextPage);
				}
				player.spigot().sendMessage(controls);
			}
			sender.sendMessage(ChatColor.DARK_GRAY + "query took " + requestLen + " ms");
			return true;
		}
	}
}
