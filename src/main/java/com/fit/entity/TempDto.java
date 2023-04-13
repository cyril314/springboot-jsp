package com.fit.entity;

public class TempDto {

    private String id;
    private String sql;
    private String dbName;
    private String tableName;

    public TempDto() {
    }

    public String getId() {
        return this.id;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSql() {
        return this.sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}