package com.programmerdan.minecraft.banstick.data;

import java.util.List;
import java.util.Map;

public class BSShares {
	
	private BSShares() {}
	
	private BSPlayer forPlayer;
	
	private List<Long> shareList;

	public static BSShares onlyFor(BSPlayer player) {
		BSShares shares = new BSShares();
		shares.forPlayer = player;
		shares.shareList = null;
		// TODO: all of this.
		return null;
	}
	
	public static void release(BSShares shares) {
		shares.forPlayer = null;
		if (shares.shareList != null) {
			shares.shareList.clear();
		}
		shares.shareList = null;
	}
	
	public void check(BSSession overlap) {
		// Basically use the IP information to look for an overlap
		// TODO:
	}
}
