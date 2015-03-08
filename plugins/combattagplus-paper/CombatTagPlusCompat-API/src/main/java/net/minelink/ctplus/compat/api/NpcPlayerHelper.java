package net.minelink.ctplus.compat.api;

import org.bukkit.entity.Player;

public interface NpcPlayerHelper {

    Player spawn(Player player);

    void despawn(Player player);

    boolean isNpc(Player player);

    NpcIdentity getIdentity(Player player);

    void updateEquipment(Player player);

    void syncOffline(Player player);

    void createPlayerList(Player player);

    void removePlayerList(Player player);

}
