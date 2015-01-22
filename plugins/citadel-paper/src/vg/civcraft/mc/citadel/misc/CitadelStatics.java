package vg.civcraft.mc.citadel.misc;

import vg.civcraft.mc.citadel.Citadel;

public enum CitadelStatics {

	UPDATE,
	DELETE,
	INSERT,
	CACHE,
	LOAD;
	
	private static int reins_loaded_from_db = 0;
	private static int reins_called_from_cache = 0;
	private static int reins_updated_to_db = 0;
	private static int reins_deleted_from_db = 0;
	private static int reins_insert_to_db = 0;
	
	/**
	 * Increments the counter of the db call type.
	 * @param en
	 */
	public static void updateHitStat(CitadelStatics en){
		switch(en){
		case UPDATE:
			reins_updated_to_db++;
			break;
		case DELETE:
			reins_deleted_from_db++;
			break;
		case INSERT:
			reins_insert_to_db++;
			break;
		case CACHE:
			reins_called_from_cache++;
			break;
		case LOAD:
			reins_loaded_from_db++;
			break;
		}
	}
	
	public static void decrementHitStat(CitadelStatics en){
		switch(en){
		case UPDATE:
			reins_updated_to_db--;
			break;
		case DELETE:
			reins_deleted_from_db--;
			break;
		case INSERT:
			reins_insert_to_db--;
			break;
		case CACHE:
			reins_called_from_cache--;
			break;
		case LOAD:
			reins_loaded_from_db--;
			break;
		}
	}
	
	public static void displayStatisticsToConsole(){
		String stats = "Citadel Reinforcement Stats:\n";
		stats += "Reinforcements loaded from db " + reins_loaded_from_db + ".\n";
		stats += "Reinforcements loaded from cache " + reins_called_from_cache + ".\n";
		stats += "Total Reinforcement calls " + (reins_called_from_cache + reins_loaded_from_db) + ".\n";
		stats += "Amount of Reinforcement saves " + reins_updated_to_db + ".\n";
		stats += "Reinforcements deleted from the db " + reins_deleted_from_db + ".\n";
		stats += "Reinforcements created and saved to db " + reins_insert_to_db + ".\n";
		Citadel.Log(stats);
	}
}
