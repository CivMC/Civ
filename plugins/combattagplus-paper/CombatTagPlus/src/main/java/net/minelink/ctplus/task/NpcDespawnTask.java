package net.minelink.ctplus.task;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.Npc;

import static com.google.common.base.Preconditions.*;

public class NpcDespawnTask implements Runnable {

    private final CombatTagPlus plugin;

    private final Npc npc;

    private long time;

    private int taskId;

    public NpcDespawnTask(CombatTagPlus plugin, Npc npc, long time) {
        this.plugin = checkNotNull(plugin, "Null plugin");
        this.npc = checkNotNull(npc, "Null npc");
        checkArgument(plugin.hasNpcs(), "The plugin doesn't have npcs enabled!");
        checkArgument(time >= 0, "Negative time %s", time);
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Npc getNpc() {
        return npc;
    }

    public void start() {
        taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 1, 1).getTaskId();
    }

    public void stop() {
        plugin.getServer().getScheduler().cancelTask(taskId);
    }

    @Override
    public void run() {
        // Do nothing if NPC should not despawn yet
        if (time > System.currentTimeMillis()) {
            return;
        }

        // Despawn the NPC
        plugin.getNpcManager().despawn(npc);
    }

}
