package net.minelink.ctplus.hook;

import net.minelink.ctplus.BlockPosition;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.util.LruCache;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HookManager {

    private final List<Hook> hooks = new ArrayList<>();

    private final LruCache<BlockPosition, PvpBlock> pvpBlocks = new LruCache<>(100000);

    private final CombatTagPlus plugin;

    public HookManager(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    public boolean addHook(Hook hook) {
        return hooks.add(hook);
    }

    public boolean removeHook(Hook hook) {
        return hooks.remove(hook);
    }

    public List<Hook> getHooks() {
        return Collections.unmodifiableList(hooks);
    }

    public boolean isPvpEnabledAt(Location location) {
        long currentTime = System.currentTimeMillis();
        BlockPosition position = new BlockPosition(location);
        PvpBlock pvpBlock = pvpBlocks.get(position);

        if (pvpBlock != null && pvpBlock.expiry > currentTime) {
            return pvpBlock.enabled;
        }

        boolean result = true;
        for (Hook hook : hooks) {
            if (!hook.isPvpEnabledAt(location)) {
                result = false;
                break;
            }
        }

        pvpBlocks.put(position, new PvpBlock(result, currentTime + 60000));
        return result;
    }

    private static class PvpBlock {

        private final boolean enabled;

        private final long expiry;

        PvpBlock(boolean enabled, long expiry) {
            this.enabled = enabled;
            this.expiry = expiry;
        }

    }

}
