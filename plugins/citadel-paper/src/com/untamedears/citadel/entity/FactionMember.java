package com.untamedears.citadel.entity;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/19/12
 * Time: 5:13 PM
 */
@Entity
public class FactionMember {

    @Id private String factionName;
    @Id private String memberName;

    public FactionMember() {
    }

    public FactionMember(String factionName, String memberName) {
        this.factionName = factionName;
        this.memberName = memberName;
    }

    public String getFactionName() {
        return factionName;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FactionMember)) return false;

        FactionMember that = (FactionMember) o;

        return factionName.equals(that.factionName) && memberName.equals(that.memberName);

    }

    @Override
    public int hashCode() {
        int result = factionName.hashCode();
        result = 31 * result + memberName.hashCode();
        return result;
    }
}
