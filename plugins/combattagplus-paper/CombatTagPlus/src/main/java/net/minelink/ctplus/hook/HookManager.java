package net.minelink.ctplus.hook;

import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HookManager {

    private final List<Hook> hooks = new ArrayList<>();

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
        for (Hook hook : hooks) {
            if (!hook.isPvpEnabledAt(location)) {
                return false;
            }
        }
        return true;
    }

}
