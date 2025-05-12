package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FMCommandManager extends CommandManager {

    public FMCommandManager(Plugin plugin) {
        super(plugin);
        init();
    }

    @Override
    public void registerCommands() {
        registerCommand(new CheatOutput());
        registerCommand(new Create());
        registerCommand(new FactoryMenu());
        registerCommand(new ItemUseMenu());
        registerCommand(new RunAmountSetterCommand());
    }

    @Override
    public void registerCompletions(@NotNull CommandCompletions<BukkitCommandCompletionContext> completions) {
        super.registerCompletions(completions);
        completions.registerCompletion("FM_Factories", (context) -> FactoryMod.getInstance().getManager().getAllFactoryEggs().stream().map(
            IFactoryEgg::getName).toList());
        completions.registerCompletion("materials_and_custom_items", (context) -> {
            Set<String> allCompletions = new TreeSet<>();
            for (Material m : Material.values()) {
                allCompletions.add(m.name().toLowerCase());
            }
            allCompletions.addAll(CustomItem.getKeys());
            return allCompletions;
        });
    }
}
