package com.aleksey.castlegates.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PatchSource {
	private static final String insertScript = "INSERT cg_patch (patch_name, applied_date) VALUES (?, ?)";
	private static final String selectScript = "SELECT * FROM cg_patch WHERE patch_name = ?";
	
	private SqlDatabase db;
	
	public PatchSource(SqlDatabase db) {
		this.db = db;
	}
	
	public void insert(PatchInfo info) throws SQLException {
		PreparedStatement sql = this.db.prepareStatementWithReturn(insertScript);
		sql.setString(1, info.patchName);
		sql.setTimestamp(2, info.appliedDate);
		
		sql.executeUpdate();
	}
	
	public Boolean isExist(String patchName) throws SQLException {
		PreparedStatement select = this.db.prepareStatement(selectScript);
		
		select.setString(1, patchName);
		
		Boolean isExist;
		ResultSet resultSet = select.executeQuery();
		
		try
		{
			isExist = resultSet.next();
		}
		finally
		{
			resultSet.close();
		}
		
		return isExist;
	}
}
