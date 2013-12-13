package com.untamedears.citadel.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "db_version")
public class DbVersion implements Serializable {
  private static final long serialVersionUID = 740812643372L;

  // DB Columns
  @Id
  @Column(name="db_version")
  private Integer dbVersion;

  private String updateTime;


  public DbVersion() {}

  public Integer getDbVersion() {
    return this.dbVersion;
  }

  public void setDbVersion(Integer val) {
    this.dbVersion = val;
  }

  public String getUpdateTime() {
    return this.updateTime;
  }

  public void setUpdateTime(String val) {
    this.updateTime = val;
  }
}
