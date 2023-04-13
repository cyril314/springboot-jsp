package com.fit.common.utils;

import java.util.HashMap;
import java.util.Map;

public class Global {

    private static Map<String, String> map = new HashMap<String, String>();
    private static PropertiesLoader propertiesLoader = new PropertiesLoader(new String[]{"application.yml"});

    public Global() {
    }

    public static String getConfig(String key) {
        String value = (String) map.get(key);
        if (value == null) {
            value = propertiesLoader.getProperty(key);
            map.put(key, value);
        }

        return value;
    }

    public static String getAdminPath() {
        return getConfig("adminPath");
    }

    public static String getFrontPath() {
        return getConfig("frontPath");
    }

    public static String getUrlSuffix() {
        return getConfig("urlSuffix");
    }
}
