package com.untamedears.jukealert.broadcaster;

import java.util.UUID;

public record RemoteSnitchAlert(
    long sentAt,
    String actionIdentifier,
    UUID playerId,
    String playerName,
    String groupName,
    String snitchTypeName,
    String snitchName,
    String worldName,
    int x,
    int y,
    int z
) {
}
