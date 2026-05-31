package net.civmc.zorweth.research;

import java.util.Objects;
import net.civmc.zorweth.ZorwethPlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ResearchManager {

    private static final NamespacedKey PHASE_ONE_RUNS_KEY = new NamespacedKey("zorweth", "research_phase_one_runs");
    private static final NamespacedKey PHASE_TWO_RUNS_KEY = new NamespacedKey("zorweth", "research_phase_two_runs");

    private final ZorwethPlugin plugin;
    private final boolean enabled;
    private final int phaseOneRequiredRuns;
    private final int phaseTwoRequiredRuns;

    public ResearchManager(final ZorwethPlugin plugin, final boolean enabled, final int phaseOneRequiredRuns,
                           final int phaseTwoRequiredRuns) {
        this.plugin = Objects.requireNonNull(plugin);
        this.enabled = enabled;
        this.phaseOneRequiredRuns = phaseOneRequiredRuns;
        this.phaseTwoRequiredRuns = phaseTwoRequiredRuns;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isResearchWorld(final World world) {
        return world != null && world.getName().equals(this.plugin.getSourceWorld());
    }

    public boolean canRunResearch(final World world, final int phase) {
        if (!this.enabled || !isResearchWorld(world)) {
            return false;
        }
        final PersistentDataContainer data = world.getPersistentDataContainer();
        final int phaseOneCompleted = getRuns(data, PHASE_ONE_RUNS_KEY);
        final int phaseTwoCompleted = getRuns(data, PHASE_TWO_RUNS_KEY);
        return switch (phase) {
            case 1 -> phaseOneCompleted < this.phaseOneRequiredRuns;
            case 2 -> phaseOneCompleted >= this.phaseOneRequiredRuns && phaseTwoCompleted < this.phaseTwoRequiredRuns;
            default -> false;
        };
    }

    public boolean isResearchComplete() {
        return !this.enabled || getResearchProgress().complete();
    }

    public void recordResearchRun(final World world, final int phase) {
        if (!canRunResearch(world, phase)) {
            return;
        }
        final PersistentDataContainer data = world.getPersistentDataContainer();
        final NamespacedKey key = phase == 1 ? PHASE_ONE_RUNS_KEY : PHASE_TWO_RUNS_KEY;
        data.set(key, PersistentDataType.INTEGER, getRuns(data, key) + 1);
    }

    public ResearchProgress getResearchProgress() {
        final World world = Bukkit.getWorld(this.plugin.getSourceWorld());
        if (world == null) {
            return new ResearchProgress(1, 0, this.phaseOneRequiredRuns, false);
        }
        final PersistentDataContainer data = world.getPersistentDataContainer();
        final int phaseOneCompleted = getRuns(data, PHASE_ONE_RUNS_KEY);
        if (phaseOneCompleted < this.phaseOneRequiredRuns) {
            return new ResearchProgress(1, phaseOneCompleted, this.phaseOneRequiredRuns, false);
        }
        final int phaseTwoCompleted = getRuns(data, PHASE_TWO_RUNS_KEY);
        if (phaseTwoCompleted < this.phaseTwoRequiredRuns) {
            return new ResearchProgress(2, phaseTwoCompleted, this.phaseTwoRequiredRuns, false);
        }
        return new ResearchProgress(2, this.phaseTwoRequiredRuns, this.phaseTwoRequiredRuns, true);
    }

    private int getRuns(final PersistentDataContainer data, final NamespacedKey key) {
        return Math.max(0, data.getOrDefault(key, PersistentDataType.INTEGER, 0));
    }
}
