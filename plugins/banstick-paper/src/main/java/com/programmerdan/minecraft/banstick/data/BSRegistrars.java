package com.programmerdan.minecraft.banstick.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

/**
 * Represents a set of banned registrars
 *
 */
public class BSRegistrars {

	private Set<String> registrars;

	public BSRegistrars() {
		registrars = loadRegistrarsFromDB();
	}
	
	public boolean isBanned(BSIPData data) {
		if (data == null) {
			return false;
		}
		return registrars.contains(data.getRegisteredAs());
	}
	
	public void banRegistrar(BSIPData data) {
		if (registrars.contains(data.getRegisteredAs())) {
			return;
		}
		if (data.getRegisteredAs() == null || data.getRegisteredAs().isEmpty()) {
			return;
		}
		registrars.add(data.getRegisteredAs());
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement insertRegistrar = connection
						.prepareStatement("insert into bs_banned_registrars (registered_as) values(?);");) {
			insertRegistrar.setString(1, data.getRegisteredAs());
			insertRegistrar.execute();
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Insertion of banned registrar failed", se);
		}
	}

	private Set<String> loadRegistrarsFromDB() {
		Set <String> result = new HashSet<>();
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
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
