/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.engine;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.engine.bridge.BridgeEventHandler;
import com.aleksey.castlegates.engine.bridge.BridgeManager;
import com.aleksey.castlegates.types.CommandMode;
import com.aleksey.castlegates.types.TimerMode;
import com.aleksey.castlegates.types.TimerOperation;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CastleGatesManager {
	private SqlDatabase _db;
	private StorageManager _storage;
	private BridgeManager _bridgeManager;
	private BridgeEventHandler _bridgeEventHandler;
	private PlayerStateManager _stateManager;

	public boolean init(SqlDatabase db) {
		_db = db;

		try {
			_storage = new StorageManager();
			_storage.init(db);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		_bridgeManager = new BridgeManager();
		_bridgeManager.init(_storage);

		_bridgeEventHandler = new BridgeEventHandler(_storage, _bridgeManager);

		_stateManager = new PlayerStateManager();

		return true;
	}

	public void reinit() {
		CastleGates.getPluginLogger().log(Level.INFO, "Start reload process...");

		_bridgeManager.close();

		try {
			_storage.close();
			_storage.init(_db);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		_bridgeManager.init(_storage);

		CastleGates.getPluginLogger().log(Level.INFO, "Reload process completed.");
	}

	public void close() {
		if(_bridgeManager != null) {
			_bridgeManager.close();
		}

		if(_storage != null) {
			_storage.close();
		}

		if(_db != null) {
			_db.close();
		}
	}

	public void setPlayerMode(Player player, CommandMode mode, Integer timer, TimerOperation timerOperation, TimerMode timerMode) {
		_stateManager.setPlayerMode(player, mode, timer, timerOperation, timerMode);
	}

	public void handlePlayerJoin(PlayerJoinEvent event) {
		_stateManager.clearPlayerMode(event.getPlayer());
	}

	public void handlePlayerQuit(PlayerQuitEvent event) {
		_stateManager.clearPlayerMode(event.getPlayer());
	}

	public void handleBlockClicked(PlayerInteractEvent event) {
		boolean interacted = _bridgeEventHandler.handleBlockClicked(event, _stateManager.getPlayerState(event.getPlayer()));

		if(interacted) {
			_stateManager.interact(event.getPlayer());
		}
	}

	public void handleBlockBreak(BlockBreakEvent event) {
		_bridgeEventHandler.handleBlockBreak(event);
	}

	public void handleBlockRedstone(BlockRedstoneEvent event) {
        _bridgeEventHandler.handleBlockRedstone(event);
	}

	public void handleBlockPhysics(BlockPhysicsEvent event) {
		_bridgeEventHandler.handleBlockPhysics(event);
	}

	public void handleEntityExplode(EntityExplodeEvent event) {
		_bridgeEventHandler.handleEntityExplode(event);
	}

	public void handleEntityChangeBlock(EntityChangeBlockEvent event) { _bridgeEventHandler.handleEntityChangeBlock(event); }

	public void handlePistonEvent(List<Block> blocks) {
		_bridgeEventHandler.handlePistonEvent(blocks);
	}
}
