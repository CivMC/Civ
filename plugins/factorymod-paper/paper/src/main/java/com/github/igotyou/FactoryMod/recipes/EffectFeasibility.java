package com.github.igotyou.FactoryMod.recipes;

import javax.annotation.Nullable;

/**
 * Captures the feasibility of applying a recipe effect along with an optional reason string.
 * The reason string should be plaintext and formatted such that it can be inserted in a
 * player message that reads like "The factory couldn't run because {reasonSnippet}."
 */
public record EffectFeasibility(boolean isFeasible, @Nullable String reasonSnippet) { }
