package com.github.devotedmc.hiddenore.tracking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.github.devotedmc.hiddenore.Config;
import com.github.devotedmc.hiddenore.HiddenOre;

public class BreakTracking {
	Map<UUID, Map<Long, short[]>> track;
	Map<UUID, Map<Long, short[]>> map;
	
	// Now for one small data structures that will let us keep track of most-recent-breaks to directly
	// prevent gaming.
	
	private static final int RECENT_MAX = 512;
	private int recentPtr;
	private Location[] recent;

	public BreakTracking() {
		track = new HashMap<UUID, Map<Long, short[]>>();
		map = new HashMap<UUID, Map<Long, short[]>>();
		recent = new Location[RECENT_MAX];
		recentPtr = 0;
	}

	public void load() {
		long s = System.currentTimeMillis();
		HiddenOre.getPlugin().getLogger().info("Starting Break Tracking load");
		File tf = Config.getTrackFile();
		if (!tf.exists()) {
			HiddenOre.getPlugin().getLogger().info("No save exists to load");
		} else {
			try {
				DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(tf)));

				String uuid = null;
				boolean active = true;
				while (active) {
					try {
						uuid = dis.readUTF();
						HiddenOre.getPlugin().getLogger().info("Loading tracking data for world " + uuid);
					} catch (EOFException done) {
						active = false;
						break;
					}
					UUID uid = UUID.fromString(uuid);
					Map<Long, short[]> world = new HashMap<Long, short[]>();
					track.put(uid, world);

					long ccnt = 0l;

					while (dis.readBoolean()) {
						Long chunk = dis.readLong();
						short[] layers = new short[256];
						for (int i = 0; i < layers.length; i++) {
							layers[i] = dis.readShort();
						}
						if (Config.isDebug) {
							HiddenOre.getPlugin().getLogger().info("Loaded layers for chunk " + chunk);
						}
						ccnt++;
						world.put(chunk, layers);
					}

					HiddenOre.getPlugin().getLogger().info("Loaded " + ccnt + " chunks");
				}

				dis.close();
			} catch (IOException ioe) {
				HiddenOre.getPlugin().getLogger().log(Level.SEVERE, "Failed to load break tracking.", ioe);
			}
		}
		s = System.currentTimeMillis() - s;
		HiddenOre.getPlugin().getLogger().info("Took " + s + "ms to load Break Tracking");
		
		s = System.currentTimeMillis();
		HiddenOre.getPlugin().getLogger().info("Starting Break Map load");
		tf = Config.getMapFile();
		if (!tf.exists()) {
			HiddenOre.getPlugin().getLogger().info("No map save exists to load");
		} else {
			try {
				DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(tf)));

				String uuid = null;
				boolean active = true;
				while (active) {
					try {
						uuid = dis.readUTF();
						HiddenOre.getPlugin().getLogger().info("Loading mapping data for world " + uuid);
					} catch (EOFException done) {
						active = false;
						break;
					}
					UUID uid = UUID.fromString(uuid);
					Map<Long, short[]> world = new HashMap<Long, short[]>();
					map.put(uid, world);

					long ccnt = 0l;

					while (dis.readBoolean()) {
						Long chunk = dis.readLong();
						short[] layers = new short[256];
						for (int i = 0; i < layers.length; i++) {
							layers[i] = dis.readShort();
						}
						if (Config.isDebug) {
							HiddenOre.getPlugin().getLogger().info("Loaded layers for chunk " + chunk);
						}
						ccnt++;
						world.put(chunk, layers);
					}

					HiddenOre.getPlugin().getLogger().info("Loaded " + ccnt + " chunks");
				}

				dis.close();
			} catch (IOException ioe) {
				HiddenOre.getPlugin().getLogger().log(Level.SEVERE, "Failed to load break map.", ioe);
			}
		}
		s = System.currentTimeMillis() - s;
		HiddenOre.getPlugin().getLogger().info("Took " + s + "ms to load Break Map");
	}

	public void liveSave() {
		Bukkit.getScheduler().runTaskLaterAsynchronously(HiddenOre.getPlugin(), new Runnable() {
			public void run() {
				save();
			}
		}, 0);

		Bukkit.getScheduler().runTaskLaterAsynchronously(HiddenOre.getPlugin(), new Runnable() {
			public void run() {
				saveMap();
			}
		}, 0);

	}

	public void save() {
		long s = System.currentTimeMillis();
		HiddenOre.getPlugin().getLogger().info("Starting Break Tracking save");
		File tf = Config.getTrackFile();
		if (tf.exists()) {
			File tfb = new File(tf.getAbsoluteFile() + ".backup");
			if (tfb.exists()) {
				if (!tfb.delete()) {
					HiddenOre.getPlugin().getLogger().info("Couldn't remove old backup file - " + tfb);
				} else {
					tf.renameTo(tfb);
				}
			} else {
				tf.renameTo(tfb);
			}
		}
		try {
			if (tf.createNewFile()) {
				DataOutputStream oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tf)));

				for (Map.Entry<UUID, Map<Long, short[]>> world : track.entrySet()) {
					HiddenOre.getPlugin().getLogger().info("Saving world - " + world.getKey());
					oos.writeUTF(world.getKey().toString());
					long ccnt = 0l;
					for (Map.Entry<Long, short[]> chunk : world.getValue().entrySet()) {
						if (Config.isDebug) {
							HiddenOre.getPlugin().getLogger().info(" Saving chunk " + chunk.getKey());
						}
						oos.writeBoolean(true);
						oos.writeLong(chunk.getKey());
						short[] layers = chunk.getValue();
						for (short layer : layers) {
							oos.writeShort(layer);
						}
						ccnt++;
					}
					HiddenOre.getPlugin().getLogger().info(" Saved " + ccnt + " chunks");
					oos.writeBoolean(false);
				}
				oos.flush();
				oos.close();
			} else {
				HiddenOre.getPlugin().getLogger().log(Level.WARNING, "Failed to create break tracking save file.");
			}
		} catch (IOException ioe) {
			HiddenOre.getPlugin().getLogger().log(Level.SEVERE, "Failed to save break tracking.", ioe);
		}
		s = System.currentTimeMillis() - s;
		HiddenOre.getPlugin().getLogger().info("Took " + s + "ms to save Break Tracking");
	}

	public void saveMap() {
		long s = System.currentTimeMillis();
		HiddenOre.getPlugin().getLogger().info("Starting Break Map save");
		File tf = Config.getMapFile();
		if (tf.exists()) {
			File tfb = new File(tf.getAbsoluteFile() + ".backup");
			if (tfb.exists()) {
				if (!tfb.delete()) {
					HiddenOre.getPlugin().getLogger().info("Couldn't remove old map backup file - " + tfb);
				} else {
					tf.renameTo(tfb);
				}
			} else {
				tf.renameTo(tfb);
			}
		}
		try {
			if (tf.createNewFile()) {
				DataOutputStream oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tf)));

				for (Map.Entry<UUID, Map<Long, short[]>> world : map.entrySet()) {
					HiddenOre.getPlugin().getLogger().info("Saving world - " + world.getKey());
					oos.writeUTF(world.getKey().toString());
					long ccnt = 0l;
					for (Map.Entry<Long, short[]> chunk : world.getValue().entrySet()) {
						if (Config.isDebug) {
							HiddenOre.getPlugin().getLogger().info(" Saving chunk " + chunk.getKey());
						}
						oos.writeBoolean(true);
						oos.writeLong(chunk.getKey());
						short[] layers = chunk.getValue();
						for (short layer : layers) {
							oos.writeShort(layer);
						}
						ccnt++;
					}
					HiddenOre.getPlugin().getLogger().info(" Saved " + ccnt + " chunks");
					oos.writeBoolean(false);
				}
				oos.flush();
				oos.close();
			} else {
				HiddenOre.getPlugin().getLogger().log(Level.WARNING, "Failed to create break map save file.");
			}
		} catch (IOException ioe) {
			HiddenOre.getPlugin().getLogger().log(Level.SEVERE, "Failed to save break map.", ioe);
		}
		s = System.currentTimeMillis() - s;
		HiddenOre.getPlugin().getLogger().info("Took " + s + "ms to save Break Map");
	}

	
	public boolean trackBreak(Location loc) {
		long s = System.currentTimeMillis();
		int Y = loc.getBlockY();
		int X = loc.getBlockX();
		int Z = loc.getBlockZ();
		UUID world = loc.getWorld().getUID();
		Chunk chunk = loc.getChunk();
		long chunk_id = ((long) chunk.getX() << 32L) + (long) chunk.getZ();
		short block_id = (short) (((short) X << 4) + (short) Z);

		Map<Long, short[]> chunks = track.get(world);
		if (chunks == null) { // init chunk
			chunks = new HashMap<Long, short[]>();
			track.put(world, chunks);
		}
		
		Map<Long, short[]> mapChunks = map.get(world);
		if (mapChunks == null) { // init map chunk
			mapChunks = new HashMap<Long, short[]>();
			map.put(world, mapChunks);
		}

		short[] layers = chunks.get(chunk_id);
		if (layers == null) { // init layers
			layers = new short[256];
			chunks.put(chunk_id, layers);
		}
		
		short[] mapLayers = mapChunks.get(chunk_id);
		if (mapLayers == null) { // init layers
			mapLayers = new short[256];
			mapChunks.put(chunk_id, mapLayers);
			
			for (int y = 0; y < 256; y++) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						Block b = chunk.getBlock(x, y, z);
						if (b.isEmpty() || b.isLiquid()) {
							short bloc = (short) (((short) x << 4) + (short) z);
							mapLayers[y] |= bloc; // if unset, set. Ignore complements basis.
						}
					}
				}
			}
		}

		if (layers[Y] == 0) {
			// quick layer scan for air and water
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					Block b = chunk.getBlock(x, Y, z);
					if (b.isEmpty() || b.isLiquid()) {
						layers[Y]++;
					}
				}
			}
		}

		boolean ret = true;
		if ((mapLayers[Y] & block_id) == block_id) {
			ret = false; // already broken!
		} else {
			ret = true; // new break according to this tracking.
			mapLayers[Y] &= block_id;
			if (Y > 0) mapLayers[Y-1] &= block_id;
			if (Y < 255) mapLayers[Y+1] &= block_id;
			
			// TODO: impact adjacent chunks.
			if (X > 0) mapLayers[Y] &= (short) (((short) (X-1) << 4) + (short) Z); 
			if (X < 15) mapLayers[Y] &= (short) (((short) (X+1) << 4) + (short) Z);
			if (Z > 0) mapLayers[Y] &= (short) (((short) (X) << 4) + (short) (Z-1)); 
			if (Z < 15) mapLayers[Y] &= (short) (((short) (X) << 4) + (short) (Z+1));
		}
		
		if (layers[Y] >= 256) { // done
			ret = false;
		} else if (ret) {
			layers[Y]++; // represent new break in layer.
			ret = true;
		}
		if (ret) { 
			boolean shallow = false;
			int j = recentPtr;
			for (int i = 0; i < RECENT_MAX; i++) {
				j = (recentPtr + i) % RECENT_MAX;
				if (recent[j] == null) break; // anything null means we're done here
				if (loc.equals(recent[j])) {
					ret = false;
					shallow = true;
					break;
				}
			}
			
			if (!shallow) { // we add to the "end" of a circular list. 
				if (--recentPtr < 0) {
					recentPtr += RECENT_MAX;
				}
				recent[recentPtr] = loc;
			}
		}
		
		if (Config.isDebug) {
			HiddenOre.getPlugin().getLogger()
					.info("now world " + world + " chunk " + chunk_id + " layersum " + layers[Y] + " mapt " + mapLayers[Y]);
		}

		s = System.currentTimeMillis() - s;
		if (s > 10l) {
			HiddenOre.getPlugin().getLogger().info("Took a long time (" + s + "ms) recording break at " + loc);
		}

		return ret;
	}
}
