package com.untamedears.citadel.entity;

import com.untamedears.citadel.Citadel;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 1:14 AM
 */
@Entity
public class Faction {
    
    @Id private String name;
    private String founder;

    public Faction() {
    }

    public Faction(String name, String founder) {
        this.name = name;
        this.founder = founder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFounder() {
        return founder;
    }

    public void setFounder(String founder) {
        this.founder = founder;
    }

    public boolean hasMember(String memberName) {
        return Citadel.getInstance().dao.hasGroupMember(name, memberName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Faction)) return false;

        Faction faction = (Faction) o;

        return name.equals(faction.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
