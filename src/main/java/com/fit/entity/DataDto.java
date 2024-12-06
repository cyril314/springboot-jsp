package com.fit.entity;

import lombok.Data;

@Data
public class DataDto {

    private String tableName;
    private String databaseName;
    private String inserted;
    private String updated;
}