package com.devotedmc.ExilePearl.listener;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class BanStickListener extends RuleListener {

    public BanStickListener(ExilePearlApi pearlApi) {
        super(pearlApi);
    }

    /**
     * Prevents alts from logging in if the limit of pearled accounts is reached
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoginEvent(AsyncPlayerPreLoginEvent e) {
        if (pearlApi.getPearl(e.getUniqueId()) != null) {
            // dont lock out pearled account
            return;
        }
        if (getPearledAlts(e.getUniqueId()) >= config.maxAltsPearled()) {
            e.setLoginResult(Result.KICK_OTHER);
            e.setKickMessage(config.altBanMessage());
        }
    }

    /**
     * Kicks online alts if the limit of pearled accounts is reached
     *
     * @param e
     */
    public void playerPearl(PlayerPearledEvent e) {
        UUID uuid = e.getPearl().getPlayerId();
        if (getPearledAlts(uuid) < config.maxAltsPearled()) {
            return;
        }
        BSPlayer player = BSPlayer.byUUID(uuid);
        for (BSPlayer alt : player.getTransitiveSharedPlayers(true)) {
            ExilePearl altPearl = pearlApi.getPearl(alt.getUUID());
            if (altPearl == null && !alt.getUUID().equals(uuid)) {
                Player p = Bukkit.getPlayer(alt.getUUID());
                if (p != null) {
                    p.kickPlayer(config.altBanMessage());
                }
            }
        }
    }

    /**
     * Calculates how many of a players alts are pearled
     *
     * @param uuid
     *            UUID of the player
     * @return Count of pearled alts or -1 if a banstick internal error occured
     */
    private int getPearledAlts(UUID uuid) {
        BSPlayer player = BSPlayer.byUUID(uuid);
        if (player == null) {
            return -1;
        }
        int pearledAlts = 0;
        for (BSPlayer alt : player.getTransitiveSharedPlayers(true)) {
            ExilePearl altPearl = pearlApi.getPearl(alt.getUUID());
            if (altPearl != null && !alt.getUUID().equals(player.getUUID())) {
                pearledAlts++;
            }
        }
        return pearledAlts;
    }

}
