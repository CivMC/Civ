package vg.civcraft.mc.civmodcore.serialization;

import com.google.common.base.Strings;
import java.lang.reflect.Modifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.util.Validation;

/**
 * NBT Serialization allows for readable encoding to and from NBT compounds.
 */
public final class NBTSerialization {

	private static final String NBT_CLASS_PATH_KEY = "==";

	private static final Logger LOGGER = LoggerFactory.getLogger(NBTSerialization.class.getSimpleName());

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
		String SERIALIZE_ERROR = "NBTSerializable[%s] could not be serialised: %s";
		if (serializable == null) {
			LOGGER.debug(SERIALIZE_ERROR, "NULL", "given class was null.");
			return null;
		}
		if (!Modifier.isFinal(serializable.getClass().getModifiers())) {
			LOGGER.debug(SERIALIZE_ERROR, serializable.getClass().getSimpleName(), "given class not final.");
			return null;
		}
		NBTCompound nbt = new NBTCompound();
		try {
			serializable.serialize(nbt);
		}
		catch (Exception exception) {
			LOGGER.warn(SERIALIZE_ERROR, serializable.getClass().getSimpleName(), "error in serialization.");
			return null;
		}
		nbt.setString(NBT_CLASS_PATH_KEY, serializable.getClass().getName());
		return nbt;
	}

	/**
	 * Deserializes an {@link NBTCompound} into a registered {@link NBTSerializable} class, if it exists and is valid.
	 *
	 * @param nbt The NBT data that's used to populate the new instance of the class.
	 * @return Returns a populated instance of a class, or null. It will return null if 1) the given NBTCompound is null
	 *     or empty, 2) the class identifier cannot be found, 3) the class cannot be found, isn't an NBTSerializable,
	 *     isn't final or cannot be constructed, or 4) there was an error during deserialization.
	 */
	@SuppressWarnings("unchecked")
	public static NBTSerializable deserialize(NBTCompound nbt) {
		if (!Validation.checkValidity(nbt)) {
			return null;
		}
		String path = nbt.getString(NBT_CLASS_PATH_KEY);
		if (Strings.isNullOrEmpty(path)) {
			return null;
		}
		Class<?> clazz;
		try {
			clazz = Class.forName(path, true, CivModCorePlugin.class.getClassLoader());
		}
		catch (Exception exception) {
			LOGGER.warn("Could not NBT deserialize into class: could not get class.", exception);
			return null;
		}
		if (!NBTSerializable.class.isAssignableFrom(clazz)) {
			LOGGER.debug("Could not NBT deserialize into class: the class is not an NBT Serializable.");
			return null;
		}
		if (!Modifier.isFinal(clazz.getModifiers())) {
			LOGGER.debug("Could not NBT deserialize into class: the class is not final.");
			return null;
		}
		NBTSerializable instance;
		try {
			instance = ((Class<? extends NBTSerializable>) clazz).newInstance();
		}
		catch (Exception exception) {
			LOGGER.warn("Could not NBT deserialize into class: could not create new instance.", exception);
			return null;
		}
		NBTCompound clone = nbt.clone();
		clone.remove(NBT_CLASS_PATH_KEY);
		try {
			instance.deserialize(clone);
		}
		catch (Exception exception) {
			LOGGER.warn("Could not NBT deserialize into class: an error occurred during deserialization.", exception);
			return null;
		}
		return instance;
	}

	// ------------------------------------------------------------
	// Deprecated Stuff
	// ------------------------------------------------------------

	/**
	 * @deprecated Registering NBTSerializable classes is no longer necessary.
	 */
	@Deprecated
	public static <T extends NBTSerializable> void registerNBTSerializable(Class<T> clazz, String... aliases) { }

	/**
	 * @deprecated Registering NBTSerializable classes is no longer necessary.
	 */
	@Deprecated
	public static <T extends NBTSerializable> void unregisterNBTSerializable(Class<T> clazz) { }

	/**
	 * @deprecated Registering NBTSerializable classes is no longer necessary.
	 */
	@Deprecated
	public static void clearAllRegistrations() { }

}
