package com.untamedears.jukealert.broadcaster;

public class NoopSnitchAlertBroadcaster implements SnitchAlertBroadcaster {

    @Override
    public void broadcast(final RemoteSnitchAlert alert) {
    }
}
