package com.untamedears.itemexchange.rules.interfaces;

public class BaseRule {

    private boolean locked;

    public final void lock() {
        this.locked = true;
        onLock();
    }

    protected void onLock() {
    }

    protected final void checkLocked() {
        if (this.locked) {
            throw new UnsupportedOperationException("Cannot modify that, it has been locked.");
        }
    }

}
