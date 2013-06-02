package com.untamedears.citadel.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */

@Entity
@Table(name="moderator",  uniqueConstraints={
		   @UniqueConstraint(columnNames={"faction_name", "member_name"})})
public class Moderator implements Comparable {

	@Id private String memberName;
	@Id private String factionName;
	
	public Moderator(){}
	
	public Moderator(String memberName, String factionName){
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
		return factionName;
	}
	
	public void setFactionName(String factionName){
		this.factionName = factionName;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Moderator)) return false;
        Moderator that = (Moderator) o;
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
        if (!(o instanceof Moderator)) {
            throw new ClassCastException();
        }
        Moderator other = (Moderator)o;
        int compare = this.getFactionName().compareTo(other.getFactionName());
        if (compare != 0) {
            return compare;
        }
        return this.getMemberName().compareTo(other.getMemberName());
    }
}
