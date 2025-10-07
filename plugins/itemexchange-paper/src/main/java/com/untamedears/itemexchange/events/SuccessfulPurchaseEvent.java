package com.untamedears.itemexchange.events;

import com.untamedears.itemexchange.rules.TradeRule;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuccessfulPurchaseEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final TradeRule trade;
    private final List<ItemStack> input;
    private final List<ItemStack> output;

    private SuccessfulPurchaseEvent(Player player, TradeRule trade, List<ItemStack> input, List<ItemStack> output) {
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

    public @NotNull List<@NotNull ItemStack> getPaymentItems() {
        return this.input;
    }

    public @Nullable List<@NotNull ItemStack> getPurchasedItems() {
        return this.output;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static SuccessfulPurchaseEvent emit(Player player, TradeRule trade, List<ItemStack> input, List<ItemStack> output) {
        SuccessfulPurchaseEvent event = new SuccessfulPurchaseEvent(player, trade, input, output);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

}
