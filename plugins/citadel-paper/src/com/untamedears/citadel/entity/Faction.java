package com.untamedears.citadel.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.untamedears.citadel.Citadel;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 1:14 AM
 */
@Entity
public class Faction implements Serializable, Comparable {

	private static final long serialVersionUID = -1660123901051487634L;
    public static final byte kDisabledFlag = 0x01;
    public static final byte kDeletedFlag = 0x02;
    public static final byte kFlagMask = kDisabledFlag | kDeletedFlag;
    public static final String kDisciplineMsg = "The group is under administrative discipline";

	@Id private String name;
	@Transient private String normalized_name;
    private String founder;
    private String password;

    @Version
    @Column(name="version")
    private int dbRowVersion;  // Do not touch

    @Column(name="discipline_flags", nullable=false)
    private Integer disciplineFlags;

    public Faction() {
        this.name = "";
        this.normalized_name = "";
        this.founder = "";
        this.disciplineFlags = 0;
    }

    public Faction(String name, String founder) {
        this.name = name;
        this.normalized_name = name.toLowerCase();
        this.founder = founder;
        this.disciplineFlags = 0;
    }

    // Do not touch
    public int getDbRowVersion() { return this.dbRowVersion; }
    public void setDbRowVersion(int value) { this.dbRowVersion = value; }
    // Do not touch

    public void Copy(Faction other) {
        this.setFounder(other.getFounder());
        this.setPassword(other.getPassword());
        this.setDisciplineFlags(other.getDisciplineFlags());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.normalized_name = name.toLowerCase();
    }

    public String getNormalizedName() {
        return this.normalized_name;
    }

    public String getFounder() {
        return founder;
    }

    public void setFounder(String founder) {
        this.founder = founder;
    }
    
    public String getPassword(){
    	return password;
    }
    
    public void setPassword(String password){
    	this.password = password;
    }

    // Don't get/set this.disciplineFlags outside of these getter/setters
    //  even when accessing from inside the class
    public Integer getDisciplineFlags() {
        return this.disciplineFlags & kFlagMask;
    }

    public void setDisciplineFlags(Integer flags) {
        this.disciplineFlags = flags & kFlagMask;
    }

    public boolean isDisabled() {
        return (getDisciplineFlags() & kDisabledFlag) != 0;
    }

    public void setDisabled(boolean set) {
        Integer flag = getDisciplineFlags();
        if (set) {
            flag |= kDisabledFlag;
        } else {
            flag &= ~kDisabledFlag;
        }
        setDisciplineFlags(flag);
    }

    public boolean isDeleted() {
        return (getDisciplineFlags() & kDeletedFlag) != 0;
    }

    public void setDeleted(boolean set) {
        Integer flag = getDisciplineFlags();
        if (set) {
            flag |= kDeletedFlag;
        } else {
            flag &= ~kDeletedFlag;
        }
        setDisciplineFlags(flag);
    }

    public boolean isDisciplined() {
        return getDisciplineFlags() != 0;
    }

    public boolean isFounder(String memberName){
    	return memberName.equalsIgnoreCase(this.founder);
    }

    public boolean isMember(String memberName) {
    	return Citadel.getGroupManager().hasGroupMember(this.name, memberName);
    }
    
    public boolean isModerator(String memberName){
    	return Citadel.getGroupManager().hasGroupModerator(this.name, memberName);
    }
    
    public boolean isPersonalGroup(){
    	PersonalGroup personalGroup = Citadel.getPersonalGroupManager().getPersonalGroup(this.founder);
    	if(personalGroup != null && personalGroup.getGroupName().equals(this.name)){
    		return true;
    	}
    	return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o == null) return false;
        if (!(o instanceof Faction)) return false;

        Faction faction = (Faction) o;
        return this.normalized_name.equals(faction.getNormalizedName());
    }

    @Override
    public int hashCode() {
        return this.normalized_name.hashCode();
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Faction)) {
            throw new ClassCastException();
        }
        Faction other = (Faction)o;
        return this.getNormalizedName().compareTo(other.getNormalizedName());
    }

    public static Faction getPersonalGroup(String player_name) {
       final PersonalGroup personalGroup =
           Citadel.getPersonalGroupManager().getPersonalGroup(player_name);
       final String personalGroupName = personalGroup.getGroupName();
       final Faction group = Citadel.getGroupManager().getGroup(personalGroupName);
       return group;
    }
}
