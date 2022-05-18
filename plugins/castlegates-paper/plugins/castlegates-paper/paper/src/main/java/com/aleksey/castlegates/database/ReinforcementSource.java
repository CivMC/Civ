/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReinforcementSource {
	private static final String selectAllScript = "SELECT * FROM cg_reinforcement ORDER BY LinkId, BlockSequence";
	private static final String insertScript = "INSERT INTO cg_reinforcement (LinkId, BlockSequence, CreationTime, TypeId, Health, GroupId, Insecure) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String deleteScript = "DELETE FROM cg_reinforcement WHERE LinkId = ?";

	private final SqlDatabase _db;

	public ReinforcementSource(SqlDatabase db) {
		_db = db;
	}

	public List<ReinforcementInfo> selectAll() throws SQLException {
		ArrayList<ReinforcementInfo> list = new ArrayList<>();

		try (PreparedStatement sql = _db.prepareStatement(selectAllScript)) {

			try (ResultSet rs = sql.executeQuery()) {
				while (rs.next()) {
					ReinforcementInfo info = new ReinforcementInfo();
					info.LinkId = rs.getInt("LinkId");
					info.BlockSequence = rs.getInt("BlockSequence");
					info.CreationTime = rs.getLong("CreationTime");
					info.TypeId = rs.getShort("TypeId");
					info.Health = rs.getFloat("Health");
					info.GroupId = rs.getInt("GroupId");
					info.Insecure = rs.getBoolean("Insecure");

					list.add(info);
				}
			}
		}

		return list;
	}

	public void insert(ReinforcementInfo info) throws SQLException {
		try (PreparedStatement sql = _db.prepareStatement(insertScript)) {
			sql.setInt(1, info.LinkId);
			sql.setInt(2, info.BlockSequence);
			sql.setLong(3, info.CreationTime);
			sql.setShort(4, info.TypeId);
			sql.setFloat(5, info.Health);
			sql.setInt(6, info.GroupId);
			sql.setBoolean(7, info.Insecure);

			sql.executeUpdate();
		}
	}

	public void deleteByLinkId(int link_id) throws SQLException {
		try (PreparedStatement sql = _db.prepareStatement(deleteScript)) {
			sql.setInt(1, link_id);

			sql.executeUpdate();
		}
	}
}
