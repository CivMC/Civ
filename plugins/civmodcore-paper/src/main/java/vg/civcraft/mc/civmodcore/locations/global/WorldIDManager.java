package vg.civcraft.mc.civmodcore.locations.global;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldIDManager {

	private Map<UUID, Short> uuidToInternalID;
	private Map<Short, UUID> internalIDToUuid;
	private CMCWorldDAO dao;
	
	public WorldIDManager(CMCWorldDAO dao) {
		this.dao = dao;
		this.uuidToInternalID = new TreeMap<>();
		this.internalIDToUuid = new TreeMap<>();
		if (!setup()) {
			throw new IllegalStateException("Failed to initialize CMC world tracking");
		}
	}
	
	/**
	 * Registers a world for internal use
	 * 
	 * @param world World to prepare data structures for
	 * @return Whether successfull or not
	 */
	public boolean registerWorld(World world) {
		if (uuidToInternalID.containsKey(world.getUID())) {
			return true;
		}
		short id = dao.getOrCreateWorldID(world);
		if (id == -1) {
			// very bad
			return false;
		}
		uuidToInternalID.put(world.getUID(), id);
		internalIDToUuid.put(id, world.getUID());
		return true;
	}

	/**
	 * Registers all currently loaded worlds internally
	 * 
	 * @return Whether all worlds were successfully loaded in or not. Errors here
	 *         would most likely mean a non-working database setup
	 */
	public boolean setup() {
		for (World world : Bukkit.getWorlds()) {
			boolean worked = registerWorld(world);
			if (!worked) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the world object mapped to an internal id
	 * 
	 * @param id ID to get world for
	 * @return World if a matching one for the given id exists and the world is
	 *         loaded currently
	 */
	public World getWorldByInternalID(short id) {
		UUID uuid = internalIDToUuid.get(id);
		if (uuid == null) {
			return null;
		}
		return Bukkit.getWorld(uuid);
	}

	/**
	 * Retrieves the internal id used for a world based on the worlds name. Should
	 * only be used to convert legacy data over
	 * 
	 * @param name Name of the world
	 * @return Id of the world or -1 if no such world is known
	 */
	public short getInternalWorldIdByName(String name) {
		World world = Bukkit.getWorld(name);
		return getInternalWorldId(world);
	}

	/**
	 * Retrieves the internal id used for a world.
	 * 
	 * @param name World to get ID for
	 * @return Id of the world or -1 if no such world is known
	 */
	public short getInternalWorldId(World world) {
		if (world == null) {
			return -1;
		}
		Short result = uuidToInternalID.get(world.getUID());
		if (result == null) {
			return -1;
		}
		return result;
	}
}
