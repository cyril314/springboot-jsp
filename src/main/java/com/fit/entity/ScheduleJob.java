package com.fit.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScheduleJob implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String group;
    private String cronExpression;
    private String status;
    private String description;
    private String className;
}