/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class GearblockSource {
	private static final String countAllScript = "SELECT COUNT(*) FROM cg_gearblock";
	private static final String selectAllScript = "SELECT * FROM cg_gearblock ORDER BY GearblockId";
	private static final String insertScript = "INSERT INTO cg_gearblock (WorldId, X, Y, Z, Timer, TimerOperation) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String updateScript = "UPDATE cg_gearblock SET WorldId = ?, X = ?, Y = ?, Z = ?, Timer = ?, TimerOperation = ? WHERE GearblockId = ?";
	private static final String deleteScript = "DELETE FROM cg_gearblock WHERE GearblockId = ?";

	private final SqlDatabase _db;

	public GearblockSource(SqlDatabase db) {
		_db = db;
	}

	public int countAll() throws SQLException {
		try (PreparedStatement sql = _db.prepareStatement(countAllScript)) {
			try (ResultSet rs = sql.executeQuery()) {
				if (rs.next()) return rs.getInt(1);
			}
		}

		return 0;
	}

	public List<GearblockInfo> selectAll() throws SQLException {
		ArrayList<GearblockInfo> list = new ArrayList<>();

		try (PreparedStatement sql = _db.prepareStatement(selectAllScript)) {
			try (ResultSet rs = sql.executeQuery()) {
				while (rs.next()) {
					GearblockInfo info = new GearblockInfo();

					info.GearblockId = rs.getInt("GearblockId");
					info.WorldId = rs.getString("WorldId");
					info.X = rs.getInt("X");
					info.Y = rs.getInt("Y");
					info.Z = rs.getInt("Z");

					info.Timer = rs.getInt("Timer");
					if (rs.wasNull()) info.Timer = null;

					info.TimerOperation = rs.getInt("TimerOperation");
					if (rs.wasNull()) info.TimerOperation = null;

					list.add(info);
				}
			}
		}

		return list;
	}

	public void insert(GearblockInfo info) throws SQLException {
		try (PreparedStatement sql = _db.prepareStatementWithReturn(insertScript)) {
			sql.setString(1, info.WorldId);
			sql.setInt(2, info.X);
			sql.setInt(3, info.Y);
			sql.setInt(4, info.Z);

			if (info.Timer != null)
				sql.setInt(5, info.Timer);
			else
				sql.setNull(5, Types.INTEGER);

			if (info.TimerOperation != null)
				sql.setInt(6, info.TimerOperation);
			else
				sql.setNull(6, Types.INTEGER);

			sql.executeUpdate();

			try (ResultSet rs = sql.getGeneratedKeys()) {
				rs.next();
				info.GearblockId = rs.getInt(1);
			}
		}
	}

	public void update(GearblockInfo info) throws SQLException {
		try (PreparedStatement sql = _db.prepareStatement(updateScript)) {
			sql.setString(1, info.WorldId);
			sql.setInt(2, info.X);
			sql.setInt(3, info.Y);
			sql.setInt(4, info.Z);

			if (info.Timer != null)
				sql.setInt(5, info.Timer);
			else
				sql.setNull(5, Types.INTEGER);

			if (info.TimerOperation != null)
				sql.setInt(6, info.TimerOperation);
			else
				sql.setNull(6, Types.INTEGER);

			sql.setInt(7, info.GearblockId);

			sql.executeUpdate();
		}
	}

	public void delete(int gear_id) throws SQLException {
		try (PreparedStatement sql = _db.prepareStatement(deleteScript)) {
			sql.setInt(1, gear_id);

			sql.executeUpdate();
		}
	}
}
