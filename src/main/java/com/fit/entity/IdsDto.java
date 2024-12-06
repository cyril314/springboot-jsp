package com.fit.entity;

import lombok.Data;

@Data
public class IdsDto {

    private String[] ids;
    private String tableName;
    private String databaseName;
    private String NoSQLDbName;
    private String primary_key;
    private String column_name;
    private String is_nullable;
    private String column_key;
    private String column_name2;
    private String checkedItems;
    private String databaseConfigId;
    private String keyName;
}