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

public class GearblockSource {
	private static final String selectAllScript = "SELECT * FROM cg_gearblock";
	private static final String insertScript = "INSERT INTO cg_gearblock (location_worlduid, location_x, location_y, location_z) VALUES (?, ?, ?, ?)";
	private static final String updateScript = "UPDATE cg_gearblock SET location_worlduid = ?, location_x = ?, location_y = ?, location_z = ? WHERE gearblock_id = ?";
	private static final String deleteScript = "DELETE FROM cg_gearblock WHERE gearblock_id = ?";
	
	private SqlDatabase db;
	
	public GearblockSource(SqlDatabase db) {
		this.db = db;
	}
	
	public List<GearblockInfo> selectAll() throws SQLException {
		ArrayList<GearblockInfo> list = new ArrayList<GearblockInfo>();
		PreparedStatement sql = this.db.prepareStatement(selectAllScript);
		
		ResultSet rs = sql.executeQuery();
		
		try {
			while(rs.next()) {
				GearblockInfo info = new GearblockInfo();
				info.gearblock_id = rs.getInt("gearblock_id");
				info.location_worlduid = rs.getString("location_worlduid");
				info.location_x = rs.getInt("location_x");
				info.location_y = rs.getInt("location_y");
				info.location_z = rs.getInt("location_z");
				
				list.add(info);
			}
		} finally {
			rs.close();
		}
		
		return list;
	}
	
	public void insert(GearblockInfo info) throws SQLException {
		PreparedStatement sql = this.db.prepareStatementWithReturn(insertScript);
		sql.setString(1, info.location_worlduid);
		sql.setInt(2, info.location_x);
		sql.setInt(3, info.location_y);
		sql.setInt(4, info.location_z);
		
		sql.executeUpdate();
		
		ResultSet rs = sql.getGeneratedKeys();
		
		try {
		    rs.next();
		    info.gearblock_id = rs.getInt(1);
		} finally {
			rs.close();
		}
	}
	
	public void update(GearblockInfo info) throws SQLException {
		PreparedStatement sql = this.db.prepareStatement(updateScript);
		sql.setString(1, info.location_worlduid);
		sql.setInt(2, info.location_x);
		sql.setInt(3, info.location_y);
		sql.setInt(4, info.location_z);
		sql.setInt(5, info.gearblock_id);
		
		sql.executeUpdate();
	}
	
	public void delete(int gear_id) throws SQLException {
		PreparedStatement sql = this.db.prepareStatement(deleteScript);
		sql.setInt(1, gear_id);
		
		sql.executeUpdate();
	}
}