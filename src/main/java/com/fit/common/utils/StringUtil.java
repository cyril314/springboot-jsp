package com.fit.common.utils;

import com.fit.common.utils.security.Encodes;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private static final String EMPTY = "";

    public static String lowerFirst(String str) {
        return isBlank(str) ? EMPTY : str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static String upperFirst(String str) {
        return isBlank(str) ? EMPTY : str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String replaceHtml(String html) {
        if (isBlank(html)) {
            return "";
        } else {
            String regEx = "<.+?>";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(html);
            String s = m.replaceAll(EMPTY);
            return s;
        }
    }

    public static String abbr(String str, int length) {
        if (str == null) {
            return "";
        } else {
            try {
                StringBuilder e = new StringBuilder();
                int currentLength = 0;
                char[] var7;
                int var6 = (var7 = replaceHtml(Encodes.unescapeHtml(str)).toCharArray()).length;

                for (int var5 = 0; var5 < var6; ++var5) {
                    char c = var7[var5];
                    currentLength += String.valueOf(c).getBytes("GBK").length;
                    if (currentLength > length - 3) {
                        e.append("...");
                        break;
                    }

                    e.append(c);
                }

                return e.toString();
            } catch (UnsupportedEncodingException var8) {
                var8.printStackTrace();
                return EMPTY;
            }
        }
    }

    public static String rabbr(String str, int length) {
        return abbr(replaceHtml(str), length);
    }

    public static Double toDouble(Object val) {
        if (val == null) {
            return Double.valueOf(0.0D);
        } else {
            try {
                return Double.valueOf(val.toString().trim());
            } catch (Exception var2) {
                return Double.valueOf(0.0D);
            }
        }
    }

    public static Float toFloat(Object val) {
        return Float.valueOf(toDouble(val).floatValue());
    }

    public static Long toLong(Object val) {
        return Long.valueOf(toDouble(val).longValue());
    }

    public static Integer toInteger(Object val) {
        return Integer.valueOf(toLong(val).intValue());
    }

    public static final String MD5(String s) {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            byte[] e = s.getBytes();
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(e);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;

            for (int i = 0; i < j; ++i) {
                byte b = md[i];
                str[k++] = hexDigits[b >> 4 & 15];
                str[k++] = hexDigits[b & 15];
            }

            return new String(str);
        } catch (Exception var10) {
            return "";
        }
    }

    /**
     * 将集合转化为字符串
     *
     * @param separator 分隔符
     * @param list      参数集合
     */
    public static String join(String separator, List<?> list) {
        Object[] objs = list.toArray();
        return join(separator, objs);
    }

    /**
     * 将数组转化为字符串
     *
     * @param array     参数数组
     * @param separator 分隔符
     */
    public static String join(String separator, Object[] array) {
        if (array == null) {
            return null;
        } else if (array.length <= 0) {
            return EMPTY;
        } else if (array.length == 1) {
            return String.valueOf(array[0]);
        } else {
            StringBuilder sb = new StringBuilder(array.length * 16);
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    sb.append(separator);
                }
                sb.append(array[i]);
            }
            return sb.toString();
        }
    }

    /**
     * 判断某字符串是否为空或长度为0或由空白符构成
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 判断是否为空
     *
     * @param cs
     * @return
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * 字符串首字母大写
     *
     * @param name
     * @return
     */
    public static String capitalize(String name) {
        if (isEmpty(name)) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String substringAfter(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (separator == null) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == -1) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    public static String substringBefore(final String str, final String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }
}
