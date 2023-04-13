package com.fit.common.utils;

import com.fit.common.Constants;
import com.fit.common.utils.security.Encodes;
import com.fit.entity.NotSqlEntity;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;

import java.util.*;

@Slf4j
public final class RedisUtil {

    private static final String HOST = Constants.IP;
    private static int PORT = 6379;
    private static int MAX_IDLE = 200;
    private static int TIMEOUT = 10000;
    private static JedisPool jedisPool = null;
    private static Jedis jedis = null;

    static {
        jedisPool = getJedisPool();
    }

    public static boolean flushAllForRedis(Map<String, Object> config) throws Exception {
        Jedis redis = getRedisForMap(config);
        redis.flushAll();
        return true;
    }

    public static int getDbAmountForRedis(Map<String, Object> config) {
        int dbAmount = 1;
        Jedis redis = getRedisForMap(config);
        try {
            List e = redis.configGet("databases");
            if (e.size() > 0) {
                dbAmount = Integer.parseInt((String) e.get(1));
            } else {
                dbAmount = 15;
            }
        } catch (Exception e) {
            log.error("取得Redis中数据库的数量出错！ {}", e.getMessage());
        }
        return dbAmount;
    }

    public static boolean testConnForRedis(String databaseType, String databaseName, String ip, String port, String user, String pass) {
        JedisPool pool = null;
        try {
            JedisPoolConfig e = new JedisPoolConfig();
            e.setMaxIdle(MAX_IDLE);
            e.setTestOnBorrow(true);
            if (!"".equals(pass) && pass != null) {
                pool = new JedisPool(e, ip, Integer.parseInt(port), TIMEOUT, pass);
            } else {
                pool = new JedisPool(e, ip, Integer.parseInt(port));
            }

            return pool != null;
        } catch (Exception e) {
            return false;
        } finally {
            pool.close();
        }
    }

    public static Map<String, Object> getNoSQLDBForRedis(Map<String, Object> config, int pageSize, int limitFrom, String NoSQLDbName, String selectKey, String selectValue) {
        HashMap tempMap = new HashMap();
        Jedis redis = getRedisForMap(config);
        String currentDbIndex = NoSQLDbName.substring(2, NoSQLDbName.length());
        try {
            redis.select(Integer.parseInt(currentDbIndex));
            Long dbSize = redis.dbSize();
            Object nodekeys = new HashSet();
            if (selectKey.equals("nokey")) {
                if (dbSize.longValue() > 1000L) {
                    limitFrom = 0;

                    for (int it = 0; it < pageSize; ++it) {
                        ((Set) nodekeys).add(redis.randomKey());
                    }
                } else {
                    nodekeys = redis.keys("*");
                }
            } else {
                nodekeys = redis.keys("*" + selectKey + "*");
            }

            Iterator iterator = ((Set) nodekeys).iterator();
            int i = 1;
            ArrayList list = new ArrayList();
            for (String value = ""; iterator.hasNext(); ++i) {
                if (i >= limitFrom && i <= limitFrom + pageSize) {
                    HashMap map = new HashMap();
                    String key = (String) iterator.next();
                    String type = redis.type(key);
                    map.put("key", key);
                    map.put("type", type);
                    if (type.equals("string")) {
                        value = redis.get(key);
                        if (value.length() > 80) {
                            map.put("value", value.substring(0, 79) + "......");
                        } else {
                            map.put("value", value);
                        }
                    }

                    Long lon;
                    if (type.equals("list")) {
                        lon = redis.llen(key);
                        if (lon.longValue() > 20L) {
                            lon = Long.valueOf(20L);
                        }

                        map.put("value", redis.lrange(key, 0L, lon.longValue()));
                    }

                    if (type.equals("set")) {
                        map.put("value", redis.smembers(key).toString());
                    }

                    if (type.equals("zset")) {
                        lon = redis.zcard(key);
                        if (lon.longValue() > 20L) {
                            lon = Long.valueOf(20L);
                        }

                        Set set = redis.zrangeWithScores(key, 0L, lon.longValue());
                        Iterator itt = set.iterator();

                        String ss;
                        Tuple str;
                        for (ss = ""; itt.hasNext(); ss = ss + "[" + str.getScore() + "," + str.getElement() + "],") {
                            str = (Tuple) itt.next();
                        }

                        ss = ss.substring(0, ss.length() - 1);
                        map.put("value", "[" + ss + "]");
                    }

                    if (type.equals("hash")) {
                        map.put("value", redis.hgetAll(key).toString());
                    }

                    list.add(map);
                } else {
                    iterator.next();
                }
            }

            if (selectKey.equals("nokey")) {
                tempMap.put("rowCount", Integer.valueOf(Integer.parseInt(dbSize.toString())));
            } else {
                tempMap.put("rowCount", Integer.valueOf(i));
            }

            tempMap.put("dataList", list);
        } catch (Exception e) {
            log.error("取得 NoSQL数据出错：{}", e.getMessage());
        }

        return tempMap;
    }

