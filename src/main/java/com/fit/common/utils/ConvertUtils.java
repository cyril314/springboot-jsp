package com.fit.common.utils;

import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @className: ConvertUtils
 * @description: 格式转换工具类
 * @author: Aim
 * @date: 2023/4/11
 **/
public class ConvertUtils {

    /**
     * 提取集合中的对象的属性(通过getter函数), 组合成List.
     *
     * @param collection   来源集合.
     * @param propertyName 要提取的属性名.
     */
    public static List convertElementPropertyToList(Collection collection, String propertyName) {
        ArrayList list = new ArrayList();
        try {
            Iterator col = collection.iterator();
            while (col.hasNext()) {
                Object e = col.next();
                list.add(BeanUtil.getFieldValue(e, propertyName));
            }

            return list;
        } catch (Exception var5) {
            throw Reflections.convertReflectionExceptionToUnchecked(var5);
        }
    }

    public static String convertElementPropertyToString(Collection collection, String propertyName, String separator) {
        List list = convertElementPropertyToList(collection, propertyName);
        return StringUtil.join(separator, list);
    }

    /**
     * 转换字符串到相应类型.
     *
     * @param v   待转换的字符串.
     * @param clz 转换目标类型.
     */
    public static <T> T convertStringToObject(String v, Class<T> clz) {
        T obj = null;
        try {
            obj = (T) obj.getClass();
            Method setter = obj.getClass().getMethod(v, clz);
            String type = obj.getClass().getTypeName();
            if ("java.lang.Byte".equals(type) || "byte".equals(type)) {
                setter.invoke(obj, Byte.parseByte(v));
            } else if ("java.lang.Short".equals(type) || "short".equals(type)) {
                setter.invoke(obj, Short.parseShort(v));
            } else if ("java.lang.Integer".equals(type) || "int".equals(type)) {
                setter.invoke(obj, Integer.parseInt(v));
            } else if ("java.lang.Long".equals(type) || "long".equals(type)) {
                setter.invoke(obj, Long.parseLong(v));
            } else if ("java.lang.Float".equals(type) || "float".equals(type)) {
                setter.invoke(obj, Float.parseFloat(v));
            } else if ("java.lang.Double".equals(type) || "double".equals(type)) {
                setter.invoke(obj, Double.parseDouble(v));
            } else if ("java.lang.String".equals(type)) {
                setter.invoke(obj, v);
            } else if ("java.util.Date".equals(type)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    setter.invoke(obj, sdf.parse(v));
                } catch (ParseException e) {
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    setter.invoke(obj, sdf.parse(v));
                }
            } else if ("java.lang.Date".equals(type)) {
                setter.invoke(obj, new Date(v).getTime());
            } else if ("java.lang.Timer".equals(type)) {
                setter.invoke(obj, Time.valueOf(v).getTime());
            } else if ("java.sql.Timestamp".equals(type)) {
                setter.invoke(obj, Timestamp.valueOf(v));
            } else {
                setter.invoke(obj, v);
            }
        } catch (Exception e) {
            throw Reflections.convertReflectionExceptionToUnchecked(e);
        }
        return obj;
    }

    public static void addAll(Collection collection, Object[] elements) {
        int i = 0;
        for (int size = elements.length; i < size; ++i) {
            collection.add(elements[i]);
        }
    }
}
