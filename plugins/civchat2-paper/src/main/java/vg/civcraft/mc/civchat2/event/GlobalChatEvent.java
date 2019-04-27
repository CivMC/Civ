package vg.civcraft.mc.civchat2.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class GlobalChatEvent extends PlayerEvent implements Cancellable {

	private final String message;

	private final String format;

	private boolean cancelled;

	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();

	public GlobalChatEvent(final Player player, final String message, final String format) {

		super(player);

		this.message = message;
		this.format = format;
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

	/**
	 * Gets the format to use to display this chat message.
	 * 
	 * @return The message format
	 */
	public String getFormat() {

		return format;
	}
}
