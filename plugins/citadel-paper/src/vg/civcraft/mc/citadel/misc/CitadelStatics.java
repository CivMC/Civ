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
		StringBuilder stats = new StringBuilder("Citadel Reinforcement Stats:\n");
		stats.append("  Reinforcements loaded from db ").append(reins_loaded_from_db).append(".\n");
		stats.append("  Reinforcements loaded from cache ").append(reins_called_from_cache).append(".\n");
		stats.append("  Total Reinforcement calls ").append(reins_called_from_cache + reins_loaded_from_db).append(".\n");
		stats.append("  Amount of Reinforcement saves ").append(reins_updated_to_db).append(".\n");
		stats.append("  Reinforcements deleted from the db ").append(reins_deleted_from_db).append(".\n");
		stats.append("  Reinforcements created and saved to db ").append(reins_insert_to_db).append(".\n");
		Citadel.getInstance().getLogger().info(stats.toString());
	}
}
