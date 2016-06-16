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

public class ReinforcementSource {
	private static final String selectAllScript = "SELECT * FROM cg_reinforcement ORDER BY link_id, block_no";
	private static final String insertScript = "INSERT INTO cg_reinforcement (link_id, block_no, material_id, durability, insecure, group_id, maturation_time, lore, acid_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String deleteScript = "DELETE FROM cg_reinforcement WHERE link_id = ?";
	
	private SqlDatabase db;
	
	public ReinforcementSource(SqlDatabase db) {
		this.db = db;
	}
	
	public List<ReinforcementInfo> selectAll() throws SQLException {
		ArrayList<ReinforcementInfo> list = new ArrayList<ReinforcementInfo>();
		PreparedStatement sql = this.db.prepareStatement(selectAllScript);
		
		ResultSet rs = sql.executeQuery();
		
		try {
			while(rs.next()) {
				ReinforcementInfo info = new ReinforcementInfo();
				info.link_id = rs.getInt("link_id");
				info.block_no = rs.getInt("block_no");
				info.material_id = rs.getInt("material_id");
				info.durability = rs.getInt("durability");
				info.insecure = rs.getBoolean("insecure");
				info.group_id = rs.getInt("group_id");
				info.maturation_time = rs.getInt("maturation_time");
				info.lore = rs.getString("lore");
				info.acid_time = rs.getInt("acid_time");
				
				list.add(info);
			}
		} finally {
			rs.close();
		}
		
		return list;
	}
	
	public void insert(ReinforcementInfo info) throws SQLException {
		PreparedStatement sql = this.db.prepareStatement(insertScript);
		sql.setInt(1, info.link_id);
		sql.setInt(2, info.block_no);
		sql.setInt(3, info.material_id);
		sql.setInt(4, info.durability);
		sql.setBoolean(5, info.insecure);
		sql.setInt(6, info.group_id);
		sql.setInt(7, info.maturation_time);
		
		if(info.lore == null || info.lore.length() == 0) {
			sql.setNull(8, Types.VARCHAR);
		} else {
			sql.setString(8, info.lore);
		}
		
		sql.setInt(9, info.acid_time);
		
		sql.executeUpdate();
	}
	
	public void deleteByLinkId(int link_id) throws SQLException {
		PreparedStatement sql = this.db.prepareStatement(deleteScript);
		sql.setInt(1, link_id);
		
		sql.executeUpdate();
	}
}