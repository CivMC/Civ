package com.untamedears.realisticbiomes.noise;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import com.untamedears.realisticbiomes.breaker.BreakManager;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SetYieldCommand extends BaseCommand {

    private final BiomeConfiguration biomes;

    public SetYieldCommand(BiomeConfiguration biomes) {
        this.biomes = biomes;
    }

    @CommandAlias("rbsetyield")
    @CommandPermission("realisticbiomes.rbsetyield")
    public void measure(Player player, double yield) {
        biomes.setYieldOverride(yield);
        player.sendMessage("Set yield to " + yield);
    }
}
