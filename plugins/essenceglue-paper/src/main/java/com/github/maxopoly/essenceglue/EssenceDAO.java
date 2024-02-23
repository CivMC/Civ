package com.github.maxopoly.essenceglue;

import com.programmerdan.minecraft.banstick.data.BSPlayer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class EssenceDAO {

	private final EssenceGluePlugin plugin;
	private final ManagedDatasource db;

	public EssenceDAO(EssenceGluePlugin plugin, ManagedDatasource db) {
		this.plugin = plugin;
		this.db = db;
		registerMigrations();
	}

	public boolean update() {
		return db.updateDatabase();
	}

	public void registerMigrations() {
		// import legacy mana
		db.registerMigration(1, false,
				"CREATE TABLE IF NOT EXISTS manaStats (ownerId int(11) NOT NULL, streak int(11) NOT NULL, "
						+ "lastDay bigint(20) NOT NULL, PRIMARY KEY (ownerId))",
				" CREATE TABLE IF NOT EXISTS manaOwners (id int(11) NOT NULL AUTO_INCREMENT, "
						+ "foreignId int(11) NOT NULL, foreignIdType tinyint(4) NOT NULL, "
						+ "PRIMARY KEY (foreignId, foreignIdType), UNIQUE KEY `id` (id))");
		db.registerMigration(2, false, () -> {
			StreakManager streakMan = plugin.getStreakManager();
			try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
					"select o.foreignId, s.lastDay, s.streak from manaStats s inner join manaOwners o "
							+ "on s.ownerId = o.id where o.foreignIdType = 0");
					ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int banStickId = rs.getInt(1);
					long timeStamp = rs.getLong(2);
					int streak = rs.getInt(3);
					BSPlayer bsPlayer = BSPlayer.byId(banStickId);
					if (bsPlayer != null) {
						streakMan.setStreakRaw(streak, timeStamp, bsPlayer.getUUID());
					}
				}
			}
			return true;
		});
	}

}
