package vg.civcraft.mc.civmodcore.serialization;

import com.google.common.base.Strings;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class NBTSerialization {

	private static final String NBT_CLASS_PATH_KEY = "==";

	private static final Logger LOGGER = LoggerFactory.getLogger(NBTSerialization.class.getSimpleName());

	private static final Map<String, Class<? extends NBTSerializable>> REGISTERED_CLASSES = new HashMap<>();

	/**
	 * Registers an {@link NBTSerializable} class and any aliases it may have.
	 *
	 * @param <T> The type of the serializable.
	 * @param clazz The class of the NBTSerializable class.
	 * @param aliases The alias class name of the class used for when the class has migrated and legacy support is
	 *     required.
	 * @throws IllegalArgumentException Throws if 1) the NBTSerializable class is null, 2) the class does not have a
	 *     public zero argument constructor, 3) are not final, or 4) is already registered. (Aliases that are already
	 *     registered will print a warning in lieu of throwing.)
	 */
	public static <T extends NBTSerializable> void registerNBTSerializable(Class<T> clazz, String... aliases) {
		String cannotRegisterError = "Cannot register NBTSerializable: ";
		if (clazz == null) {
			throw new IllegalArgumentException(cannotRegisterError + "the given class is null.");
		}
		if (!Modifier.isFinal(clazz.getModifiers())) {
			throw new IllegalArgumentException(cannotRegisterError + "the given class is not final.");
		}
		boolean hasValidConstructor = false;
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (!Modifier.isPublic(constructor.getModifiers())) {
				continue;
			}
			if (constructor.getParameterCount() > 0) {
				continue;
			}
			hasValidConstructor = true;
			break;
		}
		if (!hasValidConstructor) {
			throw new IllegalArgumentException(cannotRegisterError + "the given class has no default constructor.");
		}
		if (REGISTERED_CLASSES.containsKey(clazz.getName())) {
			throw new IllegalArgumentException(cannotRegisterError + "the given class is already registered.");
		}
		LOGGER.info("NBTSerializable[" + clazz.getName() + "] registered.");
		REGISTERED_CLASSES.put(clazz.getName(), clazz);
		if (aliases != null) {
			String errorMessage = "Could not register the alias for NBTSerializable[" + clazz.getName() + "] as: ";
			for (String alias : aliases) {
				if (Strings.isNullOrEmpty(alias)) {
					LOGGER.warn(errorMessage + "the alias is null or empty.");
					continue;
				}
				if (alias.equals(clazz.getName())) {
					LOGGER.warn(errorMessage + "the alias matches the class name.");
					continue;
				}
				if (REGISTERED_CLASSES.containsKey(alias)) {
					LOGGER.warn(errorMessage + "the alias is already registered.");
					continue;
				}
				LOGGER.info("NBTSerializable[" + clazz.getName() + "] alias [" + alias + "] registered.");
				REGISTERED_CLASSES.putIfAbsent(alias, clazz);
			}
		}
	}

	/**
	 * Unregisters an {@link NBTSerializable} class and all its paths.
	 *
	 * @param <T> The type of the serializable.
	 * @param clazz The NBTSerializable class to unregister.
	 * @throws IllegalArgumentException Throws if the given class to unregister is null.
	 */
	public static <T extends NBTSerializable> void unregisterNBTSerializable(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("Cannot unregister NBTSerializable: " +
					"the given class is null.");
		}
		LOGGER.info("NBTSerializable[" + clazz.getName() + "] unregistered.");
		REGISTERED_CLASSES.values().remove(clazz);
	}

	/**
	 * Unregisters all {@link NBTSerializable} classes.
	 */
	public static void clearAllRegistrations() {
		LOGGER.info("All NBTSerialization registered classes have been cleared.");
		REGISTERED_CLASSES.clear();
	}

	/**
	 * Serializes an {@link NBTSerializable} class into an {@link NBTCompound} for the purpose of being able to
	 * deserialize it later. For this purpose, {@link NBTSerializable#serialize(NBTCompound) NBTSerializable's
	 * serialize} method should not be called directly.
	 *
	 * @param serializable The {@link NBTSerializable} class to serialize.
	 * @return Returns an {@link NBTCompound} with the serialized data, or null if the given object was null, or if an
	 *     error occurred during serialization.
	 */
	public static NBTCompound serialize(NBTSerializable serializable) {
		if (serializable == null) {
			return null;
		}
		NBTCompound nbt = new NBTCompound();
		try {
			serializable.serialize(nbt);
		}
		catch (Exception exception) {
			throw new NBTSerializationException(
					"NBTSerializable[" + serializable.getClass().getName() + "] could not be serialized.", exception);
		}
		nbt.setString(NBT_CLASS_PATH_KEY, serializable.getClass().getName());
		return nbt;
	}

	/**
	 * Deserializes an {@link NBTCompound} into a registered {@link NBTSerializable} class, if it exists.
	 *
	 * @param nbt The NBT data that's used to populate the new instance of the class.
	 * @return Returns a populated instance of a class, or null. It will return null if 1) the given NBTCompound is null
	 *     or empty, 2) the class identifier cannot be found, 3) the class cannot be matched.
	 */
	public static NBTSerializable deserialize(NBTCompound nbt) {
		if (nbt == null || nbt.isEmpty()) {
			return null;
		}
		String path = nbt.getString(NBT_CLASS_PATH_KEY);
		if (Strings.isNullOrEmpty(path)) {
			return null;
		}
		Class<? extends NBTSerializable> clazz = REGISTERED_CLASSES.get(path);
		if (clazz == null) {
			return null;
		}
		try {
			NBTSerializable instance = clazz.newInstance();
			NBTCompound clone = nbt.clone();
			clone.remove(NBT_CLASS_PATH_KEY);
			instance.deserialize(clone);
			return instance;
		}
		catch (Exception exception) {
			throw new NBTSerializationException(
					"NBTSerializable[" + clazz.getName() + "] could not be deserialized.", exception);
		}
	}

}
