package org.sysu.renCommon.entity;

import javax.persistence.*;
import java.util.Objects;

/**
 * Created by Skye on 2019/1/2.
 */
@Entity
@Table(name = "ren_serviceinfo", schema = "renboengine")
public class RenServiceInfo {

    @Id
    @Column(name = "interpreter_id", nullable = false, length = 64)
    private String interpreterId;

    @Basic
    @Column(name = "location", nullable = false, length = 64)
    private String location;

    @Basic
    @Column(name = "is_active", nullable = false)
    private int isActive;

    public RenServiceInfo() {
    }

    public RenServiceInfo(String interpreterId, String location) {
        this.interpreterId = interpreterId;
        this.location = location;
        this.isActive = 1;
    }

    public String getInterpreterId() {
        return interpreterId;
    }

    public void setInterpreterId(String interpreterId) {
        this.interpreterId = interpreterId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RenServiceInfo that = (RenServiceInfo) o;
        return isActive == that.isActive &&
                Objects.equals(interpreterId, that.interpreterId) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interpreterId, location, isActive);
    }

    @Override
    public String toString() {
        return "RenServiceInfo{" +
                "interpreterId='" + interpreterId + '\'' +
                ", location='" + location + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
