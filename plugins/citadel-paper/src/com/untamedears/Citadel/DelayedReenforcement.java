package com.untamedears.Citadel;

import java.sql.PreparedStatement;
import java.sql.SQLException;
 
public class DelayedReenforcement implements Runnable
{
	private PreparedStatement addReenforcedBlockStatement;
 
	public DelayedReenforcement(PreparedStatement ps)
	{
		this.addReenforcedBlockStatement = ps;
	}
 
	public void run()
	{
		try
		{
			this.addReenforcedBlockStatement.execute();
			this.addReenforcedBlockStatement.close();
		} catch (SQLException e) {
			System.err.println("Error in protecting block.  Details:");
			System.err.println(e.toString());
		}
	}
}