package com.untamedears.citadel;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 2:42 AM
 */
public enum PlacementMode {
    // the normal state of things
    NORMAL,
    // hold reinforcement fortificationMaterial in hand and left click block to reinforce
    // optionally sets a security level
    REINFORCEMENT,
    // disables reinforcement mode after a single block
    REINFORCEMENT_SINGLE_BLOCK,
    // placed blocks are reinforced with chosen fortificationMaterial
    // optionally sets a security level
    FORTIFICATION,
    // punching blocks gives information about their reinforcement and access level
    INFO,
    // punching reinforcements toggles their insecure mode
    INSECURE
}