    public static boolean deleteKeys(Map<String, Object> config, String NoSQLDbName, String[] ids) {
        Jedis redis = getRedisForMap(config);
        String currentDBindex = NoSQLDbName.substring(2, NoSQLDbName.length());
        try {
            redis.select(Integer.parseInt(currentDBindex));
            for (int e = 0; e < ids.length; ++e) {
                redis.del(new String[]{ids[e]});
            }
            return true;
        } catch (Exception var9) {
            var9.printStackTrace();
        }
        return false;
    }

    public static boolean set(Map<String, Object> config, NotSqlEntity notSqlEntity, String NoSQLDbName) {
        Jedis redis = null;
        try {
            redis = getRedisForMap(config);
            String currentDBindex = NoSQLDbName.substring(2, NoSQLDbName.length());
            redis.select(Integer.parseInt(currentDBindex));
            String key = notSqlEntity.getKey();
            String value = notSqlEntity.getValue();
            value = Encodes.unescapeHtml(value);
            String[] valuek = notSqlEntity.getValuek();
            String type = notSqlEntity.getType();
            int exTime = -1;
            if (!"".equals(notSqlEntity.getExTime()) && !"0".equals(notSqlEntity.getExTime())) {
                exTime = Integer.parseInt(notSqlEntity.getExTime());
            }

            if (type.equals("string")) {
                redis.set(key, value);
            }

            if (type.equals("list")) {
                redis.del(new String[]{key});

                for (int valueV = valuek.length; valueV > 0; --valueV) {
                    if (valueV == valuek.length) {
                        redis.lpush(key, new String[]{valuek[valueV - 1]});
                    } else {
                        redis.lpushx(key, valuek[valueV - 1]);
                    }
                }
            }

            if (type.equals("set")) {
                redis.del(new String[]{key});
                String str = "";
                for (int hashmm = 0; hashmm < valuek.length; ++hashmm) {
                    if (hashmm == 0) {
                        str = str + valuek[hashmm];
                    } else {
                        str = str + "," + valuek[hashmm];
                    }
                }

                redis.sadd(key, new String[]{str});
            }

            int i;
            String valuevvv;
            String[] strs = notSqlEntity.getValuev();
            HashMap map = new HashMap();
            if (type.equals("zset")) {
                redis.del(new String[]{key});
                for (i = valuek.length; i > 0; --i) {
                    Double valuekkk = Double.valueOf(Double.parseDouble(valuek[i - 1].trim()));
                    valuevvv = strs[i - 1].trim();
                    if (valuevvv == null) {
                        valuevvv = "";
                    }

                    map.put(valuekkk, valuevvv);
                }

                redis.zadd(key, map);
            }

            if (type.equals("hash")) {
                redis.del(new String[]{key});
                for (i = valuek.length; i > 0; --i) {
                    String keyVal = valuek[i - 1].trim();
                    valuevvv = strs[i - 1].trim();
                    if (valuevvv == null) {
                        valuevvv = "";
                    }
                    map.put(keyVal, valuevvv);
                }

                redis.hmset(key, map);
            }

            if (exTime != -1) {
                redis.expire(key, exTime);
            }

            return true;
        } catch (Exception e) {
            log.error("根据redis的配置添加内容出错, {}", e.getMessage());
        }
        return false;
    }

