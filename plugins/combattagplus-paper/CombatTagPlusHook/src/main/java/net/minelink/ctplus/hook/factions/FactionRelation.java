package net.minelink.ctplus.hook.factions;

import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.*;

/**
 * The relationship between two factions or players.
 * <p>
 * Both factions must agree in order to increase their relationship level (become more friendly).
 * Either faction may choose to decrease their relationship level (become less friendly).
 * Relationships are comparable, and are considered greater if the relationship is more
 * Factions are {@link #NEUTRAL} by default.
 * </p>
 */
public enum FactionRelation {
     /*
      * We want to order relations by the 'strength' of the relation, so that ALLY > ENEMY.
      * However enum ids count up, so we actually need to put less frendly relations first.
      * This makes sure that ENEMY < NEUTRAL since ENEMY is 0 and NEUTRAL is 1.
      */
    /**
     * The two factions or player are enemies
     * <p>Either faction may choose to become enemies with the other, with no agreement needed.</p>
     */
    ENEMY,
    /**
     * The two factions or players are neutral, and have no relationship set.
     * <p>Factions are neutral by default.</p>
     */
    NEUTRAL,
    /**
     * The two factions or players are in a 'truce', and have agreed not to attack each other.
     * <p>Levels greater than or equal to this indicate the factions/entities can't attack each other.</p>
     */
    TRUCE,
    /**
     * The two factions or players are in an alliance.
     */
    ALLY,
    /**
     * The players are members of the same faction, or the factions are equal.
     */
    MEMBER;

    /**
     * Get a numeric representation of the 'level' of this relationship indicating its friendlies.
     * <p>Higher numbers indicate a better relationship, while lower numbers indicate a worse relationship.</p>
     *
     * @return the level
     */
    public int getLevel() {
        return ordinal();
    }

    /**
     * If the relationship allows attacking other people in the location
     */
    public boolean mayAttack() {
        return getLevel() >= TRUCE.getLevel();
    }
}
