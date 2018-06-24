package com.devotedmc.ExilePearl.listener;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class BanStickListener extends RuleListener {

    public BanStickListener(ExilePearlApi pearlApi) {
        super(pearlApi);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoginEvent(AsyncPlayerPreLoginEvent e) {
        BSPlayer player = BSPlayer.byUUID(e.getUniqueId());
        if (player == null) {
            return;
        }
        int pearledAlts = 0;
        for(BSPlayer alt : player.getTransitiveSharedPlayers(true)) {
            ExilePearl pearl = pearlApi.getPearl(alt.getUUID());
            if (pearl != null && !alt.getUUID().equals(player.getUUID())) {
                pearledAlts++;
            }
        }
        if (pearledAlts >= config.maxAltsPearled()) {
            e.setLoginResult(Result.KICK_OTHER);
            e.setKickMessage(config.altBanMessage());
        }
    }

}
