package com.untamedears.Citadel;

import java.sql.PreparedStatement;
import java.sql.SQLException;
 
public class DelayedReinforcement implements Runnable
{
	private PreparedStatement addReinforcedBlockStatement;
 
	public DelayedReinforcement(PreparedStatement ps)
	{
		this.addReinforcedBlockStatement = ps;
	}
 
	public void run()
	{
		try
		{
			this.addReinforcedBlockStatement.execute();
			this.addReinforcedBlockStatement.close();
		} catch (SQLException e) {
			System.err.println("Error in protecting block.  Details:");
			System.err.println(e.toString());
		}
	}
}