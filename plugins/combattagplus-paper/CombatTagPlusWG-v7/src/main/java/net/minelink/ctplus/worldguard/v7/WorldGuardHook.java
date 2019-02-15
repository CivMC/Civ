package net.minelink.ctplus.worldguard.v7;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.minelink.ctplus.hook.Hook;
import org.bukkit.Location;

public final class WorldGuardHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location loc) {
        StateFlag.State pvpflag = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(loc), Associables.constant(Association.NON_MEMBER), Flags.PVP);
        return !(pvpflag != null && pvpflag.equals(StateFlag.State.DENY));
    }
}
