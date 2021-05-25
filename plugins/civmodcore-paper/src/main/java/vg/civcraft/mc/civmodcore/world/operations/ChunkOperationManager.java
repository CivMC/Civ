package vg.civcraft.mc.civmodcore.world.operations;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.civmodcore.world.WorldTracker;
import vg.civcraft.mc.civmodcore.world.WorldUtils;
import vg.civcraft.mc.civmodcore.world.WorldXZ;

public class ChunkOperationManager extends AikarCommand implements Listener {

	public static final ChunkOperationManager INSTANCE = new ChunkOperationManager();
	private static final CivModCorePlugin PLUGIN = CivModCorePlugin.getInstance();
	private static final Map<WorldXZ, List<ChunkOperation>> STORAGE = new HashMap<>();

	@EventHandler
	public void onChunkLoad(final ChunkLoadEvent event) {
		final Chunk chunk = event.getChunk();
		final World world = chunk.getWorld();
		final WorldXZ wxz = new WorldXZ(world.getUID(), chunk.getX(), chunk.getZ());
		final List<ChunkOperation> operations = STORAGE.remove(wxz);
		if (CollectionUtils.isEmpty(operations)) {
			return;
		}
		for (final ChunkOperation operation : operations) {
			executeOperation(operation, chunk);
		}
		operations.clear();
	}

	@CommandAlias("chunkoperations")
	@Description("Shows a zoomed out view of all pending chunk operations.")
	@CommandPermission("cmc.debug")
	public void onDebugCommand(final CommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + "Chunk Operations:");
		for (final Map.Entry<WorldXZ, List<ChunkOperation>> entry : STORAGE.entrySet()) {
			final WorldXZ wxz = entry.getKey();
			final List<ChunkOperation> operations = entry.getValue();
			final int operationCount = operations == null ? 0 : operations.size();
			final World world = WorldTracker.getLoadedWorld(wxz.getWorld());
			// First line: " WorldName: 22 operations"
			final var component = Component.text().content(" ");
			if (world == null) {
				component.append(Component.text("{NOT LOADED}").color(NamedTextColor.RED));
			}
			else {
				component.append(Component.text(world.getName()).color(NamedTextColor.GREEN));
			}
			component.append(Component.text(": "));
			if (operationCount == 0) {
				component.append(Component.text("0 operations"));
				sender.sendMessage(component);
				continue;
			}
			else if (operationCount == 1) {
				component.append(Component.text("1 operation"));
			}
			else {
				component.append(Component.text(operationCount + " operations"));
			}
			sender.sendMessage(component);
			// Other lines: " - PluginName: 1 operation"
			final Map<Plugin, Integer> pluginCount = new HashMap<>();
			for (final ChunkOperation operation : operations) {
				pluginCount.computeIfPresent(operation.plugin, (o, count) -> count + 1);
				pluginCount.putIfAbsent(operation.plugin, 0);
			}
			for (final Map.Entry<Plugin, Integer> count : pluginCount.entrySet()) {
				sender.sendMessage(" - " + ChatColor.AQUA + count.getKey().getName() + ": " +
						ChatColor.RESET + count.getValue());
			}
		}
		sender.sendMessage(ChatColor.RED + "End of operations.");
	}

	/**
	 * Stages an operation for a particular chunk, which may be executed immediately if the chunk is loaded.
	 *
	 * @param location The location of the chunk to operate on. MUST NOT BE A BLOCK LOCATION!
	 * @param operation The operation to perform.
	 * @return Returns true if the operation was executed immediately.
	 */
	public static boolean stageOperation(final Location location, final ChunkOperation operation) {
		if (operation == null) {
			throw new IllegalArgumentException("Operation cannot be null!");
		}
		if (location == null) {
			throw new IllegalArgumentException("Location cannot be null!");
		}
		final World world = WorldUtils.getLocationWorld(location);
		if (world == null) {
			throw new IllegalArgumentException("Location's world must not be null!");
		}
		final Chunk chunk = WorldUtils.getLoadedChunk(world, location.getBlockX(), location.getBlockZ());
		if (chunk == null) {
			final WorldXZ wxz = new WorldXZ(location);
			STORAGE.computeIfAbsent(wxz, l -> new ArrayList<>()).add(operation);
			return false;
		}
		executeOperation(operation, chunk);
		return true;
	}

	private static void executeOperation(final ChunkOperation operation, final Chunk chunk) {
		try {
			operation.process(chunk);
		}
		catch (Exception exception) {
			PLUGIN.warning("Chunk Operation [" + operation.getClass().getName() + "] has thrown an error:", exception);
		}
	}

}
