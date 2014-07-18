package com.untamedears.citadel;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.untamedears.citadel.dao.CitadelDao;
 
public class AccountIdManager implements Listener {
  public AccountIdManager() {}

  public void initialize(CitadelDao dao) {
    dao_ = dao;
    accountIdCache_ = dao_.loadAccountIdMap();
    playerNameCache_ = new TreeMap<String, UUID>();
    for (Map.Entry<UUID, String> entry : accountIdCache_.entrySet()) {
        playerNameCache_.put(entry.getValue().toLowerCase(), entry.getKey());
    }
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event) {
    final Player player = event.getPlayer();
    final String playerName = player.getName();
    final UUID accountId = player.getUniqueId();
    final String associatedName = accountIdCache_.get(accountId);
    if (associatedName != null && associatedName.equalsIgnoreCase(playerName)) {
      return;
    }
    accountIdCache_.put(accountId, playerName);
    playerNameCache_.put(playerName.toLowerCase(), accountId);
    dao_.associatePlayerAccount(accountId, playerName);
  }

  public String getPlayerName(String accountId) {
    UUID id;
    try {
        id = UUID.fromString(accountId);
    } catch (Exception ex) {
        return null;
    }
    return accountIdCache_.get(id);
  }

  public String getPlayerName(UUID accountId) {
    return accountIdCache_.get(accountId);
  }

  public Player getPlayer(UUID accountId) {
    final String playerName = getPlayerName(accountId);
    if (playerName == null) {
      return null;
    }
    return Bukkit.getPlayerExact(playerName);
  }

  public UUID getAccountId(String playerName) {
      return playerNameCache_.get(playerName.toLowerCase());
  }

  public UUID getAccountIdFromLc(String lcPlayerName) {
      return playerNameCache_.get(lcPlayerName);
  }

  CitadelDao dao_;
  private Map<UUID, String> accountIdCache_;
  private Map<String, UUID> playerNameCache_;
}
