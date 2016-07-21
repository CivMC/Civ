package com.programmerdan.minecraft.civspy;

public class CivSpySaver implements Runnable {

	private Database db;
	private String server;
	private HashMap<UUID, CivSpyPlayer> spydata;
	private HashSet<UUID> markRemove;

	public CivSpySaver(Database db, String server){
		this.db = db;
		this.server = server;
		this.spydata = new ConcurrentHashMap<UUID, CivSpyPlayer>();
	}

	@Override
	public void run() {
		this.saveAll();
	}

	/**
	 * Clone then save the clone
	 */
	public void saveAll() {
		LinkedList<CivSpyPlayer> list = new LinkedList<CivSpyPlayer>();
		synchronized(spydata) {
			synchronized(markRemove) {
				for (CivSpyPlayer player : spydata.elements()) {
					list.add(player.clone());
					player.clear();
				}
				for (UUID player : markRemove) {
					spydata.remove(player);
				}
				markRemove.clear();
			}
		}
		for (CivSpyPlayer player : list) {
			player.save(db);
		}
	}

	public void mark(UUID player) {
		synchronized(markRemove) {
			markRemove.add(player);
		}
	}

	public void unmark(UUID player) {
		synchronized(markRemove) {
			markRemove.remove(player);
		}
	}
	
	public void increment(UUID player, CivSpyPlayer.Tracking datapoint) {
		synchronized(spydata) {
			CivSpyPlayer data = spydata.get(player);
			if (data == null) {
				data = new CivSpyPlayer(player, this.server);
				spydata.put(player, data);
			}
			data.increment(datapoint);
		}
	}
}
