package vg.civcraft.mc.civmodcore.nbt.storage;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.File;
import java.util.Objects;
import java.util.stream.Stream;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

public abstract class BatchedNbtStorage<T> {

	protected static final String EXTENSION = "nbt";

	protected final CivLogger logger;
	protected final File storageFolder;

	public BatchedNbtStorage(final Plugin plugin, final String folder) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(folder));
		this.logger = CivLogger.getLogger(plugin.getClass(), getClass());
		this.storageFolder = new File(plugin.getDataFolder(), folder);
	}

	/**
	 * Loads all the ".nbt" files from the storage folder.
	 *
	 * @return Returns a parallel stream of all the correct parsed nbt files into their appropriate container.
	 */
//	public Stream<T> loadAll() {
//		if (!this.storageFolder.isDirectory()) {
//			return Stream.<T>empty().parallel();
//		}
//		final var files = this.storageFolder.listFiles();
//		if (ArrayUtils.isEmpty(files)) {
//			return Stream.<T>empty().parallel();
//		}
//		assert files != null;
//		return Stream.of(files).parallel()
//				.filter(file -> FilenameUtils.isExtension(file.getName(), EXTENSION))
//				.map(this::loadFile)
//				.filter(Objects::nonNull);
//	}

	/**
	 * Saves a given stream of elements to their respective files.
	 *
	 * @param elements The elements to save.
	 * @return Returns a parallel stream of all elements that were successfully saved.
	 */
	public Stream<T> saveSelected(final Stream<T> elements) {
		if (elements == null) {
			return Stream.<T>empty().parallel();
		}
		return elements.parallel()
				.filter(Objects::nonNull)
				.map(this::saveElement)
				.filter(Objects::nonNull); // Remove unsuccessful saves
	}

	/**
	 * Removes all given elements' respective files.
	 *
	 * @param elements The elements to remove the files of.
	 * @return Returns a parallel stream of elements whose files could not be removed.
	 */
	public Stream<T> removeSelected(final Stream<T> elements) {
		if (elements == null) {
			return Stream.<T>empty().parallel();
		}
		return elements.parallel()
				.map(this::removeElement)
				.filter(Objects::nonNull); // Remove successful deletions
	}

	/**
	 * This method is called during {@link #loadAll()} and is used to read and parse the data within the given file. You
	 * should also check the file's name using maybe {@link FilenameUtils#getBaseName(String)} to ensure it's correctly
	 * formatted. I'd recommend using {@link FileUtils#readFileToByteArray(File)} to read the file, then using
	 * {@link NBTSerializable#fromNBT(NBTCompound)} to convert that into a usable NBT instance. If for whatever
	 * reason the file cannot be correctly parsed, the correct course of action is to log the error using
	 * {@link this#logger} and returning null.
	 *
	 * @param file The file to read and parse.
	 * @return Returns a valid instance of the resulting container, or null if something went wrong.
	 */
	protected abstract T loadFile(final File file);

	/**
	 * This method is called during {@link #saveSelected(Stream)} and is used to save particular elements to their
	 * respective files. You can use I'd recommend you use {@link FileUtils#writeByteArrayToFile(File, byte[])} via
	 * {@link NBTSerializable#toNBT(NBTCompound)}. If the element was successfully saved, return the element or
	 * otherwise return null.
	 *
	 * @param element The element to save to its respective file.
	 * @return Returns the element if successfully saved, otherwise null.
	 */
	protected abstract T saveElement(final T element);

	/**
	 * This method is called during {@link #removeSelected(Stream)} and is used to delete particular elements' files.
	 *
	 * @param element The element to delete the file of.
	 * @return Returns null if the file was successfully deleted, otherwise return the given element.
	 */
	protected abstract T removeElement(final T element);

}
