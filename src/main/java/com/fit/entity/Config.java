package com.fit.entity;

import lombok.Data;

@Data
public class Config {

    private String id;
    private String databaseType;
    private String driver;
    private String url;
    private String databaseName;
    private String userName;
    private String password;
    private String port;
    private String ip;
    private String isDefault;
}
