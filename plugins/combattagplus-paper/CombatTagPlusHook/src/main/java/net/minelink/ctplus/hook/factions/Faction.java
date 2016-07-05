package net.minelink.ctplus.hook.factions;

import java.util.Set;
import java.util.UUID;

public interface Faction {

    String getId();

    String getName();

    boolean isMember(UUID playerId);

    Set<UUID> getMembers();

    FactionRelation getRelationTo(Faction other);
}
