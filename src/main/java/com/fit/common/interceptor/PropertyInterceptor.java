package com.fit.common.interceptor;

import com.fit.common.utils.ConvertUtils;
import com.fit.common.utils.ServletUtils;
import com.fit.common.utils.StringUtil;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.Map.Entry;

public class PropertyInterceptor {

    public static final String OR_SEPARATOR = "_OR_";
    private MatchType matchType = null;
    private Object matchValue = null;
    private Class<?> propertyClass = null;
    private String[] propertyNames = null;

    public PropertyInterceptor(String filterName, String value) {
        String firstPart = StringUtil.substringBefore(filterName, "_");
        String matchTypeCode = firstPart.substring(0, firstPart.length() - 1);
        String propertyTypeCode = firstPart.substring(firstPart.length() - 1, firstPart.length());

        try {
            this.matchType = Enum.valueOf(MatchType.class, matchTypeCode);
            this.propertyClass = (Enum.valueOf(PropertyType.class, propertyTypeCode)).getValue();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("filter名称" + filterName + "没有按规则编写,无法得到属性比较类型.", e);
        }

        String propertyNameStr = StringUtil.substringAfter(filterName, "_");
        Assert.isTrue(StringUtil.isNotBlank(propertyNameStr), "filter名称" + filterName + "没有按规则编写,无法得到属性名称.");
        this.propertyNames = propertyNameStr.split(OR_SEPARATOR);
        this.matchValue = ConvertUtils.convertStringToObject(value, this.propertyClass);
    }

    public static List<PropertyInterceptor> buildFromHttpRequest(HttpServletRequest request) {
        return buildFromHttpRequest(request, "filter");
    }

    public static List<PropertyInterceptor> buildFromHttpRequest(HttpServletRequest request, String filterPrefix) {
        ArrayList filterList = new ArrayList();
        Map filterParamMap = ServletUtils.getParametersStartingWith(request, filterPrefix + "_");
        Iterator var5 = filterParamMap.entrySet().iterator();

        while (var5.hasNext()) {
            Entry entry = (Entry) var5.next();
            String filterName = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtil.isNotBlank(value)) {
                PropertyInterceptor filter = new PropertyInterceptor(filterName, value);
                filterList.add(filter);
            }
        }

        return filterList;
    }

    public Class<?> getPropertyClass() {
        return this.propertyClass;
    }

    public MatchType getMatchType() {
        return this.matchType;
    }

    public Object getMatchValue() {
        return this.matchValue;
    }

    public String[] getPropertyNames() {
        return this.propertyNames;
    }

    public String getPropertyName() {
        Assert.isTrue(this.propertyNames.length == 1, "There are not only one property in this filter.");
        return this.propertyNames[0];
    }

    public boolean hasMultiProperties() {
        return this.propertyNames.length > 1;
    }

    public static enum MatchType {
        EQ,
        LIKE,
        LT,
        GT,
        LE,
        GE;

        private MatchType() {
        }
    }

    public static enum PropertyType {
        S(String.class),
        I(Integer.class),
        L(Long.class),
        N(Double.class),
        D(Date.class),
        B(Boolean.class);

        private Class<?> clazz;

        private PropertyType(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getValue() {
            return this.clazz;
        }
    }
}