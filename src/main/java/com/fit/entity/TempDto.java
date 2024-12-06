package com.fit.entity;

import lombok.Data;

@Data
public class TempDto {

    private String id;
    private String sql;
    private String dbName;
    private String tableName;
}