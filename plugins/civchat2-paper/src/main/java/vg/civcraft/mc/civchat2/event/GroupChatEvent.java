package vg.civcraft.mc.civchat2.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class GroupChatEvent extends PlayerEvent implements Cancellable {

    private final String groupName;

    private final Component message;

    private boolean cancelled;

    // Handler list for spigot events
    private static final HandlerList handlers = new HandlerList();

    public GroupChatEvent(final Player player, final String groupName, final Component message) {
        super(player);

        this.groupName = groupName;
        this.message = message;
    }

    /**
     * Gets the group name
     *
     * @return The group name
     */
    public String getGroup() {

        return groupName;
    }

    /**
     * Gets the chat message
     *
     * @return The chat message
     */
    public Component getMessage() {
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
