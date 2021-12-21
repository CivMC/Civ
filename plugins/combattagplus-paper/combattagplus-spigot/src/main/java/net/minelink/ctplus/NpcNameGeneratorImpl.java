package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcNameGenerator;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;

public final class NpcNameGeneratorImpl implements NpcNameGenerator {

    private static final Random random = new Random();

    private final CombatTagPlus plugin;

    NpcNameGeneratorImpl(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String generate(Player player) {
        if (!plugin.getSettings().generateRandomName()) {
            return player.getName();
        }

        String prefix = plugin.getSettings().getRandomNamePrefix();
        prefix = prefix.length() > 12 ? prefix.substring(0, 12) : prefix;

        int max = Integer.valueOf("1" + StringUtils.repeat("0", Math.min(4, 16 - prefix.length() - 1)));
        String name = null;

        while (name == null || Bukkit.getPlayerExact(name) != null) {
            name = prefix + random.nextInt(max);
        }

        return name;
    }

}
