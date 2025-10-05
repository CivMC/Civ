package net.civmc.kitpvp.kit;

public enum KitDrugs {
    Cyanide("Cyanide", 0),
    Cannabis("Cannabis", 6),
    Meth("Meth", 6),
    Blue_Meth("Blue Meth", 6),
    Heroin("Heroin", 6),
    Oestrogen("Oestrogen", 6),
    Caffeine("Caffeine", 6),
    Ivermectin("Ivermectin", 6),
    DMT("DMT", 6),
    Xanax("Xanax", 6),
    Steroids("Steroids", 6),
    Testosterone("Testosterone", 6),
    Vicodin("Vicodin", 6),
    Yakult("Yakult", 6),
    Cocaine("Cocaine", 6),
    Speed("Speed", 6),
    NAD("NAD+", 6),
    Epinephrine("Epinephrine", 6),
    Firefoam("Firefoam", 6),
    Nitroglycerin("Nitroglycerin", 6),
    ;

    private final String item;
    private final int cost;

    KitDrugs(String item, int cost) {
        this.item = item;
        this.cost = cost;
    }

    public String getBrew() {
        return item;
    }

    public int getCost() {
        return cost;
    }
}
