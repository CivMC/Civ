package vg.civcraft.mc.civmodcore.command;

import java.util.LinkedList;
import java.util.List;

import vg.civcraft.mc.mercury.MercuryAPI;

public class MercuryTabCompleter {
	
	public static List <String> complete(String args) {
		List <String> completes = new LinkedList<String>();
		for(String p : MercuryAPI.getAllPlayers()) {
			if (p.toLowerCase().startsWith(args)) {
				completes.add(p);
			}
		}
		return completes;
	}

}
