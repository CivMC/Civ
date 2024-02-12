package vg.civcraft.mc.civmodcore.world.locations.global;

import it.unimi.dsi.fastutil.objects.Object2ShortAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldIDManager {

	private final CMCWorldDAO dao;
	private final Object2ShortMap<UUID> uuidToInternalID;
	private final Short2ObjectMap<UUID> internalIDToUuid;
	
	public WorldIDManager(CMCWorldDAO dao) {
		this.dao = dao;
		this.uuidToInternalID = new Object2ShortAVLTreeMap<>();
		this.uuidToInternalID.defaultReturnValue((short) -1);
		this.internalIDToUuid = new Short2ObjectAVLTreeMap<>();
		if (!setup()) {
			throw new IllegalStateException("Failed to initialize CMC world tracking");
		}
	}

	/**
	 * Registers all currently loaded worlds internally
	 *
	 * @return Whether all worlds were successfully loaded in or not. Errors here would most likely mean a non-working
	 *         database setup.
	 */
	public boolean setup() {
		for (final World world : Bukkit.getWorlds()) {
			if (!registerWorld(world)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Registers a world for internal use.
	 * 
	 * @param world World to prepare data structures for.
	 * @return Returns whether the registration was successful or not.
	 */
	public boolean registerWorld(final World world) {
		if (this.uuidToInternalID.containsKey(world.getUID())) {
			return true;
		}
		final short id = this.dao.getOrCreateWorldID(world);
		if (id == -1) {
			// very bad
			return false;
		}
		this.uuidToInternalID.put(world.getUID(), id);
		this.internalIDToUuid.put(id, world.getUID());
		return true;
	}

	/**
	 * Gets the world object mapped to an internal id.
	 * 
	 * @param id ID to get world for.
	 * @return World if a matching one for the given id exists and the world is loaded currently.
	 */
	public World getWorldByInternalID(final short id) {
		final UUID uuid = this.internalIDToUuid.get(id);
		if (uuid == null) {
			return null;
		}
		return Bukkit.getWorld(uuid);
	}

	/**
	 * Retrieves the internal id used for a world.
	 *
	 * @param world World UUID to get ID for.
	 * @return Id of the world or -1 if no such world is known.
	 */
	public short getInternalWorldId(final UUID world) {
		return world == null ? -1 : this.uuidToInternalID.getShort(world);
	}

	/**
	 * Retrieves the internal id used for a world.
	 * 
	 * @param world World to get ID for.
	 * @return Id of the world or -1 if no such world is known.
	 */
	public short getInternalWorldId(World world) {
		return world == null ? - 1: getInternalWorldId(world.getUID());
	}

}
