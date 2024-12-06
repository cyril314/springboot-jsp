package com.fit.utils;

import com.fit.common.utils.ConvertUtils;
import com.fit.common.utils.security.Encodes;
import com.fit.entity.NotSqlEntity;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class MemcachedUtil {

    private static String databaseType = "Memcache";
    private static String userName = "root";
    private static String passwd = "123456";
    private static String port = "11211";
    private static String ip = "127.0.0.1";

    public MemcachedUtil() {
    }

    public static boolean testConnection(String databaseType2, String databaseName2, String ip2, String port2, String user2, String pass2) {
        try {
            String e = "";
            if (databaseType2.equals("Memcache")) {
                e = ip2 + ":" + port2;
                XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(e));
                MemcachedClient client = builder.build();
                client.set("hello", 0, "welcome to use TreeNMS for memcache!");
                client.shutdown();
                return true;
            } else {
                return true;
            }
        } catch (Exception var10) {
            System.out.println("memcache 测试连接失败，" + var10.getMessage());
            return false;
        }
    }

    public boolean memcachedSet(NotSqlEntity notSqlEntity, String ip, String port, String passwd) throws Exception {
        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(ip + ":" + port), new int[]{1});
        MemcachedClient memcachedClient = null;
        try {
            memcachedClient = builder.build();
            memcachedClient.setOpTimeout(5000L);
            memcachedClient.setEnableHeartBeat(false);
            String e = notSqlEntity.getKey();
            String cacheValue = notSqlEntity.getValue().trim();
            cacheValue = Encodes.unescapeHtml(cacheValue);
            String type = notSqlEntity.getType();
            int o1 = Integer.parseInt(notSqlEntity.getExTime());
            if (o1 == -1) {
                o1 = 0;
            }

            try {
                if (type.equals("string") || type.equals("String")) {
                    memcachedClient.set(e, o1, cacheValue);
                }

                String[] e1;
                ArrayList valueV;
                if (type.equals("list")) {
                    memcachedClient.delete(e);
                    e1 = notSqlEntity.getValuek();
                    valueV = new ArrayList();
                    ConvertUtils.addAll(valueV, e1);
                    memcachedClient.set(e, o1, valueV);
                }

                HashSet var33;
                if (type.equals("set")) {
                    memcachedClient.delete(e);
                    e1 = notSqlEntity.getValuek();
                    var33 = new HashSet();
                    ConvertUtils.addAll(var33, e1);
                    memcachedClient.set(e, o1, var33);
                }

                if (type.equals("zset")) {
                    memcachedClient.delete(e);
                    e1 = notSqlEntity.getValuek();
                    var33 = new HashSet();
                    ConvertUtils.addAll(var33, e1);
                    memcachedClient.set(e, o1, var33);
                }

                if (type.equals("HashSet")) {
                    memcachedClient.delete(e);
                    e1 = notSqlEntity.getValuek();
                    var33 = new HashSet();
                    ConvertUtils.addAll(var33, e1);
                    memcachedClient.set(e, o1, var33);
                }

                if (type.equals("ArrayList")) {
                    memcachedClient.delete(e);
                    e1 = notSqlEntity.getValuek();
                    valueV = new ArrayList();
                    ConvertUtils.addAll(valueV, e1);
                    memcachedClient.set(e, o1, valueV);
                }

                String[] var34;
                HashMap hashmm;
                int i;
                String valuekkk;
                String valuevvv;
                if (type.equals("hash")) {
                    e1 = notSqlEntity.getValuek();
                    var34 = notSqlEntity.getValuev();
                    hashmm = new HashMap();
                    for (i = e1.length; i > 0; --i) {
                        valuekkk = e1[i - 1].trim();
                        valuevvv = var34[i - 1].trim();
                        if (valuevvv == null) {
                            valuevvv = "";
                        }
                        hashmm.put(valuekkk, valuevvv);
                    }
                    memcachedClient.set(e, o1, hashmm);
                }

                if (type.equals("HashMap")) {
                    e1 = notSqlEntity.getValuek();
                    var34 = notSqlEntity.getValuev();
                    hashmm = new HashMap();
                    for (i = e1.length; i > 0; --i) {
                        valuekkk = e1[i - 1].trim();
                        valuevvv = var34[i - 1].trim();
                        if (valuevvv == null) {
                            valuevvv = "";
                        }
                        hashmm.put(valuekkk, valuevvv);
                    }
                    memcachedClient.set(e, o1, hashmm);
                }
            } catch (TimeoutException var28) {
                var28.printStackTrace();
            } catch (InterruptedException var29) {
                var29.printStackTrace();
            } catch (MemcachedException var30) {
                var30.printStackTrace();
            }
        } catch (IOException var31) {
            var31.printStackTrace();
            System.out.println(var31.getMessage());
        } finally {
            if (memcachedClient != null) {
                try {
                    memcachedClient.shutdown();
                } catch (IOException var27) {
                    var27.printStackTrace();
                }
            }
        }
        return true;
    }

    public boolean memcachedDelete(String cacheKey, String ip, String port, String passwd) throws Exception {
        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(ip + ":" + port), new int[]{1});
        MemcachedClient memcachedClient = null;
        try {
            memcachedClient = builder.build();
            try {
                memcachedClient.delete(cacheKey);
            } catch (TimeoutException var19) {
                var19.printStackTrace();
            } catch (InterruptedException var20) {
                var20.printStackTrace();
            } catch (MemcachedException var21) {
                var21.printStackTrace();
            }
        } catch (IOException var22) {
            var22.printStackTrace();
        } finally {
            if (memcachedClient != null) {
                try {
                    memcachedClient.shutdown();
                } catch (IOException var18) {
                    var18.printStackTrace();
                }
            }
        }
        return true;
    }

    public static String memcachedGet(String obj, String ip, String port, String passwd) throws Exception {
        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(ip + ":" + port));
        MemcachedClient client = builder.build();
        String value = "";
        try {
            Object e = client.get(obj);
            if (e instanceof String) {
                value = e.toString();
            } else if (e instanceof HashSet) {
                value = e.toString();
            } else {
                value = e.toString();
            }
        } catch (MemcachedException var9) {
            System.err.println("MemcachedClient operation fail");
            var9.printStackTrace();
        } catch (TimeoutException var10) {
            System.err.println("MemcachedClient operation timeout");
            var10.printStackTrace();
        } catch (Exception var11) {
            System.err.println("MemcachedClient operation Exception");
            var11.printStackTrace();
        }
        try {
            client.shutdown();
        } catch (IOException var8) {
            System.err.println("Shutdown MemcachedClient fail");
            var8.printStackTrace();
        }
        return value;
    }

    public static Map<String, Object> memcachedGet2(String key, String ip, String port, String passwd) throws Exception {
        HashMap map = new HashMap();
        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(ip + ":" + port));
        MemcachedClient client = builder.build();
        String type = "";
        String value = "";

        try {
            Object e = client.get(key);
            if (e instanceof String) {
                value = e.toString();
                type = "String";
            } else if (e instanceof HashSet) {
                type = "HashSet";
                value = e.toString();
            } else if (e instanceof ArrayList) {
                type = "ArrayList";
                value = e.toString();
            } else if (e instanceof HashMap) {
                type = "HashMap";
                value = e.toString();
            } else if (e instanceof ConcurrentHashMap) {
                type = "HashMap";
                value = e.toString();
            } else if (e instanceof Serializable) {
                type = "Object";
                value = "已序列化的对象";
                System.out.println("已序列化的对象");
            } else if (e == null) {
                type = "Object";
                value = "已序列化的对象";
                System.out.println("已序列化的对象");
            } else {
                type = "String";
                value = e.toString();
            }
            map.put("key", key);
            map.put("type", type);
            map.put("value", value);
        } catch (MemcachedException var11) {
            System.err.println("MemcachedClient operation fail");
            var11.printStackTrace();
        } catch (TimeoutException var12) {
            System.err.println("MemcachedClient operation timeout");
            var12.printStackTrace();
        } catch (Exception var13) {
            System.err.println("MemcachedClient operation Exception");
            var13.printStackTrace();
        }

        try {
            client.shutdown();
        } catch (IOException var10) {
            System.err.println("Shutdown MemcachedClient fail");
            var10.printStackTrace();
        }

        return map;
    }

    public static Map<String, Object> memcachedGet3(String key, String ip, String port, String passwd) throws Exception {
        HashMap map = new HashMap();
        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(ip + ":" + port));
        MemcachedClient client = builder.build();
        String type = "";
        String value = "";

        try {
            Object e = client.get(key);
            if (e instanceof String) {
                value = e.toString();
                type = "String";
            } else if (e instanceof HashSet) {
                type = "HashSet";
                value = e.toString();
            } else if (e instanceof ArrayList) {
                type = "ArrayList";
                value = e.toString();
            } else if (e instanceof HashMap) {
                type = "HashMap";
                value = e.toString();
            } else {
                type = "String";
                value = e.toString();
            }

            map.put("key", key);
            map.put("type", type);
            map.put("value", value);
        } catch (MemcachedException var11) {
            System.err.println("MemcachedClient operation fail");
            var11.printStackTrace();
        } catch (TimeoutException var12) {
            System.err.println("MemcachedClient operation timeout");
            var12.printStackTrace();
        } catch (Exception var13) {
            System.err.println("MemcachedClient operation Exception");
            var13.printStackTrace();
        }

        try {
            client.shutdown();
        } catch (IOException var10) {
            System.err.println("Shutdown MemcachedClient fail");
            var10.printStackTrace();
        }

        return map;
    }

    public static Map<String, String> memcachedStatus(String ip, String port, String passwd) throws Exception {
        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(ip + ":" + port));
        MemcachedClient client = builder.build();
        Object result = new HashMap();
        try {
            InetSocketAddress e = new InetSocketAddress(ip, Integer.parseInt(port));
            result = client.stats(e);
        } catch (MemcachedException var8) {
            System.err.println("MemcachedClient operation fail");
            var8.printStackTrace();
        } catch (TimeoutException var9) {
            System.err.println("MemcachedClient operation timeout");
            var9.printStackTrace();
        } catch (InterruptedException var10) {
            ;
        }
        try {
            client.shutdown();
        } catch (IOException var7) {
            System.err.println("Shutdown MemcachedClient fail");
            var7.printStackTrace();
        }
        return (Map) result;
    }

    public static List<String> getAllDataBaseForMemcached(String ip, String port, String passwd) {
        ArrayList list = new ArrayList();
        ArrayList ids = new ArrayList();
        StringBuffer r = new StringBuffer();

        try {
            Socket e = new Socket(ip, Integer.parseInt(port));
            PrintWriter os = new PrintWriter(e.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(e.getInputStream()));
            os.println("stats items");
            os.flush();

            String l;
            while (!(l = is.readLine()).equals("END")) {
                r.append(l).append("\n");
            }

            String rr = r.toString();
            if (rr.length() > 0) {
                new StringBuffer();
                rr.replace("STAT items", "");
                String[] var14;
                int var13 = (var14 = rr.split("\n")).length;

                for (int str = 0; str < var13; ++str) {
                    String i = var14[str];
                    ids.add(i.split(":")[1]);
                }
            }

            os.close();
            is.close();
            e.close();

            for (int var16 = 0; var16 < ids.size(); ++var16) {
                String var17 = (String) ids.get(var16);
                if (!list.contains(var17)) {
                    list.add(var17);
                }
            }
        } catch (Exception var15) {
            System.out.println("Error" + var15);
        }

        return list;
    }

    public static Map<String, Object> getAllKeyAndValue(int pageSize, int limitFrom, String NoSQLDbName, String selectKey,
                                                        String selectValue, String ip, String port, String passwd) {
        HashMap tempMap = new HashMap();
        ArrayList keyList = new ArrayList();
        ArrayList resultList = new ArrayList();

        try {
            String e = NoSQLDbName.replaceFirst("items", "");
            Socket socket = new Socket(ip, Integer.parseInt(port));
            PrintWriter os = new PrintWriter(socket.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os.println("stats cachedump " + e + " 0");
            os.flush();

            String ll;
            int i;
            for (i = 0; !(ll = is.readLine()).equals("END"); ++i) {
                String maxNum = ll.split(" ")[1];
                if (selectKey.equals("nokey")) {
                    keyList.add(maxNum);
                } else if (maxNum.indexOf(selectKey) >= 0) {
                    keyList.add(maxNum);
                }
            }

            int var22 = pageSize + limitFrom;
            if (var22 > keyList.size()) {
                var22 = keyList.size();
            }

            for (int y = limitFrom; y < var22; ++y) {
                String key = (String) keyList.get(y);
                Map map2 = memcachedGet2(key, ip, port, passwd);
                resultList.add(map2);
            }

            tempMap.put("rowCount", Integer.valueOf(i));
            tempMap.put("dataList", resultList);
            os.close();
            is.close();
            socket.close();
        } catch (Exception var21) {
            System.out.println("Error" + var21);
        }

        return tempMap;
    }

    public static String all_keys(String host, int port) {
        ArrayList list = new ArrayList();
        StringBuffer r = new StringBuffer();

        try {
            Socket e = new Socket(host, port);
            PrintWriter os = new PrintWriter(e.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(e.getInputStream()));
            os.println("stats items");
            os.flush();

            String l;
            while (!(l = is.readLine()).equals("END")) {
                r.append(l).append("\n");
                System.out.println("line = " + l);
            }

            String rr = r.toString();
            HashSet ids = new HashSet();
            if (rr.length() > 0) {
                r = new StringBuffer();
                rr.replace("STAT items", "");
                String[] var13;
                int var12 = (var13 = rr.split("\n")).length;

                String s;
                for (int var11 = 0; var11 < var12; ++var11) {
                    s = var13[var11];
                    System.out.println("items " + s.split(":")[1]);
                    ids.add(s.split(":")[1]);
                }

                if (ids.size() > 0) {
                    r = new StringBuffer();
                    Iterator var15 = ids.iterator();

                    while (var15.hasNext()) {
                        s = (String) var15.next();
                        os.println("stats cachedump " + s + " 0");
                        os.flush();

                        while (!(l = is.readLine()).equals("END")) {
                            r.append(l.split(" ")[1]).append("\n");
                            list.add(l.split(" ")[1]);
                        }
                    }
                }
            }

            os.close();
            is.close();
            e.close();
        } catch (Exception var14) {
            System.out.println("Error" + var14);
        }

        System.out.println("dddddd " + list.toString());
        return r.toString();
    }
}
