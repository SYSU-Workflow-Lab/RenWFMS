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

    @Column(name = "business")
    private double business;

    @Column(name = "cpu_occupancy_rate")
    private double cpuOccupancyRate;

    @Column(name = "memory_occupancy_rate")
    private double memoryOccupancyRate;

    @Column(name = "tomcat_concurrency")
    private double tomcatConcurrency;

    @Column(name = "workitem_count")
    private double workitemCount;

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

    public double getBusiness() {
        return business;
    }

    public void setBusiness(double business) {
        this.business = business;
    }

    public double getCpuOccupancyRate() {
        return cpuOccupancyRate;
    }

    public void setCpuOccupancyRate(double cpuOccupancyRate) {
        this.cpuOccupancyRate = cpuOccupancyRate;
    }

    public double getMemoryOccupancyRate() {
        return memoryOccupancyRate;
    }

    public void setMemoryOccupancyRate(double memoryOccupancyRate) {
        this.memoryOccupancyRate = memoryOccupancyRate;
    }

    public double getTomcatConcurrency() {
        return tomcatConcurrency;
    }

    public void setTomcatConcurrency(double tomcatConcurrency) {
        this.tomcatConcurrency = tomcatConcurrency;
    }

    public double getWorkitemCount() {
        return workitemCount;
    }

    public void setWorkitemCount(double workitemCount) {
        this.workitemCount = workitemCount;
    }

    public void updateBusiness() {
        this.business = cpuOccupancyRate + memoryOccupancyRate + tomcatConcurrency + workitemCount / 10000.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RenServiceInfo that = (RenServiceInfo) o;
        return isActive == that.isActive &&
                Double.compare(that.business, business) == 0 &&
                Double.compare(that.cpuOccupancyRate, cpuOccupancyRate) == 0 &&
                Double.compare(that.memoryOccupancyRate, memoryOccupancyRate) == 0 &&
                Double.compare(that.tomcatConcurrency, tomcatConcurrency) == 0 &&
                Double.compare(that.workitemCount, workitemCount) == 0 &&
                Objects.equals(interpreterId, that.interpreterId) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interpreterId, location, isActive, business, cpuOccupancyRate, memoryOccupancyRate, tomcatConcurrency, workitemCount);
    }

    @Override
    public String toString() {
        return "RenServiceInfo{" +
                "interpreterId='" + interpreterId + '\'' +
                ", location='" + location + '\'' +
                ", isActive=" + isActive +
                ", business=" + business +
                ", cpuOccupancyRate=" + cpuOccupancyRate +
                ", memoryOccupancyRate=" + memoryOccupancyRate +
                ", tomcatConcurrency=" + tomcatConcurrency +
                ", workitemCount=" + workitemCount +
                '}';
    }
}
