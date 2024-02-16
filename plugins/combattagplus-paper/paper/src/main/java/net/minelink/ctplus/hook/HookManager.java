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
        PvpBlock pvpBlock;

        synchronized (pvpBlocks) {
            pvpBlock = pvpBlocks.get(position);

            if (pvpBlock != null && pvpBlock.expiry > currentTime) {
                return pvpBlock.enabled;
            }

            pvpBlock = new PvpBlock(currentTime + 60000);
            pvpBlocks.put(position, pvpBlock);
        }

        for (Hook hook : hooks) {
            if (!hook.isPvpEnabledAt(location)) {
                pvpBlock.enabled = false;
                break;
            }
        }

        return pvpBlock.enabled;
    }

    private static class PvpBlock {

        private final long expiry;

        private boolean enabled = true;

        PvpBlock(long expiry) {
            this.expiry = expiry;
        }

    }

}
