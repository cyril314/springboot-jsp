package com.fit.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class NotSqlEntity implements Serializable {

    private static final long serialVersionUID = -362284023270541690L;

    private String key;
    private String value;
    private String type;
    private List<String> list;
    private Set<String> set;
    private List<Map<String, Object>> listMap;
    private String[] valuek;
    private String[] valuev;
    private String exTime;
}
