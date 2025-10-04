package com.untamedears.itemexchange.events;

import com.untamedears.itemexchange.rules.TradeRule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.civmodcore.inventory.items.ItemStash;

public class SuccessfulPurchaseEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final TradeRule trade;
    private final ItemStash input;
    private final ItemStash output;

    private SuccessfulPurchaseEvent(Player player, TradeRule trade, ItemStash input, ItemStash output) {
        this.player = player;
        this.trade = trade;
        this.input = input;
        this.output = output;
    }

    public Player getPurchaser() {
        return this.player;
    }

    public TradeRule getTrade() {
        return this.trade;
    }

    public ItemStash getPaymentItems() {
        return this.input;
    }

    public ItemStash getPurchasedItems() {
        return this.output;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static SuccessfulPurchaseEvent emit(Player player, TradeRule trade, ItemStash input, ItemStash output) {
        SuccessfulPurchaseEvent event = new SuccessfulPurchaseEvent(player, trade, input, output);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

}
