package com.untamedears.citadel.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AcidBlockDestroy extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	private boolean isCancelled = false;
	private String reason_ = "Another plugin cancelled this event.";
	private Block acidBlock;
	
	public AcidBlockDestroy(Block block){
		acidBlock = block;
	}
	
	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(boolean value) {
		isCancelled = value;
	}

	public HandlerList getHandlers() {
		return handlers;
	}
	
	public void setReasonForCancel(String reason){
		reason_ = reason;
	}

	public String getReasonForCancel(){
		return reason_;
	}
	
	public Block getAcidBlock(){
		return acidBlock;
	}
}