    public static Map<String, Object> get(Map<String, Object> config, String key, String NoSQLDbName) {
        Jedis redis = getRedisForMap(config);
        String currentDBindex = NoSQLDbName.substring(2, NoSQLDbName.length());
        HashMap map = new HashMap();
        try {
            redis.select(Integer.parseInt(currentDBindex));
            String e = redis.type(key);
            String exTime = "" + redis.ttl(key);
            map.put("key", key);
            map.put("type", e);
            map.put("exTime", exTime);
            if (e.equals("string")) {
                map.put("value", redis.get(key));
            }

            if (e.equals("list")) {
                Long set = redis.llen(key);
                map.put("value", redis.lrange(key, 0L, set.longValue()));
            }

            if (e.equals("set")) {
                map.put("value", redis.smembers(key).toString());
            }

            if (e.equals("zset")) {
                Set set1 = redis.zrangeWithScores(key, 0L, -1L);
                map.put("value", set1);
            }

            if (e.equals("hash")) {
                map.put("value", redis.hgetAll(key));
            }
        } catch (Exception e) {
            log.error("根据redis的配置获取无库名的指定KEY出错, {}", e.getMessage());
        } finally {
            redis = null;
        }

        return map;
    }

    public static String getConfig(Map<String, Object> config, String configKey) {
        String value = "";
        Jedis redis = getRedisForMap(config);
        try {
            List<String> e = redis.configGet(configKey);
            for (int i = 0; i < e.size(); ++i) {
                value = e.get(i);
            }
        } catch (Exception e) {
            log.error("获取redis的配置出错, {}", e.getMessage());
        }

        return value;
    }

    public static String getInfo(Map<String, Object> config) {
        String value = null;
        Jedis redis = getRedisForMap(config);
        try {
            value = redis.info();
        } catch (Exception e) {
            log.error("取redis的状态出错, {}", e.getMessage());
        } finally {
            redis = null;
        }

        return value;
    }

    public static boolean bgSave(Map<String, Object> config) {
        Jedis redis = getRedisForMap(config);
        try {
            redis.bgsave();
            return true;
        } catch (Exception e) {
            log.error("设置bgSave出错, {}", e.getMessage());
        } finally {
            redis = null;
        }
        return false;
    }

    public static Jedis getRedisForMap(Map<String, Object> map) {
        String ip = (String) map.get("ip");
        String port = (String) map.get("port");
        String password = (String) map.get("password");
        Jedis redis = new Jedis(ip, Integer.parseInt(port));
        if (!password.equals("")) {
            redis.auth(password);
        }
        return redis;
    }

    public static String get(String key) {
        String value = null;

        try {
            hasConnect();
            value = jedis.get(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
        } finally {
            returnResource(jedisPool, jedis);
        }

        return value;
    }

    private static void hasConnect() {
        if (jedis == null) {
            jedis = getJedisPool().getResource();
        }
    }

    public static synchronized JedisPool getJedisPool() {
        if (jedisPool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(5);
            config.setTestOnBorrow(true);
            if (Constants.PASS_WROD.equals("")) {
                return new JedisPool(config, HOST, PORT);
            } else {
                return new JedisPool(config, HOST, PORT, TIMEOUT, Constants.PASS_WROD);
            }
        }
        return null;
    }

    public static void returnResource(JedisPool pool, Jedis redis) {
        if (redis != null) {
            pool.returnResource(redis);
        }
    }
}
