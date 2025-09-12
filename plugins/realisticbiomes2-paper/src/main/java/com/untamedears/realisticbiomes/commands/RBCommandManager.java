package com.untamedears.realisticbiomes.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import java.util.ArrayList;
import java.util.Arrays;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public class RBCommandManager extends CommandManager {

    public RBCommandManager(@NotNull Plugin plugin) {
        super(plugin);
        init();
    }

    @Override
    public void registerCommands() {
        registerCommand(new Menu());
    }

    @Override
    public void registerCompletions(@NotNull CommandCompletions<BukkitCommandCompletionContext> completions) {
        super.registerCompletions(completions);
        completions.registerCompletion("RB_Biomes", (context) -> RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).stream().map(Biome::getKey).map(NamespacedKey::value).toList());
    }
}
