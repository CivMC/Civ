package vg.civcraft.mc.civchat2.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PrivateMessageEvent extends PlayerEvent implements Cancellable {

	private final Player receiver;

	private final String message;

	private boolean cancelled;

	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();

	public PrivateMessageEvent(final Player player, final Player receiver, final String message) {
		super(player);

		this.receiver = receiver;
		this.message = message;
	}

	/**
	 * Gets the message receiver
	 * 
	 * @return The message receiver
	 */
	public Player getReceiver() {

		return receiver;
	}

	/**
	 * Gets the chat message
	 * 
	 * @return The chat message
	 */
	public String getMessage() {

		return message;
	}

	@Override
	public HandlerList getHandlers() {

		return handlers;
	}

	public static HandlerList getHandlerList() {

		return handlers;
	}

	@Override
	public boolean isCancelled() {

		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {

		this.cancelled = cancelled;
	}
}
