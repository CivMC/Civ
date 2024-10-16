package net.minelink.ctplus.event;

import net.minelink.ctplus.Npc;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class NpcDespawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Npc npc;
    private final NpcDespawnReason reason;

    public NpcDespawnEvent(Npc npc, NpcDespawnReason reason) {
        this.npc = npc;
        this.reason = reason;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Npc getNpc() {
        return npc;
    }
    
    public NpcDespawnReason getDespawnReason(){
    	return reason;
    }
}
