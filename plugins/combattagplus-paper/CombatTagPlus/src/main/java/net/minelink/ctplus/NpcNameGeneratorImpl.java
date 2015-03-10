package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcNameGenerator;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.Random;

public class NpcNameGeneratorImpl implements NpcNameGenerator {

    private static final Random random = new Random();

    private final CombatTagPlus plugin;

    public NpcNameGeneratorImpl(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String generate(Player player) {
        if (!plugin.getSettings().generateRandomName()) {
            return player.getName();
        }

        String name = plugin.getSettings().getRandomNamePrefix();
        name = name.length() > 12 ? name.substring(0, 12) : name;

        int maxRandom = Integer.valueOf("1" + StringUtils.repeat("0", Math.min(4, 16 - name.length() - 1)));
        return name + random.nextInt(maxRandom);
    }

}
