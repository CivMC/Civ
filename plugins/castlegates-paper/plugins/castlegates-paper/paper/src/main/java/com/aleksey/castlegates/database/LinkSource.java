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

public class LinkSource {
	private static final String countAllScript = "SELECT COUNT(*) FROM cg_link";
	private static final String selectAllScript = "SELECT * FROM cg_link ORDER BY LinkId";
	private static final String insertScript = "INSERT INTO cg_link (StartGearblockId, EndGearblockId, Blocks) VALUES (?, ?, ?)";
	private static final String updateScript = "UPDATE cg_link SET StartGearblockId = ?, EndGearblockId = ?, Blocks = ? WHERE LinkId = ?";
	private static final String deleteScript = "DELETE FROM cg_link WHERE LinkId = ?";

	private final SqlDatabase _db;

	public LinkSource(SqlDatabase db) {
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

	public List<LinkInfo> selectAll() throws SQLException {
		ArrayList<LinkInfo> list = new ArrayList<>();

		try (PreparedStatement sql = _db.prepareStatement(selectAllScript)) {
			try (ResultSet rs = sql.executeQuery()) {
				while (rs.next()) {
					LinkInfo info = new LinkInfo();
					info.LinkId = rs.getInt("LinkId");

					info.StartGearblockId = rs.getInt("StartGearblockId");
					if (rs.wasNull()) info.StartGearblockId = null;

					info.EndGearblockId = rs.getInt("EndGearblockId");
					if (rs.wasNull()) info.EndGearblockId = null;

					info.Blocks = rs.getString("Blocks");

					list.add(info);
				}
			}
		}

		return list;
	}

	public void insert(LinkInfo info) throws SQLException {
		try (PreparedStatement sql = _db.prepareStatementWithReturn(insertScript)) {
			if (info.StartGearblockId != null) {
				sql.setInt(1, info.StartGearblockId);
			} else {
				sql.setNull(1, Types.INTEGER);
			}

			if (info.EndGearblockId != null) {
				sql.setInt(2, info.EndGearblockId);
			} else {
				sql.setNull(2, Types.INTEGER);
			}

			if (info.Blocks != null) {
				sql.setString(3, info.Blocks);
			} else {
				sql.setNull(3, Types.VARCHAR);
			}

			sql.executeUpdate();

			try (ResultSet rs = sql.getGeneratedKeys()) {
				rs.next();
				info.LinkId = rs.getInt(1);
			}
		}
	}

	public void update(LinkInfo info) throws SQLException {
		try (PreparedStatement sql = _db.prepareStatement(updateScript)) {
			if (info.StartGearblockId != null) {
				sql.setInt(1, info.StartGearblockId);
			} else {
				sql.setNull(1, Types.INTEGER);
			}

			if (info.EndGearblockId != null) {
				sql.setInt(2, info.EndGearblockId);
			} else {
				sql.setNull(2, Types.INTEGER);
			}

			if (info.Blocks != null) {
				sql.setString(3, info.Blocks);
			} else {
				sql.setNull(3, Types.VARCHAR);
			}

			sql.setInt(4, info.LinkId);

			sql.executeUpdate();
		}
	}

	public void delete(int link_id) throws SQLException {
		try (PreparedStatement sql = _db.prepareStatement(deleteScript)) {
			sql.setInt(1, link_id);

			sql.executeUpdate();
		}
	}
}
