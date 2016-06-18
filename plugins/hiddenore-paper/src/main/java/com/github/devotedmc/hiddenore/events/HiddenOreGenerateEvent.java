package com.github.devotedmc.hiddenore.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HiddenOreGenerateEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final Block transformBlock;
	private Material transform;

	public HiddenOreGenerateEvent(final Player player, final Block transformBlock, Material transform) {
		super(false);
		this.player = player;
		this.transformBlock = transformBlock;
		this.transform = transform;
	}

	private boolean cancel = false;

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancel = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return HiddenOreGenerateEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}

	public Block getBlock() {
		return transformBlock;
	}

	public Material getTransform() {
		return transform;
	}

	public void setTransform(Material transform) {
		if (transform == this.transform) return;
		this.transform = transform;
	}

}
