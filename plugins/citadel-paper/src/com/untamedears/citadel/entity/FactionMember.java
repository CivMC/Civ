package com.untamedears.citadel.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * User: JonnyD & chrisrico
 * Date: 7/18/12
 * Time: 11:57 PM
 */

@Entity
@Table(name="faction_member",  uniqueConstraints={
		   @UniqueConstraint(columnNames={"faction_name", "member_name"})})
public class FactionMember implements Comparable {
	@Id private String factionName;
	@Id private String memberName;
	
	public FactionMember(){}
	
	public FactionMember(String memberName, String factionName){
		this.memberName = memberName;
		this.factionName = factionName;
	}

	public String getMemberName(){
		return this.memberName;
	}
	
	public void setMemberName(String memberName){
		this.memberName = memberName;
	}
	
	public String getFactionName(){
		return this.factionName;
	}
	
	public void setFactionName(String factionName){
		this.factionName = factionName;
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

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof FactionMember)) {
            throw new ClassCastException();
        }
        FactionMember other = (FactionMember)o;
        int compare = this.getFactionName().compareTo(other.getFactionName());
        if (compare != 0) {
            return compare;
        }
        return this.getMemberName().compareTo(other.getMemberName());
    }
}
