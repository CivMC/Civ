package com.untamedears.citadel;

import com.avaje.ebean.annotation.EnumValue;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/19/12
 * Time: 5:12 PM
 */
public enum SecurityLevel {
    @EnumValue("0")
    PUBLIC,
    @EnumValue("1")
    PRIVATE,
    @EnumValue("2")
    GROUP,
    // Used when auto-generating a reinforcement in the world
    @EnumValue("3")
    GENERATED
}
