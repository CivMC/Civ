package com.untamedears.citadel.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "faction_delete")
public class FactionDelete implements Serializable {
  private static final long serialVersionUID = 54271370768632792L;

  // DB Columns
  @Id
  @Column(name="deleted_faction")
  private String deletedFaction;

  @Column(name="personal_group")
  private String personalGroup;


  public FactionDelete() {}

  public String getDeletedFaction() {
    return this.deletedFaction;
  }

  public void setDeletedFaction(String val) {
    this.deletedFaction = val;
  }

  public String getPersonalGroup() {
    return this.personalGroup;
  }

  public void setPersonalGroup(String val) {
    this.personalGroup = val;
  }
}
