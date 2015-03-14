package net.minelink.ctplus.event;

import net.minelink.ctplus.Npc;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class NpcDespawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Npc npc;

    public NpcDespawnEvent(Npc npc) {
        this.npc = npc;
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

}
