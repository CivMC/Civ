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
	private static final String selectAllScript = "SELECT * FROM cg_link ORDER BY link_id";
	private static final String insertScript = "INSERT INTO cg_link (gearblock1_id, gearblock2_id, blocks) VALUES (?, ?, ?)";
	private static final String updateScript = "UPDATE cg_link SET gearblock1_id = ?, gearblock2_id = ?, blocks = ? WHERE link_id = ?";
	private static final String deleteScript = "DELETE FROM cg_link WHERE link_id = ?";

	private SqlDatabase db;

	public LinkSource(SqlDatabase db) {
		this.db = db;
	}

	public int countAll() throws SQLException {
		PreparedStatement sql = this.db.prepareStatement(countAllScript);

		ResultSet rs = sql.executeQuery();

		try {
			if(rs.next()) return rs.getInt(1);
		} finally {
			rs.close();
		}

		return 0;
	}

	public List<LinkInfo> selectAll() throws SQLException {
		ArrayList<LinkInfo> list = new ArrayList<LinkInfo>();
		PreparedStatement sql = this.db.prepareStatement(selectAllScript);

		ResultSet rs = sql.executeQuery();

		try {
			while(rs.next()) {
				LinkInfo info = new LinkInfo();
				info.link_id = rs.getInt("link_id");

				info.gearblock1_id = rs.getInt("gearblock1_id");
				if(rs.wasNull()) info.gearblock1_id = null;

				info.gearblock2_id = rs.getInt("gearblock2_id");
				if(rs.wasNull()) info.gearblock2_id = null;

				info.blocks = rs.getBytes("blocks");

				list.add(info);
			}
		} finally {
			rs.close();
		}

		return list;
	}

	public void insert(LinkInfo info) throws SQLException {
		PreparedStatement sql = this.db.prepareStatementWithReturn(insertScript);

		if(info.gearblock1_id != null) {
			sql.setInt(1, info.gearblock1_id);
		} else {
			sql.setNull(1, Types.INTEGER);
		}

		if(info.gearblock2_id != null) {
			sql.setInt(2, info.gearblock2_id);
		} else {
			sql.setNull(2, Types.INTEGER);
		}

		if(info.blocks != null) {
			sql.setBytes(3, info.blocks);
		} else {
			sql.setNull(3, Types.VARBINARY);
		}

		sql.executeUpdate();

		ResultSet rs = sql.getGeneratedKeys();

		try {
		    rs.next();
		    info.link_id = rs.getInt(1);
		} finally {
			rs.close();
		}
	}

	public void update(LinkInfo info) throws SQLException {
		PreparedStatement sql = this.db.prepareStatement(updateScript);

		if(info.gearblock1_id != null) {
			sql.setInt(1, info.gearblock1_id);
		} else {
			sql.setNull(1, Types.INTEGER);
		}

		if(info.gearblock2_id != null) {
			sql.setInt(2, info.gearblock2_id);
		} else {
			sql.setNull(2, Types.INTEGER);
		}

		if(info.blocks != null) {
			sql.setBytes(3, info.blocks);
		} else {
			sql.setNull(3, Types.VARBINARY);
		}

		sql.setInt(4, info.link_id);

		sql.executeUpdate();
	}

	public void delete(int link_id) throws SQLException {
		PreparedStatement sql = this.db.prepareStatement(deleteScript);
		sql.setInt(1, link_id);

		sql.executeUpdate();
	}
}
