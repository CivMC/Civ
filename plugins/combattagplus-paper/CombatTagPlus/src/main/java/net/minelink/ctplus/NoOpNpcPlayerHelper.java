package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcIdentity;
import net.minelink.ctplus.compat.api.NpcPlayerHelper;

import org.bukkit.entity.Player;

public class NoOpNpcPlayerHelper implements NpcPlayerHelper {
    private NoOpNpcPlayerHelper() {}

    public static final NoOpNpcPlayerHelper INSTANCE = new NoOpNpcPlayerHelper();

    @Override
    public boolean isNpc(Player player) {
        return false;
    }

    // Unsupported methods

    @Override
    public Player spawn(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void despawn(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NpcIdentity getIdentity(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEquipment(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void syncOffline(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createPlayerList(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePlayerList(Player player) {
        throw new UnsupportedOperationException();
    }
}
