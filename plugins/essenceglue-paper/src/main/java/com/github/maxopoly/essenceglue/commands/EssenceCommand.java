package com.github.maxopoly.essenceglue.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.github.maxopoly.essenceglue.VirtualEssenceManager;
import java.util.Collection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

@CommandAlias("essence")
public class EssenceCommand extends BaseCommand {

    private static final TextColor NICE_BLUE = TextColor.color(55, 159, 163);

    private final VirtualEssenceManager manager;
    private final ItemMap essence;

    public EssenceCommand(VirtualEssenceManager manager, ItemMap essence) {
        this.manager = manager;
        this.essence = essence;
    }

    @Default
    public void onDefault(Player sender) {
        sender.sendMessage(Component.empty().color(NICE_BLUE)
            .append(Component.text("You have "))
            .append(Component.text(manager.getEssence(sender.getUniqueId()), NamedTextColor.AQUA))
            .append(Component.text(" essence. Use "))
            .append(Component.text("/essence withdraw", NamedTextColor.AQUA))
            .append(Component.text(" to materialise all of your essence as an item.")));
    }

    @Subcommand("withdraw")
    public void onWithdraw(Player sender) {
        int count = manager.getEssence(sender.getUniqueId());

        if (count > 0) {
            for (ItemStack item : this.essence.getItemStackRepresentation()) {
                ItemStack clonedItem = item.clone();
                clonedItem.setAmount(count);

                Collection<ItemStack> couldNotAdd = sender.getInventory().addItem(clonedItem).values();
                int couldNotAddCount = 0;
                for (ItemStack i : couldNotAdd) {
                    couldNotAddCount += i.getAmount() / item.getAmount();
                }
                manager.setEssence(sender.getUniqueId(), couldNotAddCount);
            }
            sender.sendMessage(Component.empty().color(NICE_BLUE)
                .append(Component.text("You have withdrawn "))
                .append(Component.text(count, NamedTextColor.AQUA))
                .append(Component.text(" essence.")));
        } else {
            sender.sendMessage(Component.empty().color(NamedTextColor.RED)
                .append(Component.text("You do not have any essence to withdraw!")));
        }
    }
}
