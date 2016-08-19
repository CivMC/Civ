package vg.civcraft.mc.namelayer.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.database.AssociationList;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * A class for modifying the name of a minecraft player.
 *
 * Created by tyro on 7/1/16.
 */
final class PlayerNameModifier {

	private final AssociationList provider = NameAPI.getAssociationList();

	/**
	 * A field of the EntityHuman class. It is of type GameProfile.
	 */
	private Field gameProfileField;

	/**
	 * A field of the GameProfile class. It is of type String.
	 */
	private Field nameField;

	/**
	 * A instance method for the CraftHumanEntity class. Returns the underlying EntityHuman instance.
	 */
	private Method getHandle;

	/**
	 * A static method for the MinecraftServer class. Returns the singleton MinecraftServer instance.
	 */
	private Method getServer;

	/**
	 * An instance method for the MinecraftServer class. Returns a UserCache instance.
	 */
	private Method getUserCache;

	/**
	 * An instance method for the UserCache class. Takes a GameProfile instance as a parameter.
	 */
	private Method updateGameProfile;

	/**
	 * Constructor.
	 * <p>
	 * Throws exceptions if there is a problem while configuring reflection.
	 * </p>
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws NoSuchMethodException
	 */
	PlayerNameModifier() throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
		final String pkg = Bukkit.getServer().getClass().getPackage().getName();
		final String version = pkg.substring(pkg.lastIndexOf('.') + 1);
		final String nms = "net.minecraft.server." + version;
		final String craft = "org.bukkit.craftbukkit." + version;

		Class<?> craftHumanEntityClass = Class.forName(craft + ".entity.CraftHumanEntity");
		getHandle = craftHumanEntityClass.getMethod("getHandle");

		Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
		nameField = gameProfileClass.getDeclaredField("name");
		nameField.setAccessible(true);

		Class<?> entityHumanClass = Class.forName(nms + ".EntityHuman");
		gameProfileField = getFieldOfType(entityHumanClass, gameProfileClass);
		gameProfileField.setAccessible(true);

		Class<?> minecraftServerClass = Class.forName(nms + ".MinecraftServer");
		getServer = minecraftServerClass.getMethod("getServer");
		getUserCache = minecraftServerClass.getMethod("getUserCache");

		Class<?> userCacheClass = Class.forName(nms + ".UserCache");
		updateGameProfile = userCacheClass.getMethod("a", gameProfileClass);
	}

	/**
	 * Modifies the name of the given player to a suitable one that is unique across
	 * the server.
	 * @param player Player whose name needs to be modified.
	 * @return true if the name is properly set, false otherwise.
     */
	boolean modify(final Player player) {
		String name = provider.getCurrentName(player.getUniqueId());
		String oldName = player.getName();

		// If the names are the same then there is no need to attempt reflection.
		if (oldName.equals(name)) {
			return true;
		}

		try {
			Object humanEntity = getHandle.invoke(player);
			Object gameProfile = gameProfileField.get(humanEntity);
			nameField.set(gameProfile, name);

			Object server = getServer.invoke(null);
			Object userCache = getUserCache.invoke(server);
			updateGameProfile.invoke(userCache, gameProfile);

			// Set the api names after reflection so that there will not be an inconsistency
			// between the profile name and the display name if the reflection fails early on.
			player.setDisplayName(name);
			player.setPlayerListName(name);
			player.setCustomName(name);
		} catch (InvocationTargetException e) {
			return false;
		} catch (IllegalAccessException e) {
			return false;
		} catch (NoSuchElementException e) {
			return false;
		}
		return true;
	}

	/**
	 * Searches for the first field of type fieldType inside of clazz and returns it.
	 * <p>
	 * If a field of type fieldType is not found, then a {@link NoSuchFieldException} is thrown.
	 * In the context of this class, this method returns the GameProfile field for the EntityHuman class.
	 * </p>
	 * @param clazz The class to search for the desired field.
	 * @param fieldType The type of the desired field.
	 * @return The desired field of fieldType in clazz.
	 * @throws NoSuchFieldException
	 */
	private static Field getFieldOfType(final Class<?> clazz, final Class<?> fieldType) throws NoSuchFieldException {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getType().equals(fieldType)) {
				return field;
			}
		}
		throw new NoSuchFieldException(String.format(
			"Field of type %s does not exist in class %s!",
			fieldType.getName(), clazz.getName()));
	}

}
