package com.untamedears.itemexchange.rules.interfaces;

public abstract class ModifierData extends ExchangeData implements Comparable<ModifierData> {

    private final String slug;
    private final int order;

    public ModifierData(String slug, int order) {
        this.slug = slug;
        this.order = order;
    }

    public final String getSlug() {
        return this.slug;
    }

    public final int compareTo(ModifierData other) {
        return Integer.compare(this.order, other.order);
    }

}
