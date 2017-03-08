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
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class DrillDownCommand  implements CommandExecutor {

	public static String name = "drilldown";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		if (arguments.length < 2) return false;
		/*
		 * /drilldown [[CONTINENT|COUNTRY|REGION|STATE|CITY|POSTAL|ZIP|DOMAIN|PROVIDER|REGISTEREDAS|CONNECTION] "value"]x3
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
		
		int page = 0;
		int perpage = 20;
		ArrayList<String> values = new ArrayList<String>();
		StringBuilder nextValue = new StringBuilder();
		boolean hasPage = false;
		boolean foundAnchor = false;
		
		for (String arg : arguments) {
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
			try (ResultSet results = complex.executeQuery()) {
				while(results.next()) {
					BSIPData data = BSIPData.byId(results.getLong(1));
					BSIP ip = BSIP.byId(results.getLong(2));
					
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
					found++;
				}
			}
		} catch (SQLException e) {
			BanStick.getPlugin().severe("Failure to satisfy request at a DB level!", e);
			sender.sendMessage(ChatColor.RED + "There was a database failure, try again later.");
			return true;
		} finally {
			requestLen = System.currentTimeMillis() - requestLen;
		}
		
		if (found == 0) {
			sender.sendMessage(ChatColor.YELLOW + "No results. " + ChatColor.DARK_GRAY + " took " + requestLen + " ms");
			return true;
		} else {
			sender.sendMessage(ChatColor.DARK_GRAY + Integer.toString(found) + " results in " + requestLen + " ms");
			return true;
		}
	}
}
