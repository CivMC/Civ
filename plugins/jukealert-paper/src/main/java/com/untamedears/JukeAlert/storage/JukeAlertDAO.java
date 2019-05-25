package com.untamedears.JukeAlert.storage;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggedSnitchAction;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class JukeAlertDAO {
	
	private ManagedDatasource db;
	
	public JukeAlertDAO(ManagedDatasource db) {
		this.db = db;
		updateDatabase();
	}
	
	private void updateDatabase() {
		//TODO
		db.registerMigration(1, false, () -> {return true;}, "");
	}
	
	public void insertSnitchLog(LoggedSnitchAction action) {
		
	}
	
	public void insertNewSnitch(Snitch snitch) {
		
	}
	
	public void updateSnitch(Snitch snitch) {
		
	}
	
	public void loadAllActions(Snitch snitch) {
		
	}

}
