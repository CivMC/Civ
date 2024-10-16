package com.programmerdan.minecraft.banstick.data;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Represents a set of banned registrars
 *
 * @author Maxopoly
 */
public class BSRegistrars {

	private Set<String> registrars;

	public BSRegistrars() {
		registrars = loadRegistrarsFromDB();
		BanStick.getPlugin().getLogger().info("Loaded " + registrars + " banned registrars from database");
	}
	
	/**
	 * Checks if a particular Proxy's Registrar has been banned.
	 * 
	 * @param data the Proxy to check
	 * @return true if banned, false if not banned.
	 */
	public boolean isBanned(BSIPData data) {
		if (data == null) {
			return false;
		}
		return registrars.contains(data.getRegisteredAs());
	}
	
	/**
	 * Ban the registrar of a particular Proxy.
	 * @param data the Proxy whose registrar to ban.
	 */
	public void banRegistrar(BSIPData data) {
		if (registrars.contains(data.getRegisteredAs())) {
			return;
		}
		if (data.getRegisteredAs() == null || data.getRegisteredAs().isEmpty()) {
			return;
		}
		registrars.add(data.getRegisteredAs());
		try (Connection connection = BanStickDatabaseHandler.getInstanceData().getConnection();
			 PreparedStatement insertRegistrar = connection
						.prepareStatement("insert into bs_banned_registrars (registered_as) values(?);");) {
			insertRegistrar.setString(1, data.getRegisteredAs());
			insertRegistrar.execute();
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Insertion of banned registrar failed", se);
		}
	}
	
	/**
	 * Given a particular Proxy, unban its registrar
	 * @param data the Proxy whose registrar to unban.
	 */
	public void unbanRegistrar(BSIPData data) {
		if (data.getRegisteredAs() == null) {
			return;
		}
		registrars.remove(data.getRegisteredAs());
		try (Connection connection = BanStickDatabaseHandler.getInstanceData().getConnection();
			 PreparedStatement insertRegistrar = connection
						.prepareStatement("delete from bs_banned_registrars where registered_as = ?");) {
			insertRegistrar.setString(1, data.getRegisteredAs());
			insertRegistrar.execute();
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Deletion of banned registrar failed", se);
		}
	}
	
	/**
	 * After creating an IP data entry this method checks whether the 
	 * registrar is banned and removes all active players with this registrar if neccessary
	 * 
	 * <p>Note that this will create a unique UUID ban for online players, in addition to the
	 * Registrar ban, so if you're "undoing" this later, you'll have to manually unban players 
	 * who use this Registrar.
	 * 
	 * @param data IP data created just now
	 */
	public void checkAndCleanup(BSIPData data) {
		if (!registrars.contains(data.getRegisteredAs())) {
			return;
		}
		for (BSSession session : BSSession.byIP(data.getIP())) {
			if (!session.isEnded()) {
				//dont always reban people who logged in on a vpn once in the past
				Player player = Bukkit.getPlayer(session.getPlayer().getUUID());
				if (player == null) {
					BanStick.getPlugin().info("Session " + session.toFullString(true) + " was active, "
							+ "but did not have an active player");
					continue;
				}
				BanHandler.doUUIDBan(player.getUniqueId(), true);
				BanStick.getPlugin().info("Banning " + player.getName() + " for "
						+ "blacklisted provider " + data.getRegisteredAs());
				BanStick.getPlugin().getEventHandler().doKickWithCheckup(player.getUniqueId(),
						session.getPlayer().getBan());
			}
		}
	}

	private Set<String> loadRegistrarsFromDB() {
		Set<String> result = new HashSet<>();
		try (Connection connection = BanStickDatabaseHandler.getInstanceData().getConnection();
			 PreparedStatement loadSet = connection
						.prepareStatement("SELECT registered_as FROM bs_banned_registrars;");) {
			try (ResultSet rs = loadSet.executeQuery();) {
				while (rs.next()) {
					result.add(rs.getString(1));
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Retrieval of banned registrars failed", se);
		}
		return result;
	}

}
