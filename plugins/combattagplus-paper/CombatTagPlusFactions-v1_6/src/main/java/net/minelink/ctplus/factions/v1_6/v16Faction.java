package net.minelink.ctplus.factions.v1_6;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import net.minelink.ctplus.hook.factions.FactionRelation;

import static com.google.common.base.Preconditions.*;

public class v16Faction implements net.minelink.ctplus.hook.factions.Faction {
    private final Faction handle;

    v16Faction(Faction handle) {
        this.handle = checkNotNull(handle, "Null faction");
        checkArgument(handle.isNormal(), "Abnormal faction %s", handle);
    }

    @Override
    public String getId() {
        return handle.getId();
    }

    @Override
    public String getName() {
        return handle.getTag();
    }

    @Override
    public boolean isMember(UUID playerId) {
        return FPlayers.getInstance().getById(checkNotNull(playerId, "Null playerId").toString()).getFaction().getId().equals(handle.getId());
    }

    @Override
    public Set<UUID> getMembers() {
        return handle.getFPlayers().stream().map(FPlayer::getId).map(UUID::fromString).collect(Collectors.toSet());
    }

    @Override
    public FactionRelation getRelationTo(net.minelink.ctplus.hook.factions.Faction other) {
        return other instanceof v16Faction ? FactionsHook.toAPIRelation(this.handle.getRelationTo(((v16Faction) other).handle)) : FactionRelation.NEUTRAL;
    }

    @Override
    public String toString() {
        return getName();
    }

    // Faction.class doesn't override equals and hash code like it should

    @Override
    public int hashCode() {
        return handle.getTag().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return handle == this || obj != null && obj instanceof v16Faction && this.getName().equals(((v16Faction) obj).getName());
    }



}
