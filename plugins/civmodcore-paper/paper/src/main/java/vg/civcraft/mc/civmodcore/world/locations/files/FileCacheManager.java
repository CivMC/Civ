package vg.civcraft.mc.civmodcore.world.locations.files;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileCacheManager<D extends TableBasedDataObject> {
	private class ChunkState {
		public boolean isValid; // If the chunk is valid this means that RegionFile contains up-to date data for this chunk
		public boolean isLoaded;
	}

	private class FileState {
		public final Map<ChunkPos, ChunkState> chunkStates;
		public int loadedCount;

		public FileState() {
			this.chunkStates = new HashMap<>();
		}
	}

	private class UnloadStatus {
		public final boolean needSaveChunk;
		public final boolean needCloseFile;

		public UnloadStatus(boolean needSaveChunk, boolean needCloseFile) {
			this.needSaveChunk = needSaveChunk;
			this.needCloseFile = needCloseFile;
		}

		@Override
		public String toString() {
			return " { needSaveChunk = " + this.needSaveChunk + ", needCloseFile = " + this.needCloseFile + " }";
		}
	}

	private static final short InvalidCacheVersion = -1;
	private static final short CacheVersion = 1; // TODO-AL : move to the config
	private static final long CacheExpiredAfterMs = 3L * 24L * 60L * 60L * 1000L; // 3 days TODO-AL : move to the config

	private final FileCacheSerializer<D> serializer;
	private final Path cacheFolder;
	private final Map<Path, RegionFile> files;
	private final Map<Path, FileState> fileStates;

	public FileCacheManager(FileCacheSerializer<D> serializer, Path cacheFolder) {
		this.serializer = serializer;
		this.cacheFolder = cacheFolder;
		this.files = new HashMap<>();
		this.fileStates = new ConcurrentHashMap<>();
	}

	public List<D> load(World world, int chunkX, int chunkZ) {
		List<D> result;

		try {
			RegionFile regionFile = getRegionFile(world, chunkX, chunkZ);

			synchronized (regionFile) {
				ChunkPos pos = new ChunkPos(chunkX, chunkZ);
				if (regionFile.doesChunkExist(pos)) {
					try (DataInputStream stream = regionFile.getChunkDataInputStream(pos)) {
						result = readFromStream(world, chunkX, chunkZ, stream);
					}
				} else {
					result = null;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		markLoaded(world, chunkX, chunkZ, result != null);

		return result;
	}

	private List<D> readFromStream(World world, int chunkX, int chunkZ, DataInputStream stream) throws IOException {
		short cacheVersion = stream.readShort();
		if (cacheVersion != CacheVersion)
			return null;

		long timestamp = stream.readLong();
		if (timestamp +  CacheExpiredAfterMs <= System.currentTimeMillis())
			return null;

		int chunkXOffset = chunkX << 4; // multiply by 16
		int chunkZOffset = chunkZ << 4; // multiply by 16

		List<D> result = new ArrayList<>();

		int count = stream.readInt();
		for (int i = 0; i < count; i++) {
			D d = this.serializer.deserialize(stream, world, chunkXOffset, chunkZOffset);
			result.add(d);
		}

		return result;
	}

	public void invalidate(World world, int chunkX, int chunkZ) {
		if (!markLoaded(world, chunkX, chunkZ, false)) // Already marked as invalid no need to do other actions
			return;

		// Aleksey's Temporary: CivModCorePlugin.getInstance().getLogger().warning("Invalidate chunk world = " + world.getName() + ", x = " + chunkX + ", z = " + chunkZ );

		try {
			RegionFile regionFile = getRegionFile(world, chunkX, chunkZ);

			synchronized (regionFile) {
				ChunkPos pos = new ChunkPos(chunkX, chunkZ);

				if (!regionFile.doesChunkExist(pos))
					return;

				try (DataOutputStream stream = regionFile.getChunkDataOutputStream(pos)) {
					stream.writeLong(InvalidCacheVersion);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void unload(World world, int chunkX, int chunkZ, List<D> objects) {
		if (!isChunkLoaded(world, chunkX, chunkZ))
			return;

		try {
			RegionFile regionFile = getRegionFile(world, chunkX, chunkZ);
			UnloadStatus unloadStatus = markUnloaded(world, chunkX, chunkZ);

			// Aleksey's Temporary: CivModCorePlugin.getInstance().getLogger().warning("Unload chunk world = " + world.getName() + ", x = " + chunkX + ", z = " + chunkZ + ", objects.size() = " + objects.size() + ", unloadStatus = " + unloadStatus + ", loadedCount = " + this.fileStates.get(getRegionFilePath(world, chunkX, chunkZ)).loadedCount);

			synchronized (regionFile) {
				if (unloadStatus.needSaveChunk) {
					try (DataOutputStream stream = regionFile.getChunkDataOutputStream(new ChunkPos(chunkX, chunkZ))) {
						stream.writeShort(CacheVersion);
						stream.writeLong(System.currentTimeMillis()); // Timestamp
						stream.writeInt(objects.size());

						for (D o : objects)
							this.serializer.serialize(stream, o);
					}
				}

				if (unloadStatus.needCloseFile)
					regionFile.close();
			}

			if (unloadStatus.needCloseFile) {
				synchronized (this) {
					Path file = getRegionFilePath(world, chunkX, chunkZ);
					this.files.remove(file);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean markLoaded(World world, int chunkX, int chunkZ, boolean isValid) {
		boolean oldIsValid;
		Path file = getRegionFilePath(world, chunkX, chunkZ);

		FileState fileState = this.fileStates.get(file);
		if (fileState == null) {
			this.fileStates.putIfAbsent(file, new FileState());
			fileState = this.fileStates.get(file);
		}

		synchronized (fileState) {
			ChunkPos pos = new ChunkPos(chunkX, chunkZ);
			ChunkState state = fileState.chunkStates.get(pos);

			if (state == null)
				fileState.chunkStates.put(pos, state = new ChunkState());

			oldIsValid = state.isValid;

			state.isValid = isValid;

			if (!state.isLoaded) {
				state.isLoaded = true;
				fileState.loadedCount++;
			}
		}

		return oldIsValid;
	}

	private boolean isChunkLoaded(World world, int chunkX, int chunkZ) {
		Path file = getRegionFilePath(world, chunkX, chunkZ);
		FileState fileState = this.fileStates.get(file);

		if (fileState == null)
			return false;

		synchronized (fileState) {
			ChunkPos pos = new ChunkPos(chunkX, chunkZ);
			ChunkState state = fileState.chunkStates.get(pos);

			return state != null && state.isLoaded;
		}
	}

	private UnloadStatus markUnloaded(World world, int chunkX, int chunkZ) { // True - if all chunks in the region are unloaded
		Path file = getRegionFilePath(world, chunkX, chunkZ);
		FileState fileState = this.fileStates.get(file);

		if (fileState == null)
			return new UnloadStatus(true, true);

		synchronized (fileState) {
			ChunkPos pos = new ChunkPos(chunkX, chunkZ);
			ChunkState state = fileState.chunkStates.get(pos);
			boolean needSaveChunk;

			if (state != null) {
				needSaveChunk = !state.isValid;

				if (state.isLoaded) {
					state.isLoaded = false;
					fileState.loadedCount--;
				}
			} else {
				needSaveChunk = true;
			}

			return new UnloadStatus(needSaveChunk, fileState.loadedCount <= 0);
		}
	}

	public void closeCacheFiles() {
		try {
			for (RegionFile regionFile : this.files.values()) {
				regionFile.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		this.files.clear();
	}

	private synchronized RegionFile getRegionFile(World world, int chunkX, int chunkZ) throws IOException {
		Path file = getRegionFilePath(world, chunkX, chunkZ);
		RegionFile regionFile = this.files.get(file);

		if (regionFile != null)
			return regionFile;

		// Aleksey's Temporary: long startTime = System.currentTimeMillis();

		if (Files.notExists(file.getParent()))
			Files.createDirectories(file.getParent());

		regionFile = createRegionFile(file);
		this.files.put(file, regionFile);

		// Aleksey's Temporary: CivModCorePlugin.getInstance().getLogger().warning("createRegionFile() :: file = " + file + ", time = " + (System.currentTimeMillis() - startTime));

		return regionFile;
	}

	private RegionFile createRegionFile(Path path) throws IOException {
		boolean isSyncChunkWrites = ((CraftServer) Bukkit.getServer()).getServer().forceSynchronousWrites();
		return new RegionFile(path, path.getParent(), RegionFileVersion.VERSION_DEFLATE, isSyncChunkWrites);
	}

	public Path getRegionFilePath(World world, int chunkX, int chunkZ) {
		Path dir = this.cacheFolder.resolve(world.getName());
		return dir.resolve("r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca");
	}
}
