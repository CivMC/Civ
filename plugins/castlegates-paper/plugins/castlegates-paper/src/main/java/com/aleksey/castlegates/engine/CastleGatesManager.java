/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.engine;

import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.engine.bridge.BridgeEventHandler;
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

public class CastleGatesManager {
	private SqlDatabase db;
	private StorageManager storage;
	private PlayerStateManager stateManager;
	private BridgeEventHandler bridgeEventHandler;

	public boolean init(SqlDatabase db) {
		this.db = db;

		try {
			this.storage = new StorageManager();
			this.storage.init(db);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		this.stateManager = new PlayerStateManager();

		this.bridgeEventHandler = new BridgeEventHandler();
		this.bridgeEventHandler.init(this.storage);

		return true;
	}

	public void close() {
		if(this.bridgeEventHandler != null) {
			this.bridgeEventHandler.close();
		}

		if(this.storage != null) {
			this.storage.close();
		}

		if(this.db != null) {
			this.db.close();
		}
	}

	public void setPlayerMode(Player player, CommandMode mode, Integer timer, TimerOperation timerOperation, TimerMode timerMode) {
		stateManager.setPlayerMode(player, mode, timer, timerOperation, timerMode);
	}

	public void handlePlayerJoin(PlayerJoinEvent event) {
		this.stateManager.clearPlayerMode(event.getPlayer());
	}

	public void handlePlayerQuit(PlayerQuitEvent event) {
		this.stateManager.clearPlayerMode(event.getPlayer());
	}

	public void handleBlockClicked(PlayerInteractEvent event) {
		boolean interacted = this.bridgeEventHandler.handleBlockClicked(event, this.stateManager.getPlayerState(event.getPlayer()));

		if(interacted) {
			this.stateManager.interact(event.getPlayer());
		}
	}

	public void handleBlockBreak(BlockBreakEvent event) {
		this.bridgeEventHandler.handleBlockBreak(event);
	}

	public void handleBlockRedstone(BlockRedstoneEvent event) {
        this.bridgeEventHandler.handleBlockRedstone(event);
	}

	public void handleBlockPhysics(BlockPhysicsEvent event) {
		this.bridgeEventHandler.handleBlockPhysics(event);
	}

	public void handleEntityExplode(EntityExplodeEvent event) {
		this.bridgeEventHandler.handleEntityExplode(event);
	}

	public void handleEntityChangeBlock(EntityChangeBlockEvent event) {
		this.bridgeEventHandler.handleEntityChangeBlock(event);
	}

	public void handlePistonEvent(List<Block> blocks) {
		this.bridgeEventHandler.handlePistonEvent(blocks);
	}
}
