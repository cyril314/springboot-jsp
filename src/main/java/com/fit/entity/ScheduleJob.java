package com.fit.entity;

import java.io.Serializable;

public class ScheduleJob implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String group;
    private String cronExpression;
    private String status;
    private String description;
    private String className;

    public ScheduleJob() {
    }

    public ScheduleJob(String name, String group, String cronExpression, String status, String description, String className) {
        this.name = name;
        this.group = group;
        this.cronExpression = cronExpression;
        this.status = status;
        this.description = description;
        this.className = className;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCronExpression() {
        return this.cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}