package vg.civcraft.mc.namelayer.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GroupInvalidationEvent extends Event {
	private String reason;
	private String[] parameter;
	private static final HandlerList handlers = new HandlerList();

	public GroupInvalidationEvent(String reason, String... parameter) {
		this.reason = reason;
		this.parameter = parameter;
	}

	public String getReason() {
		return reason;
	}

	public String[] getParameter() {
		return parameter;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
