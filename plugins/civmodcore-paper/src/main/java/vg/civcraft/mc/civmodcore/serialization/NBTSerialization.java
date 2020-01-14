package vg.civcraft.mc.civmodcore.serialization;

import com.google.common.base.Strings;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import vg.civcraft.mc.civmodcore.api.NBTCompound;

/**
 *
 */
public final class NBTSerialization {

	private static final String NBT_CLASS_PATH_KEY = "==";
	private static final Map<String, Class<? extends NBTSerializable>> registeredClasses = new HashMap<>();

	/**
	 * Registers an {@link NBTSerializable} class and any aliases it may have.
	 *
	 * @param clazz The class of the NBTSerializable class.
	 * @param aliases The alias class name of the class used for when the class has migrated and legacy support is
	 * required.
	 * @throws IllegalArgumentException Throws if 1) the NBTSerializable class is null, 2) the class does not have a
	 * public zero argument constructor, or 3) is already registered. (Aliases that are already registered will print a
	 * warning in lieu of throwing.)
	 */
	public static <T extends NBTSerializable> void registerNBTSerializable(Class<T> clazz, String... aliases) {
		if (clazz == null) {
			throw new IllegalArgumentException("Cannot register NBTSerializable: " +
					"the given class is null.");
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
			throw new IllegalArgumentException("Cannot register NBTSerializable: " +
					"the given class has no public, zero parameter constructor.");
		}
		if (registeredClasses.containsKey(clazz.getName())) {
			throw new IllegalArgumentException("Cannot register NBTSerializable: " +
					"the given class is already registered.");
		}
		System.out.println("NBTSerializable[" + clazz.getName() + "] registered.");
		registeredClasses.put(clazz.getName(), clazz);
		if (aliases != null) {
			String errorMessage = "Could not register the alias for NBTSerializable [" + clazz.getName() + "] as: ";
			for (String alias : aliases) {
				if (Strings.isNullOrEmpty(alias)) {
					System.err.println(errorMessage + "the alias is null or empty.");
					continue;
				}
				if (alias.equals(clazz.getName())) {
					System.err.println(errorMessage + "the alias matches the class name.");
					continue;
				}
				if (registeredClasses.containsKey(alias)) {
					System.err.println(errorMessage + "the alias is already registered.");
					continue;
				}
				System.out.println("NBTSerializable[" + clazz.getName() + "] alias [" + alias + "] registered.");
				registeredClasses.putIfAbsent(alias, clazz);
			}
		}
	}

	/**
	 * Unregisters an {@link NBTSerializable} class and all its paths.
	 *
	 * @param clazz The NBTSerializable class to unregister.
	 * @throws IllegalArgumentException Throws if the given class to unregister is null.
	 */
	public static <T extends NBTSerializable> void unregisterNBTSerializable(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("Cannot unregister NBTSerializable: " +
					"the given class is null.");
		}
		System.out.println("NBTSerializable[" + clazz.getName() + "] unregistered.");
		registeredClasses.values().remove(clazz);
	}

	/**
	 * Unregisters all {@link NBTSerializable} classes.
	 */
	public static void clearAllRegistrations() {
		System.out.println("All NBTSerialization registered classes have been cleared.");
		registeredClasses.clear();
	}

	/**
	 * Serializes an {@link NBTSerializable} class into an {@link NBTCompound} for the purpose of being able to
	 * deserialize it later. For this purpose, {@link NBTSerializable#serialize(NBTCompound) NBTSerializable's
	 * serialize} method should not be called directly.
	 *
	 * @param serializable The {@link NBTSerializable} class to serialize.
	 * @return Returns an {@link NBTCompound} with the serialized data, or null if the given object was null, or if an
	 * error occurred during serialization.
	 */
	public static NBTCompound serialize(NBTSerializable serializable) {
		if (serializable == null) {
			return null;
		}
		NBTCompound nbt = new NBTCompound();
		try {
			serializable.serialize(nbt);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
		nbt.setString(NBT_CLASS_PATH_KEY, serializable.getClass().getName());
		return nbt;
	}

	/**
	 * Deserializes an {@link NBTCompound} into a registered {@link NBTSerializable} class, if it exists.
	 *
	 * @param nbt The NBT data that's used to populate the new instance of the class.
	 * @return Returns a populated instance of a class, or null. It will return null if 1) the given NBTCompound is null
	 * or empty, 2) the class identifier cannot be found, 3) the class cannot be matched, or 4) there was an error in
	 * casting the instance into the given generic type.
	 */
	public static NBTSerializable deserialize(NBTCompound nbt) {
		if (nbt == null || nbt.isEmpty()) {
			return null;
		}
		String path = nbt.getString(NBT_CLASS_PATH_KEY);
		nbt.remove(NBT_CLASS_PATH_KEY);
		if (Strings.isNullOrEmpty(path)) {
			return null;
		}
		Class<? extends NBTSerializable> clazz = registeredClasses.get(path);
		if (clazz == null) {
			return null;
		}
		NBTSerializable instance;
		try {
			instance = clazz.newInstance();
			instance.deserialize(nbt);
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
		return instance;
	}

}
