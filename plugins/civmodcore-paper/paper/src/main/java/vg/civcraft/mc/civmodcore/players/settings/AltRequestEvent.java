package vg.civcraft.mc.civmodcore.players.settings;

import com.google.common.base.Preconditions;
import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AltRequestEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private UUID main;
	private UUID accountToGetFor;
	
	public AltRequestEvent(UUID accountToGetFor) {
		Preconditions.checkNotNull(accountToGetFor);
		this.accountToGetFor = accountToGetFor;
		this.main = accountToGetFor; //default
	}
	
	public UUID getAccountToGetMainFor() {
		return accountToGetFor;
	}
	
	public void setMain(UUID uuid) {
		Preconditions.checkNotNull(uuid);
		this.main = uuid;
	}
	
	public UUID getMain() {
		return main;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
