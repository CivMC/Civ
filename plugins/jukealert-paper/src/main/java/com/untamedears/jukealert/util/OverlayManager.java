package com.untamedears.jukealert.util;


import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;

public class OverlayManager {
	
	private BottomLine snitchCountBottomLine;
	private CivScoreBoard snitchCountBoard;
	
	public OverlayManager() {
		this.snitchCountBottomLine = BottomLineAPI.createBottomLine("jaSnitchCount", 5);
		this.snitchCountBoard = ScoreBoardAPI.createBoard("jaSnitchCount");
	}
	
	public void updateSnitchCount() {
		
	}

}
