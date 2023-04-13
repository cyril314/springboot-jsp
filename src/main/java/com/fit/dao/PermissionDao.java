package com.fit.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fit.common.Constants;
import com.fit.common.base.Page;
import com.fit.common.utils.DBUtil;
import com.fit.common.utils.FileUtil;
import com.fit.common.utils.RedisUtil;
import com.fit.common.utils.SqliteUtil;
import com.fit.entity.Config;
import com.fit.entity.NotSqlEntity;
import com.fit.utils.*;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionDao {

    public List<Map<String, Object>> getAllDataBase() {
        String sql = " select * from  information_schema.schemata  ";
        DBUtil.setDbName(Constants.DATABASE_NAME);
        List list = DBUtil.queryForList(sql);
        ArrayList list2 = new ArrayList();
        HashMap map = new HashMap();
        map.put("SCHEMA_NAME", Constants.DATABASE_NAME);
        list2.add(map);

        for (int i = 0; i < list.size(); ++i) {
            Map map2 = (Map) list.get(i);
            String schema_name = (String) map2.get("SCHEMA_NAME");
            if (!schema_name.equals(Constants.DATABASE_NAME)) {
                list2.add(map2);
            }
        }

        return list2;
    }

    public List<Map<String, Object>> getAllTables(String dbName) throws Exception {
        DBUtil.setDbName(dbName);
        String sql = " select TABLE_NAME from information_schema.TABLES where table_schema='" + dbName + "' and table_type='BASE TABLE' ";
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getAllViews(String dbName) throws Exception {
        String sql = " select TABLE_NAME   from information_schema.TABLES where table_schema='" + dbName + "' and table_type='VIEW' ";
        DBUtil.setDbName(dbName);
        return DBUtil.queryForList(sql);
    }

    public List<Map<String, Object>> getAllFuntion(String dbName) throws Exception {
        String sql = " select ROUTINE_NAME   from information_schema.ROUTINES where routine_schema='" + dbName + "' ";
        DBUtil.setDbName(dbName);
        return DBUtil.queryForList(sql);
    }

    public List<Map<String, Object>> getTableColumns(String dbName, String tableName) throws Exception {
        String sql = "select * from  " + dbName + "." + tableName + " limit 1 ";
        DBUtil.setDbName(dbName);
        return DBUtil.queryForColumnOnly(sql);
    }

    public List<Map<String, Object>> getTableColumns3(String dbName, String tableName) throws Exception {
        String sql = " select column_name as TREESOFTPRIMARYKEY, COLUMN_NAME,COLUMN_TYPE , DATA_TYPE ,CHARACTER_MAXIMUM_LENGTH,IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT  from information_schema.columns where   table_name='" + tableName + "' and table_schema='" + dbName + "'  ";
        DBUtil.setDbName(dbName);
        return DBUtil.queryForList(sql);
    }

    public Page<Map<String, Object>> getData(Page<Map<String, Object>> page, String tableName, String dbName) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        String orderBy = page.getOrderBy();
        String order = page.getOrder();
        DBUtil.setDbName(dbName);
        List list3 = this.getPrimaryKeyss(dbName, tableName);
        String tem = "";

        Map primaryKey;
        for (Iterator sql = list3.iterator(); sql.hasNext(); tem = tem + primaryKey.get("column_name") + ",") {
            primaryKey = (Map) sql.next();
        }

        String primaryKey1 = "";
        if (!tem.equals("")) {
            primaryKey1 = tem.substring(0, tem.length() - 1);
        }

        String sql1 = "select count(*) from  " + dbName + "." + tableName;
        String sql2 = "";
        if (orderBy != null && !orderBy.equals("")) {
            sql2 = "select  *  from  " + dbName + "." + tableName + " order by " + orderBy + " " + order + "  LIMIT " + limitFrom + "," + pageSize;
        } else {
            sql2 = "select  *  from  " + dbName + "." + tableName + "  LIMIT " + limitFrom + "," + pageSize;
        }

        List list = DBUtil.queryForList(sql2);
        int rowCount = DBUtil.executeQueryForCount(sql1);
        List columns = this.getTableColumns(dbName, tableName);
        ArrayList tempList = new ArrayList();
        HashMap map1 = new HashMap();
        map1.put("field", "treeSoftPrimaryKey");
        map1.put("checkbox", Boolean.valueOf(true));
        tempList.add(map1);

        HashMap map2;
        for (Iterator jsonfromList = columns.iterator(); jsonfromList.hasNext(); tempList.add(map2)) {
            Map mapper = (Map) jsonfromList.next();
            map2 = new HashMap();
            map2.put("field", mapper.get("column_name"));
            map2.put("title", mapper.get("column_name"));
            map2.put("sortable", Boolean.valueOf(true));
            map2.put("editor", "text");
            if (mapper.get("data_type").equals("DATETIME")) {
                map2.put("editor", "datebox");
            } else if (!mapper.get("data_type").equals("INT") && !mapper.get("data_type").equals("SMALLINT") && !mapper.get("data_type").equals("TINYINT")) {
                if (mapper.get("data_type").equals("DOUBLE")) {
                    map2.put("editor", "numberbox");
                } else {
                    map2.put("editor", "text");
                }
            } else {
                map2.put("editor", "numberbox");
            }
        }

        ObjectMapper mapper1 = new ObjectMapper();
        String jsonfromList1 = "[" + mapper1.writeValueAsString(tempList) + "]";
        page.setTotalCount((long) rowCount);
        page.setResult(list);
        page.setColumns(jsonfromList1);
        page.setPrimaryKey(primaryKey1);
        return page;
    }

    public Page<Map<String, Object>> executeSql(Page<Map<String, Object>> page, String sql, String dbName) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        String sql2 = " select * from ( " + sql + " ) tab  LIMIT " + limitFrom + "," + pageSize;
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql2);
        int rowCount = DBUtil.executeQueryForCount(sql);
        List columns = this.executeSqlForColumns(sql, dbName);
        ArrayList tempList = new ArrayList();

        HashMap temp;
        for (Iterator tableName = columns.iterator(); tableName.hasNext(); tempList.add(temp)) {
            Map primaryKey = (Map) tableName.next();
            temp = new HashMap();
            temp.put("field", primaryKey.get("column_name"));
            temp.put("title", primaryKey.get("column_name"));
            temp.put("sortable", Boolean.valueOf(true));
            if (primaryKey.get("data_type").equals("DATETIME")) {
                temp.put("editor", "datebox");
            } else if (!primaryKey.get("data_type").equals("INT") && !primaryKey.get("data_type").equals("SMALLINT") && !primaryKey.get("data_type").equals("TINYINT")) {
                if (primaryKey.get("data_type").equals("DOUBLE")) {
                    temp.put("editor", "numberbox");
                } else {
                    temp.put("editor", "text");
                }
            } else {
                temp.put("editor", "numberbox");
            }
        }

        String var22 = "";
        String var23 = "";
        String var24 = "";
        if (this.checkSqlIsOneTableForMySql(dbName, sql)) {
            Pattern mapper = Pattern.compile("\\s+");
            Matcher jsonfromList = mapper.matcher(sql);
            var24 = jsonfromList.replaceAll(" ");
            var24 = var24.toLowerCase();

            String tem;
            for (int list3 = 14; list3 < var24.length(); ++list3) {
                tem = String.valueOf(var24.charAt(list3));
                if (tem.equals(" ")) {
                    break;
                }

                var23 = var23 + tem;
            }

            List var27 = this.getPrimaryKeyss(dbName, var23);
            tem = "";

            Map map3;
            for (Iterator var21 = var27.iterator(); var21.hasNext(); tem = tem + map3.get("column_name") + ",") {
                map3 = (Map) var21.next();
            }

            if (!tem.equals("")) {
                var22 = tem.substring(0, tem.length() - 1);
            }
        }

        ObjectMapper var25 = new ObjectMapper();
        String var26 = "[" + var25.writeValueAsString(tempList) + "]";
        page.setTotalCount((long) rowCount);
        page.setResult(list);
        page.setColumns(var26);
        page.setPrimaryKey(var22);
        page.setTableName(var23);
        return page;
    }

    public boolean checkSqlIsOneTableForMySql(String dbName, String sql) {
        String temp = "";
        String tableName = "";

        try {
            DBUtil.setDbName(dbName);
            Pattern p = Pattern.compile("\\s+");
            Matcher m = p.matcher(sql);
            temp = m.replaceAll(" ");
            temp = temp.toLowerCase();
            if (temp.indexOf("select * from") >= 0) {
                for (int sql2 = 14; sql2 < temp.length(); ++sql2) {
                    String list = String.valueOf(temp.charAt(sql2));
                    if (list.equals(" ")) {
                        break;
                    }

                    tableName = tableName + list;
                }

                String var11 = " select TABLE_NAME from information_schema.TABLES where table_schema='" + dbName + "' and table_type='BASE TABLE' and  TABLE_NAME ='" + tableName + "'";
                List var12 = DBUtil.queryForList(sql);
                if (var12.size() > 0) {
                    return true;
                }
            }

            return false;
        } catch (Exception var10) {
            return false;
        }
    }

    public List<Map<String, Object>> executeSqlForColumns(String sql, String dbName) throws Exception {
        String sql2 = " select * from  ( " + sql + " ) tab  limit 1 ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.executeSqlForColumns(sql2);
        return list;
    }

    public boolean saveSearchHistory(String name, String sql, String dbName) {
        SqliteUtil db = new SqliteUtil();
        String insertSQL = "insert into  treesoft_searchHistory ( createdate, sqls, name, database,user_id) values (  datetime('now') ,'" + sql + "','" + name + "','" + dbName + "','')";

        try {
            db.do_update(insertSQL);
            return true;
        } catch (Exception var7) {
            System.out.println(var7.getMessage());
            return false;
        }
    }

    public boolean updateSearchHistory(String id, String name, String sql, String dbName) {
        SqliteUtil db = new SqliteUtil();
        String insertSQL = "update  treesoft_searchHistory set createdate= datetime('now') , sqls='" + sql + "', name = '" + name + "', database='" + dbName + "' where id='" + id + "' ";

        try {
            db.do_update(insertSQL);
            return true;
        } catch (Exception var8) {
            System.out.println(var8.getMessage());
            return false;
        }
    }

    public boolean deleteSearchHistory(String id) {
        SqliteUtil db = new SqliteUtil();
        String insertSQL = "delete  from  treesoft_searchHistory  where id='" + id + "' ";

        try {
            db.do_update(insertSQL);
            return true;
        } catch (Exception var5) {
            System.out.println(var5.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> selectSearchHistory() {
        String sql = " select * from  treesoft_searchHistory ";
        return SqliteUtil.executeSqliteQuery(sql);
    }

    public boolean configUpdate(Config config) throws Exception {
        SqliteUtil db = new SqliteUtil();
        String id = config.getId();
        String sql = "";
        String isdefault = config.getIsdefault();
        if (isdefault == null) {
            isdefault = "0";
        }

        if (!id.equals("")) {
            sql = " update treesoft_config  set databaseType='" + config.getDatabaseType() + "' ," + "databaseName='" + config.getDatabaseName() + "' ," + "userName='" + config.getUserName() + "', " + "passwrod='" + config.getPassword() + "', " + "isdefault='" + isdefault + "', " + "port='" + config.getPort() + "', " + "ip='" + config.getIp() + "', " + "url='" + config.getUrl() + "'  where id='" + id + "'";
        } else {
            sql = " insert into treesoft_config (databaseType ,databaseName, userName ,passwrod , port, ip , isdefault , url ) values ( '" + config.getDatabaseType() + "','" + config.getDatabaseName() + "','" + config.getUserName() + "','" + config.getPassword() + "','" + config.getPort() + "','" + config.getIp() + "','" + isdefault + "','" + config.getUrl() + "' ) ";
        }

        boolean bl = db.do_update(sql);
        return bl;
    }

    public List<Map<String, Object>> selectUserById(String userId) {
        String sql = "select * from treesoft_users where id='" + userId + "'";
        return SqliteUtil.executeSqliteQuery(sql);
    }

    public boolean updateUserPass(String userId, String newPass) throws Exception {
        SqliteUtil db = new SqliteUtil();
        String sql = "update treesoft_users set password='" + newPass + "'  where id='" + userId + "'";
        boolean bl = db.do_update(sql);
        return bl;
    }

    public int executeSqlNotRes(String sql, String dbName) throws Exception {
        DBUtil.setDbName(dbName);
        return DBUtil.updateData(sql);
    }

    public int deleteRows(String databaseName, String tableName, String primary_key, String[] ids) throws Exception {
        int y = 0;
        for (int i = 0; i < ids.length; ++i) {
            String sql = " delete from  " + databaseName + "." + tableName + " where " + primary_key + " ='" + ids[i] + "'";
            y += DBUtil.updateData(sql);
        }

        return y;
    }

    public int deleteRowsNew(String databaseName, String tableName, String primary_key, List<String> condition) throws Exception {
        int y = 0;

        for (int i = 0; i < condition.size(); ++i) {
            String whereStr = condition.get(i);
            String sql = " delete from  " + databaseName + "." + tableName + " where 1=1 " + whereStr;
            y += DBUtil.updateData(sql);
        }

        return y;
    }

    public int saveRows(Map<String, String> map, String databaseName, String tableName) throws Exception {
        DBUtil.setDbName(databaseName);
        String sql = "insert into " + databaseName + "." + tableName;
        String colums = " ";
        String values = " ";
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, String> entry = (Entry) iterator.next();
            colums = colums + entry.getKey() + ",";
            String val = entry.getValue();
            if (val.equals("")) {
                values = values + " null ,";
            } else {
                values = values + "'" + val + "',";
            }
        }

        colums = colums.substring(0, colums.length() - 1);
        values = values.substring(0, values.length() - 1);
        sql = sql + " (" + colums + ") values (" + values + ")";
        return DBUtil.updateData(sql);
    }

    public List<Map<String, Object>> getOneRowById(String databaseName, String tableName, String id, String idValues) {
        DBUtil.setDbName(databaseName);
        String sql2 = " select * from   " + databaseName + "." + tableName + " where " + id + "='" + idValues + "' ";
        return DBUtil.queryForListWithType(sql2);
    }

    public int updateRows(Map<String, Object> map, String databaseName, String tableName, String id, String idValues) throws Exception {
        if (id != null && !"".equals(id)) {
            if (idValues != null && !"".equals(idValues)) {
                DBUtil.setDbName(databaseName);
                String sql = " update  " + databaseName + "." + tableName;
                boolean y = false;
                String ss = " set  ";
                Iterator var11 = map.entrySet().iterator();

                while (var11.hasNext()) {
                    Entry entry = (Entry) var11.next();
                    String str = "" + entry.getValue();
                    if (str.equals("")) {
                        ss = ss + (String) entry.getKey() + "= null ,";
                    } else if (entry.getValue() instanceof String) {
                        ss = ss + (String) entry.getKey() + "= '" + entry.getValue() + "',";
                    } else {
                        ss = ss + (String) entry.getKey() + "= " + entry.getValue() + ",";
                    }
                }

                ss = ss.substring(0, ss.length() - 1);
                sql = sql + ss + " where " + id + "='" + idValues + "'";
                int y1 = DBUtil.updateData(sql);
                return y1;
            } else {
                throw new Exception("数据不完整,保存失败!");
            }
        } else {
            throw new Exception("数据不完整,保存失败!");
        }
    }

    public String getViewSql(String databaseName, String tableName) throws Exception {
        String sql = " select  view_definition  from  information_schema.VIEWS  where  table_name='" + tableName + "' and table_schema='" + databaseName + "'  ";
        String str = "";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        if (list.size() == 1) {
            Map map = (Map) list.get(0);
            str = (String) map.get("view_definition");
        }

        return str;
    }

    public List<Map<String, Object>> getTableColumns2(String databaseName, String tableName) throws Exception {
        String sql = "select * from  " + databaseName + "." + tableName + " limit 1";
        DBUtil.setDbName(databaseName);
        return DBUtil.queryForColumnOnly(sql);
    }

    public String getPrimaryKeys(String databaseName, String tableName) {
        DBUtil.setDbName(databaseName);
        return DBUtil.getPrimaryKey(databaseName, tableName);
    }

    public List<Map<String, Object>> getPrimaryKeyss(String databaseName, String tableName) throws Exception {
        String sql = " select   column_name  from information_schema.columns where   table_name='" + tableName + "' and table_schema='" + databaseName + "' and column_key='PRI' ";
        DBUtil.setDbName(databaseName);
        return DBUtil.queryForList(sql);
    }

    public boolean testConn(String databaseType, String databaseName, String ip, String port, String user, String pass) {
        return databaseType.equals("Redis") ? RedisUtil.testConnForRedis(databaseType, databaseName, ip, port, user, pass) : (databaseType.equals("Memcache") ? MemcachedUtil.testConnection(databaseType, databaseName, ip, port, user, pass) : false);
    }

    public List<Map<String, Object>> selectSqlStudy() {
        String sql = " select id, title, content, pid,icon  from  treesoft_study   ";
        return SqliteUtil.executeSqliteQuery(sql);
    }

    public int saveDesginColumn(Map<String, String> map, String databaseName, String tableName) throws Exception {
        DBUtil.setDbName(databaseName);
        StringBuffer sb = new StringBuffer();
        sb.append(" alter table ").append(databaseName).append(".").append(tableName).append(" add column ");
        sb.append(map.get("COLUMN_NAME")).append(" ").append(map.get("DATA_TYPE"));
        if (map.get("CHARACTER_MAXIMUM_LENGTH") != null && !((String) map.get("CHARACTER_MAXIMUM_LENGTH")).equals("")) {
            sb.append(" (").append(map.get("CHARACTER_MAXIMUM_LENGTH")).append(") ");
        }

        if (map.get("COLUMN_COMMENT") != null && !((String) map.get("COLUMN_COMMENT")).equals("")) {
            sb.append(" comment '").append(map.get("COLUMN_COMMENT")).append("'");
        }

        return DBUtil.updateData(sb.toString());
    }

    public int deleteTableColumn(String databaseName, String tableName, String[] ids) throws Exception {
        DBUtil.setDbName(databaseName);
        int y = 0;

        for (int i = 0; i < ids.length; ++i) {
            String sql = " alter table   " + databaseName + "." + tableName + " drop column  " + ids[i];
            y += DBUtil.updateData(sql);
        }

        return y;
    }

    public int updateTableColumn(Map<String, Object> map, String databaseName, String tableName, String columnName, String idValues) throws Exception {
        if (columnName != null && !"".equals(columnName)) {
            if (idValues != null && !"".equals(idValues)) {
                DBUtil.setDbName(databaseName);
                String old_field_name = (String) map.get("TREESOFTPRIMARYKEY");
                String column_name = (String) map.get("COLUMN_NAME");
                String data_type = (String) map.get("DATA_TYPE");
                String character_maximum_length = "" + map.get("CHARACTER_MAXIMUM_LENGTH");
                String column_comment = (String) map.get("COLUMN_COMMENT");
                String sql2;
                int y;
                if (!old_field_name.endsWith(column_name)) {
                    sql2 = " alter table  " + databaseName + "." + tableName + " change ";
                    sql2 = sql2 + old_field_name + " " + column_name;
                    y = DBUtil.updateData(sql2);
                }

                sql2 = " alter table  " + databaseName + "." + tableName + " modify column " + column_name + " " + data_type;
                if (character_maximum_length != null && !character_maximum_length.equals("")) {
                    sql2 = sql2 + " (" + character_maximum_length + ")";
                }

                if (column_comment != null && !column_comment.equals("")) {
                    sql2 = sql2 + " comment '" + column_comment + "'";
                }

                y = DBUtil.updateData(sql2);
                return y;
            } else {
                throw new Exception("数据不完整,保存失败!");
            }
        } else {
            throw new Exception("数据不完整,保存失败!");
        }
    }

    public int dropPrimaryKey(String databaseName, String tableName) throws Exception {
        DBUtil.setDbName(databaseName);
        String sql4 = " alter table  " + databaseName + "." + tableName + " drop primary key ";
        DBUtil.updateData(sql4);
        return 0;
    }

    public int savePrimaryKey2(String databaseName, String tableName, String primaryKeys) throws Exception {
        String sql4 = "";
        if (primaryKeys != null && !primaryKeys.equals("")) {
            DBUtil.setDbName(databaseName);
            sql4 = " alter table  " + databaseName + "." + tableName + " add primary key (" + primaryKeys + ")";
            DBUtil.updateData(sql4);
        }

        return 0;
    }

    public int savePrimaryKey(String databaseName, String tableName, String column_name, String isSetting) throws Exception {
        String sql4 = "";
        if (column_name != null && !column_name.equals("")) {
            DBUtil.setDbName(databaseName);
            List list2 = this.selectTablePrimaryKey(databaseName, tableName);
            if (isSetting.equals("true")) {
                list2.add(column_name);
            } else {
                list2.remove(column_name);
            }

            String tem = list2.toString();
            String primaryKey = tem.substring(1, tem.length() - 1);
            if (primaryKey.equals("")) {
                sql4 = " alter table  " + databaseName + "." + tableName + " drop primary key ";
            } else if (list2.size() == 1 && isSetting.equals("true")) {
                sql4 = " alter table  " + databaseName + "." + tableName + " add primary key (" + primaryKey + ")";
            } else {
                sql4 = " alter table  " + databaseName + "." + tableName + " drop primary key, add primary key (" + primaryKey + ")";
            }

            DBUtil.updateData(sql4);
        }

        return 0;
    }

    public List<String> selectTablePrimaryKey(String databaseName, String tableName) throws Exception {
        String sql = " select column_name   from information_schema.columns where   table_name='" + tableName + "' and table_schema='" + databaseName + "'  and column_key='PRI' ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        ArrayList list2 = new ArrayList();
        Iterator var8 = list.iterator();

        while (var8.hasNext()) {
            Map map = (Map) var8.next();
            list2.add((String) map.get("column_name"));
        }

        return list2;
    }

    public String selectOneColumnType(String databaseName, String tableName, String column_name) throws Exception {
        String sql = " select   column_type  from information_schema.columns where   table_name='" + tableName + "' and table_schema='" + databaseName + "' and column_name='" + column_name + "'";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        return (String) ((Map) list.get(0)).get("column_type");
    }

    public int updateTableNullAble(String databaseName, String tableName, String column_name, String is_nullable) throws Exception {
        String sql4 = "";
        if (column_name != null && !column_name.equals("")) {
            DBUtil.setDbName(databaseName);
            String column_type = this.selectOneColumnType(databaseName, tableName, column_name);
            if (is_nullable.equals("true")) {
                sql4 = " alter table  " + databaseName + "." + tableName + " modify column " + column_name + " " + column_type + "  null ";
            } else {
                sql4 = " alter table  " + databaseName + "." + tableName + " modify column " + column_name + " " + column_type + " not null ";
            }

            DBUtil.updateData(sql4);
        }

        return 0;
    }

    public int upDownColumn(String databaseName, String tableName, String column_name, String column_name2) throws Exception {
        String sql4 = "";
        if (column_name != null && !column_name.equals("")) {
            DBUtil.setDbName(databaseName);
            String column_type = this.selectOneColumnType(databaseName, tableName, column_name);
            if (column_name2 != null && !column_name2.equals("")) {
                sql4 = " alter table  " + databaseName + "." + tableName + " modify column " + column_name + " " + column_type + " after " + column_name2;
            } else {
                sql4 = " alter table  " + databaseName + "." + tableName + " modify column " + column_name + " " + column_type + " first ";
            }

            DBUtil.updateData(sql4);
        }

        return 0;
    }

    public List<Map<String, Object>> getAllDataBaseForOracle() throws Exception {
        ArrayList list = new ArrayList();
        HashMap map = new HashMap();
        map.put("SCHEMA_NAME", Constants.DATABASE_NAME);
        list.add(map);
        return list;
    }

    public List<Map<String, Object>> selectUserByName(String userName) {
        String sql = " select * from  treesoft_users where username='" + userName + "' ";
        return SqliteUtil.executeSqliteQuery(sql);
    }

    public List<Map<String, Object>> getAllTablesForOracle(String dbName) throws Exception {
        DBUtil.setDbName(dbName);
        String sql = " select TABLE_NAME  from  user_tables  ";
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getTableColumns3ForOracle(String dbName, String tableName) throws Exception {
        String sql = "select t1.column_name as TREESOFTPRIMARYKEY, t1.COLUMN_NAME,  nvl2( t1.CHAR_COL_DECL_LENGTH,  t1.DATA_TYPE||'(' ||CHAR_COL_DECL_LENGTH||')',t1.DATA_TYPE ) as COLUMN_TYPE ,t1.data_type,   t1.data_length as CHARACTER_MAXIMUM_LENGTH ,CASE t1.nullable when 'Y' then 'YES' END as IS_NULLABLE  ,  nvl2(t3.column_name ,'PRI' ,'')  as COLUMN_KEY,  t2.comments as COLUMN_COMMENT  from user_tab_columns  t1   left join user_col_comments t2  on  t1.table_name = t2.table_name and t1.COLUMN_NAME = t2.COLUMN_NAME   left join   (select a.table_name, a.column_name   from user_cons_columns a, user_constraints b    where a.constraint_name = b.constraint_name    and b.constraint_type = 'P' ) t3    on t1.TABLE_NAME = t3.table_name  and t1.COLUMN_NAME = t3.COLUMN_NAME    where   t1.table_name= '" + tableName + "'  ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getAllViewsForOracle(String dbName) throws Exception {
        String sql = " select view_name as TABLE_NAME from  user_views  ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public String getViewSqlForOracle(String databaseName, String tableName) throws Exception {
        String sql = " select  view_definition  from  information_schema.VIEWS  where  table_name='" + tableName + "' and table_schema='" + databaseName + "'  ";
        String str = "";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        if (list.size() == 1) {
            Map map = (Map) list.get(0);
            str = (String) map.get("view_definition");
        }

        return str;
    }

    public List<Map<String, Object>> getAllFuntionForOracle(String dbName) throws Exception {
        String sql = " select object_name as ROUTINE_NAME from  user_procedures ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public Page<Map<String, Object>> getDataForOracle(Page<Map<String, Object>> page, String tableName, String dbName) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        String orderBy = page.getOrderBy();
        String order = page.getOrder();
        DBUtil.setDbName(dbName);
        List list3 = this.getPrimaryKeyssForOracle(dbName, tableName);
        String tem = "";

        Map primaryKey;
        for (Iterator sql = list3.iterator(); sql.hasNext(); tem = tem + primaryKey.get("COLUMN_NAME") + ",") {
            primaryKey = (Map) sql.next();
        }

        String primaryKey1 = "";
        if (!tem.equals("")) {
            primaryKey1 = tem.substring(0, tem.length() - 1);
        }

        String sql1 = "select * from  " + tableName;
        String sql2 = "";
        if (orderBy != null && !orderBy.equals("")) {
            sql2 = "select * from (select rownum rn, t1.* from " + tableName + " t1) where rn between " + limitFrom + " and  " + (limitFrom + pageSize) + " order by " + orderBy + " " + order;
        } else {
            sql2 = "select * from (select rownum rn, t1.* from " + tableName + " t1) where rn between " + limitFrom + " and  " + (limitFrom + pageSize);
        }

        List list = DBUtil.queryForList(sql2);
        int rowCount = DBUtil.executeQueryForCountForOracle(sql1);
        List columns = this.getTableColumnsForOracle(dbName, tableName);
        ArrayList tempList = new ArrayList();
        HashMap map1 = new HashMap();
        map1.put("field", "treeSoftPrimaryKey");
        map1.put("checkbox", Boolean.valueOf(true));
        tempList.add(map1);

        HashMap map2;
        for (Iterator jsonfromList = columns.iterator(); jsonfromList.hasNext(); tempList.add(map2)) {
            Map mapper = (Map) jsonfromList.next();
            map2 = new HashMap();
            map2.put("field", mapper.get("column_name"));
            map2.put("title", mapper.get("column_name"));
            map2.put("sortable", Boolean.valueOf(true));
            map2.put("editor", "text");
            if (!mapper.get("data_type").equals("DATETIME") && !mapper.get("data_type").equals("DATE")) {
                if (!mapper.get("data_type").equals("INT") && !mapper.get("data_type").equals("SMALLINT") && !mapper.get("data_type").equals("TINYINT")) {
                    if (mapper.get("data_type").equals("DOUBLE")) {
                        map2.put("editor", "numberbox");
                    } else {
                        map2.put("editor", "text");
                    }
                } else {
                    map2.put("editor", "numberbox");
                }
            } else {
                map2.put("editor", "datebox");
            }
        }

        ObjectMapper mapper1 = new ObjectMapper();
        String jsonfromList1 = "[" + mapper1.writeValueAsString(tempList) + "]";
        page.setTotalCount((long) rowCount);
        page.setResult(list);
        page.setColumns(jsonfromList1);
        page.setPrimaryKey(primaryKey1);
        return page;
    }

    public List<Map<String, Object>> getPrimaryKeyssForOracle(String databaseName, String tableName) throws Exception {
        String sql = "  select  COLUMN_NAME   from   user_cons_columns  where   constraint_name= (select  constraint_name  from user_constraints  where table_name = '" + tableName + "' and constraint_type ='P') ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getTableColumnsForOracle(String dbName, String tableName) throws Exception {
        String sql = "select  * from   " + tableName + " where rownum =1 ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForColumnOnly(sql);
        return list;
    }

    public Page<Map<String, Object>> executeSqlHaveResForOracle(Page<Map<String, Object>> page, String sql, String dbName) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        String sql2 = "SELECT * FROM (SELECT A.*, ROWNUM RN  FROM (  " + sql + " ) A ) WHERE RN BETWEEN " + limitFrom + " AND " + (limitFrom + pageSize);
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql2);
        int rowCount = DBUtil.executeQueryForCountForOracle(sql);
        List columns = this.executeSqlForColumnsForOracle(sql, dbName);
        ArrayList tempList = new ArrayList();
        Iterator jsonfromList = columns.iterator();

        while (jsonfromList.hasNext()) {
            Map mapper = (Map) jsonfromList.next();
            HashMap map2 = new HashMap();
            map2.put("field", mapper.get("column_name"));
            map2.put("title", mapper.get("column_name"));
            map2.put("sortable", Boolean.valueOf(true));
            tempList.add(map2);
        }

        ObjectMapper mapper1 = new ObjectMapper();
        String jsonfromList1 = "[" + mapper1.writeValueAsString(tempList) + "]";
        page.setTotalCount((long) rowCount);
        page.setResult(list);
        page.setColumns(jsonfromList1);
        return page;
    }

    public List<Map<String, Object>> executeSqlForColumnsForOracle(String sql, String dbName) throws Exception {
        String sql2 = " select * from (" + sql + ") where  rownum = 1 ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.executeSqlForColumns(sql2);
        return list;
    }

    public int updateTableNullAbleForOracle(String databaseName, String tableName, String column_name, String is_nullable) throws Exception {
        String sql4 = "";
        if (column_name != null && !column_name.equals("")) {
            DBUtil.setDbName(databaseName);
            if (is_nullable.equals("true")) {
                sql4 = " alter table  " + tableName + " modify   " + column_name + "  null ";
            } else {
                sql4 = " alter table  " + tableName + " modify   " + column_name + "  not null ";
            }

            DBUtil.updateData(sql4);
        }

        return 0;
    }

    public int savePrimaryKeyForOracle(String databaseName, String tableName, String column_name, String isSetting) throws Exception {
        String sql4 = "";
        if (column_name != null && !column_name.equals("")) {
            DBUtil.setDbName(databaseName);
            List list2 = this.selectTablePrimaryKeyForOracle(databaseName, tableName);
            ArrayList list3 = new ArrayList();
            Iterator primaryKey = list2.iterator();

            while (primaryKey.hasNext()) {
                Map tem = (Map) primaryKey.next();
                list3.add((String) tem.get("COLUMN_NAME"));
            }

            if (isSetting.equals("true")) {
                list3.add(column_name);
            } else {
                list3.remove(column_name);
            }

            String tem1 = list3.toString();
            String primaryKey1 = tem1.substring(1, tem1.length() - 1);
            if (list2.size() > 0) {
                String temp = (String) ((Map) list2.get(0)).get("CONSTRAINT_NAME");
                sql4 = " alter table   " + tableName + " drop constraint  " + temp;
                DBUtil.updateData(sql4);
            }

            if (!primaryKey1.equals("")) {
                sql4 = " alter table " + tableName + " add   primary key (" + primaryKey1 + ") ";
                DBUtil.updateData(sql4);
            }
        }

        return 0;
    }

    public List<Map<String, Object>> selectTablePrimaryKeyForOracle(String databaseName, String tableName) throws Exception {
        String sql = " select a.CONSTRAINT_NAME,  a.COLUMN_NAME  from user_cons_columns a, user_constraints b  where a.constraint_name = b.constraint_name   and b.constraint_type = 'P'  and a.table_name = '" + tableName + "' ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        new ArrayList();
        return list;
    }

    public int saveDesginColumnForOracle(Map<String, String> map, String databaseName, String tableName) throws Exception {
        DBUtil.setDbName(databaseName);
        String sql = " alter table " + tableName + " add  ";
        sql = sql + (String) map.get("COLUMN_NAME") + "  ";
        sql = sql + (String) map.get("DATA_TYPE");
        if (map.get("CHARACTER_MAXIMUM_LENGTH") != null && !((String) map.get("CHARACTER_MAXIMUM_LENGTH")).equals("")) {
            sql = sql + " (" + (String) map.get("CHARACTER_MAXIMUM_LENGTH") + ") ";
        }

        if (map.get("COLUMN_COMMENT") != null && !((String) map.get("COLUMN_COMMENT")).equals("")) {
            sql = sql + " comment '" + (String) map.get("COLUMN_COMMENT") + "'";
        }

        boolean y = false;
        int y1 = DBUtil.updateData(sql);
        return y1;
    }

    public int updateTableColumnForOracle(Map<String, Object> map, String databaseName, String tableName, String columnName, String idValues) throws Exception {
        if (columnName != null && !"".equals(columnName)) {
            if (idValues != null && !"".equals(idValues)) {
                DBUtil.setDbName(databaseName);
                String old_field_name = (String) map.get("TREESOFTPRIMARYKEY");
                String column_name = (String) map.get("COLUMN_NAME");
                String data_type = (String) map.get("DATA_TYPE");
                String character_maximum_length = "" + map.get("CHARACTER_MAXIMUM_LENGTH");
                String column_comment = (String) map.get("COLUMN_COMMENT");
                String sql2;
                int y;
                if (!old_field_name.endsWith(column_name)) {
                    sql2 = " ALTER TABLE " + tableName + " RENAME COLUMN " + old_field_name + " to  " + column_name;
                    y = DBUtil.updateData(sql2);
                }

                sql2 = " alter table  " + tableName + " modify  " + column_name + " " + data_type;
                if (character_maximum_length != null && !character_maximum_length.equals("")) {
                    sql2 = sql2 + " (" + character_maximum_length + ")";
                }

                y = DBUtil.updateData(sql2);
                if (column_comment != null && !column_comment.equals("")) {
                    String sql4 = "  comment on column " + tableName + "." + column_name + " is '" + column_comment + "' ";
                    DBUtil.updateData(sql4);
                }

                return y;
            } else {
                throw new Exception("数据不完整,保存失败!");
            }
        } else {
            throw new Exception("数据不完整,保存失败!");
        }
    }

    public int deleteTableColumnForOracle(String databaseName, String tableName, String[] ids) throws Exception {
        DBUtil.setDbName(databaseName);
        int y = 0;

        for (int i = 0; i < ids.length; ++i) {
            String sql = " alter table   " + tableName + " drop (" + ids[i] + ")";
            y += DBUtil.updateData(sql);
        }

        return y;
    }

    public int saveRowsForOracle(Map<String, String> map, String databaseName, String tableName) throws Exception {
        DBUtil.setDbName(databaseName);
        String sql = " insert into  " + tableName;
        boolean y = false;
        String colums = " ";
        String values = " ";
        String columnType = "";
        Iterator var11 = map.entrySet().iterator();

        while (var11.hasNext()) {
            Entry entry = (Entry) var11.next();
            colums = colums + (String) entry.getKey() + ",";
            columnType = this.selectColumnTypeForOracle(databaseName, tableName, (String) entry.getKey());
            String str = (String) entry.getValue();
            if (str.equals("")) {
                values = values + " null ,";
            } else if (columnType.equals("DATE")) {
                values = values + " to_date('" + (String) entry.getValue() + "' ,'yyyy-mm-dd hh24:mi:ss') ,";
            } else {
                values = values + "'" + (String) entry.getValue() + "',";
            }
        }

        colums = colums.substring(0, colums.length() - 1);
        values = values.substring(0, values.length() - 1);
        sql = sql + " (" + colums + ") values (" + values + ")";
        int y1 = DBUtil.updateData(sql);
        return y1;
    }

    public String selectColumnTypeForOracle(String databaseName, String tableName, String column) throws Exception {
        String sql = " select DATA_TYPE from user_tab_columns where table_name ='" + tableName + "' AND COLUMN_NAME ='" + column + "' ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        return (String) ((Map) list.get(0)).get("DATA_TYPE");
    }

    public int deleteRowsNewForOracle(String databaseName, String tableName, String primary_key, List<String> condition) throws Exception {
        DBUtil.setDbName(databaseName);
        int y = 0;

        for (int i = 0; i < condition.size(); ++i) {
            String whereStr = (String) condition.get(i);
            String sql = " delete from  " + tableName + " where  1=1 " + whereStr;
            y += DBUtil.updateData(sql);
        }

        return y;
    }

    public List<Map<String, Object>> getAllDataBaseForPostgreSQL() throws Exception {
        ArrayList list = new ArrayList();
        HashMap map = new HashMap();
        map.put("SCHEMA_NAME", Constants.DATABASE_NAME);
        list.add(map);
        return list;
    }

    public List<Map<String, Object>> getAllTablesForPostgreSQL(String dbName) throws Exception {
        DBUtil.setDbName(dbName);
        String sql = " select  tablename as \"TABLE_NAME\" from pg_tables  where schemaname='public'  ";
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getTableColumns3ForPostgreSQL(String dbName, String tableName) throws Exception {
        String sql = "   select t1.column_name as \"TREESOFTPRIMARYKEY\", t1.COLUMN_NAME as \"COLUMN_NAME\", t1.DATA_TYPE   as \"COLUMN_TYPE\" , t1.DATA_TYPE as \"DATA_TYPE\" , character_maximum_length as \"CHARACTER_MAXIMUM_LENGTH\" ,   t1.IS_NULLABLE as \"IS_NULLABLE\" ,  '' as \"COLUMN_COMMENT\" , CASE  WHEN t2.COLUMN_NAME IS NULL THEN ''  ELSE 'PRI'  END AS \"COLUMN_KEY\"   from information_schema.columns t1    left join information_schema.constraint_column_usage t2    on t1.table_name = t2.table_name  and t1.COLUMN_NAME = t2.COLUMN_NAME where  t1.table_name='" + tableName + "'    ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getAllViewsForPostgreSQL(String dbName) throws Exception {
        String sql = " select   viewname as \"TABLE_NAME\"  from pg_views  where schemaname='public'  ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getAllFuntionForPostgreSQL(String dbName) throws Exception {
        String sql = "  select prosrc as \"ROUTINE_NAME\" from pg_proc where 1=2  ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public Page<Map<String, Object>> getDataForPostgreSQL(Page<Map<String, Object>> page, String tableName, String dbName) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        String orderBy = page.getOrderBy();
        String order = page.getOrder();
        DBUtil.setDbName(dbName);
        List list3 = this.getPrimaryKeyssForPostgreSQL(dbName, tableName);
        String tem = "";

        Map primaryKey;
        for (Iterator sql = list3.iterator(); sql.hasNext(); tem = tem + primaryKey.get("COLUMN_NAME") + ",") {
            primaryKey = (Map) sql.next();
        }

        String primaryKey1 = "";
        if (!tem.equals("")) {
            primaryKey1 = tem.substring(0, tem.length() - 1);
        }

        String sql1 = "select * from  " + tableName;
        String sql2 = "";
        if (orderBy != null && !orderBy.equals("")) {
            sql2 = "select  *  from  " + tableName + " order by " + orderBy + " " + order + "  LIMIT " + pageSize + "  OFFSET " + limitFrom;
        } else {
            sql2 = "select  *  from  " + tableName + "  LIMIT " + pageSize + " OFFSET  " + limitFrom;
        }

        List list = DBUtil.queryForList(sql2);
        int rowCount = DBUtil.executeQueryForCountForPostgreSQL(sql1);
        List columns = this.getTableColumnsForPostgreSQL(dbName, tableName);
        ArrayList tempList = new ArrayList();
        HashMap map1 = new HashMap();
        map1.put("field", "treeSoftPrimaryKey");
        map1.put("checkbox", Boolean.valueOf(true));
        tempList.add(map1);

        HashMap map2;
        for (Iterator jsonfromList = columns.iterator(); jsonfromList.hasNext(); tempList.add(map2)) {
            Map mapper = (Map) jsonfromList.next();
            map2 = new HashMap();
            map2.put("field", mapper.get("column_name"));
            map2.put("title", mapper.get("column_name"));
            map2.put("sortable", Boolean.valueOf(true));
            map2.put("editor", "text");
            if (!mapper.get("data_type").equals("DATETIME") && !mapper.get("data_type").equals("DATE")) {
                if (!mapper.get("data_type").equals("INT") && !mapper.get("data_type").equals("SMALLINT") && !mapper.get("data_type").equals("TINYINT")) {
                    if (mapper.get("data_type").equals("DOUBLE")) {
                        map2.put("editor", "numberbox");
                    } else {
                        map2.put("editor", "text");
                    }
                } else {
                    map2.put("editor", "numberbox");
                }
            } else {
                map2.put("editor", "datebox");
            }
        }

        ObjectMapper mapper1 = new ObjectMapper();
        String jsonfromList1 = "[" + mapper1.writeValueAsString(tempList) + "]";
        page.setTotalCount((long) rowCount);
        page.setResult(list);
        page.setColumns(jsonfromList1);
        page.setPrimaryKey(primaryKey1);
        return page;
    }

    public Page<Map<String, Object>> executeSqlHaveResForPostgreSQL(Page<Map<String, Object>> page, String sql, String dbName) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        String sql2 = "select  *  from  (" + sql + ") t  LIMIT " + pageSize + " OFFSET  " + limitFrom;
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql2);
        int rowCount = DBUtil.executeQueryForCountForPostgreSQL(sql);
        List columns = this.executeSqlForColumnsForPostgreSQL(sql, dbName);
        ArrayList tempList = new ArrayList();
        Iterator jsonfromList = columns.iterator();

        while (jsonfromList.hasNext()) {
            Map mapper = (Map) jsonfromList.next();
            HashMap map2 = new HashMap();
            map2.put("field", mapper.get("column_name"));
            map2.put("title", mapper.get("column_name"));
            map2.put("sortable", Boolean.valueOf(true));
            tempList.add(map2);
        }

        ObjectMapper mapper1 = new ObjectMapper();
        String jsonfromList1 = "[" + mapper1.writeValueAsString(tempList) + "]";
        page.setTotalCount((long) rowCount);
        page.setResult(list);
        page.setColumns(jsonfromList1);
        return page;
    }

    public int deleteRowsNewForPostgreSQL(String databaseName, String tableName, String primary_key, List<String> condition) throws Exception {
        DBUtil.setDbName(databaseName);
        int y = 0;

        for (int i = 0; i < condition.size(); ++i) {
            String whereStr = (String) condition.get(i);
            String sql = " delete from  " + tableName + " where  1=1 " + whereStr;
            y += DBUtil.updateData(sql);
        }

        return y;
    }

    public int saveRowsForPostgreSQL(Map<String, String> map, String databaseName, String tableName) throws Exception {
        DBUtil.setDbName(databaseName);
        String sql = " insert into  " + tableName;
        boolean y = false;
        String colums = " ";
        String values = " ";
        String columnType = "";
        Iterator var11 = map.entrySet().iterator();

        while (var11.hasNext()) {
            Entry entry = (Entry) var11.next();
            colums = colums + (String) entry.getKey() + ",";
            columnType = this.selectColumnTypeForPostgreSQL(databaseName, tableName, (String) entry.getKey());
            String str = (String) entry.getValue();
            if (str.equals("")) {
                values = values + " null ,";
            } else if (columnType.equals("DATE")) {
                values = values + " to_date('" + (String) entry.getValue() + "' ,'yyyy-mm-dd hh24:mi:ss') ,";
            } else {
                values = values + "'" + (String) entry.getValue() + "',";
            }
        }

        colums = colums.substring(0, colums.length() - 1);
        values = values.substring(0, values.length() - 1);
        sql = sql + " (" + colums + ") values (" + values + ")";
        int y1 = DBUtil.updateData(sql);
        return y1;
    }

    public int saveDesginColumnForPostgreSQL(Map<String, String> map, String databaseName, String tableName) throws Exception {
        DBUtil.setDbName(databaseName);
        String sql = " alter table " + tableName + " add  ";
        sql = sql + (String) map.get("COLUMN_NAME") + "  ";
        sql = sql + (String) map.get("DATA_TYPE");
        if (map.get("CHARACTER_MAXIMUM_LENGTH") != null && !((String) map.get("CHARACTER_MAXIMUM_LENGTH")).equals("")) {
            sql = sql + " (" + (String) map.get("CHARACTER_MAXIMUM_LENGTH") + ") ";
        }

        if (map.get("COLUMN_COMMENT") != null && !((String) map.get("COLUMN_COMMENT")).equals("")) {
            sql = sql + " comment '" + (String) map.get("COLUMN_COMMENT") + "'";
        }

        boolean y = false;
        int y1 = DBUtil.updateData(sql);
        return y1;
    }

    public int updateTableColumnForPostgreSQL(Map<String, Object> map, String databaseName, String tableName, String columnName, String idValues) throws Exception {
        if (columnName != null && !"".equals(columnName)) {
            if (idValues != null && !"".equals(idValues)) {
                DBUtil.setDbName(databaseName);
                String old_field_name = (String) map.get("TREESOFTPRIMARYKEY");
                String column_name = (String) map.get("COLUMN_NAME");
                String data_type = (String) map.get("DATA_TYPE");
                String character_maximum_length = "" + map.get("CHARACTER_MAXIMUM_LENGTH");
                String column_comment = (String) map.get("COLUMN_COMMENT");
                String sql2;
                int y;
                if (!old_field_name.endsWith(column_name)) {
                    sql2 = " ALTER TABLE " + tableName + " RENAME COLUMN " + old_field_name + " to  " + column_name;
                    y = DBUtil.updateData(sql2);
                }

                sql2 = " alter table  " + tableName + " modify  " + column_name + " " + data_type;
                if (character_maximum_length != null && !character_maximum_length.equals("")) {
                    sql2 = sql2 + " (" + character_maximum_length + ")";
                }

                y = DBUtil.updateData(sql2);
                if (column_comment != null && !column_comment.equals("")) {
                    String sql4 = "  comment on column " + tableName + "." + column_name + " is '" + column_comment + "' ";
                    DBUtil.updateData(sql4);
                }

                return y;
            } else {
                throw new Exception("数据不完整,保存失败!");
            }
        } else {
            throw new Exception("数据不完整,保存失败!");
        }
    }

    public int deleteTableColumnForPostgreSQL(String databaseName, String tableName, String[] ids) throws Exception {
        DBUtil.setDbName(databaseName);
        int y = 0;

        for (int i = 0; i < ids.length; ++i) {
            String sql = " alter table   " + tableName + " drop (" + ids[i] + ")";
            y += DBUtil.updateData(sql);
        }

        return y;
    }

    public int updateTableNullAbleForPostgreSQL(String databaseName, String tableName, String column_name, String is_nullable) throws Exception {
        String sql4 = "";
        if (column_name != null && !column_name.equals("")) {
            DBUtil.setDbName(databaseName);
            if (is_nullable.equals("true")) {
                sql4 = " alter table  " + tableName + " alter column   " + column_name + " drop not null ";
            } else {
                sql4 = " alter table  " + tableName + " alter column   " + column_name + " set not null ";
            }

            DBUtil.updateData(sql4);
        }

        return 0;
    }

    public int savePrimaryKeyForPostgreSQL(String databaseName, String tableName, String column_name, String isSetting) throws Exception {
        String sql4 = "";
        if (column_name != null && !column_name.equals("")) {
            DBUtil.setDbName(databaseName);
            List list2 = this.selectTablePrimaryKeyForPostgreSQL(databaseName, tableName);
            ArrayList list3 = new ArrayList();
            Iterator primaryKey = list2.iterator();

            while (primaryKey.hasNext()) {
                Map tem = (Map) primaryKey.next();
                list3.add((String) tem.get("COLUMN_NAME"));
            }

            if (isSetting.equals("true")) {
                list3.add(column_name);
            } else {
                list3.remove(column_name);
            }

            String tem1 = list3.toString();
            String primaryKey1 = tem1.substring(1, tem1.length() - 1);
            if (list2.size() > 0) {
                String temp = (String) ((Map) list2.get(0)).get("CONSTRAINT_NAME");
                sql4 = " alter table   " + tableName + " drop constraint  " + temp;
                DBUtil.updateData(sql4);
            }

            if (!primaryKey1.equals("")) {
                sql4 = " alter table " + tableName + " add   primary key (" + primaryKey1 + ") ";
                DBUtil.updateData(sql4);
            }
        }

        return 0;
    }

    public List<Map<String, Object>> getPrimaryKeyssForPostgreSQL(String databaseName, String tableName) throws Exception {
        String sql = " select  pg_attribute.attname as \"COLUMN_NAME\" from   pg_constraint  inner join pg_class    on pg_constraint.conrelid = pg_class.oid    inner join pg_attribute on pg_attribute.attrelid = pg_class.oid    and  pg_attribute.attnum = pg_constraint.conkey[1]     inner join pg_type on pg_type.oid = pg_attribute.atttypid  where pg_class.relname = '" + tableName + "'  " + " and pg_constraint.contype='p' ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getTableColumnsForPostgreSQL(String dbName, String tableName) throws Exception {
        String sql = "select  * from   " + tableName + " limit 1 ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForColumnOnly(sql);
        return list;
    }

    public String selectColumnTypeForPostgreSQL(String databaseName, String tableName, String column) throws Exception {
        String sql = " select data_type as \"DATA_TYPE\"  from  information_schema.columns  where    table_name ='" + tableName + "' AND COLUMN_NAME ='" + column + "' ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        return (String) ((Map) list.get(0)).get("DATA_TYPE");
    }

    public List<Map<String, Object>> selectTablePrimaryKeyForPostgreSQL(String databaseName, String tableName) throws Exception {
        String sql = " select pg_constraint.conname as \"CONSTRAINT_NAME\" ,pg_attribute.attname as \"COLUMN_NAME\" ,pg_type.typname as typename from   pg_constraint  inner join pg_class   on pg_constraint.conrelid = pg_class.oid    inner join pg_attribute on pg_attribute.attrelid = pg_class.oid    and  pg_attribute.attnum = pg_constraint.conkey[1]   inner join pg_type on pg_type.oid = pg_attribute.atttypid    where pg_class.relname = '" + tableName + "'  " + "  and pg_constraint.contype='p' ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        new ArrayList();
        return list;
    }

    public List<Map<String, Object>> executeSqlForColumnsForPostgreSQL(String sql, String dbName) throws Exception {
        String sql2 = " select * from (" + sql + ") t   limit 1; ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.executeSqlForColumns(sql2);
        return list;
    }

    public String getViewSqlForPostgreSQL(String databaseName, String tableName) throws Exception {
        String sql = " select  view_definition  from  information_schema.views  where  table_name='" + tableName + "' and table_catalog='" + databaseName + "'  ";
        String str = " ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        if (list.size() == 1) {
            Map map = (Map) list.get(0);
            str = (String) map.get("view_definition");
        }

        return str;
    }

    public List<Map<String, Object>> getAllDataBaseForMSSQL() throws Exception {
        String sql = " SELECT Name as SCHEMA_NAME FROM Master..SysDatabases ORDER BY Name  ";
        DBUtil.setDbName(Constants.DATABASE_NAME);
        List list = DBUtil.queryForList(sql);
        ArrayList list2 = new ArrayList();
        HashMap map = new HashMap();
        map.put("SCHEMA_NAME", Constants.DATABASE_NAME);
        list2.add(map);

        for (int i = 0; i < list.size(); ++i) {
            Map map2 = (Map) list.get(i);
            String schema_name = (String) map2.get("SCHEMA_NAME");
            if (!schema_name.equals(Constants.DATABASE_NAME)) {
                list2.add(map2);
            }
        }

        return list2;
    }

    public List<Map<String, Object>> getAllTablesForMSSQL(String dbName) throws Exception {
        DBUtil.setDbName(dbName);
        String sql = " SELECT Name as TABLE_NAME FROM " + dbName + "..SysObjects Where XType='U' ORDER BY Name ";
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getTableColumns3ForMSSQL(String dbName, String tableName) throws Exception {
        String sql = " select b.name TREESOFTPRIMARYKEY, b.name COLUMN_NAME, ISNULL( c.name +'('+  cast(b.length as varchar(10)) +')' , c.name ) as  COLUMN_TYPE, c.name DATA_TYPE, b.length CHARACTER_MAXIMUM_LENGTH ,  case when b.isnullable=1  then 'YES' else 'NO' end IS_NULLABLE , '' as COLUMN_COMMENT , '' as COLUMN_KEY  from sysobjects a,syscolumns b,systypes c  where a.id=b.id  and a.name='" + tableName + "' and a.xtype='U'  and b.xtype=c.xtype ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getAllViewsForMSSQL(String dbName) throws Exception {
        String sql = "  SELECT  NAME AS TABLE_NAME FROM  sysobjects where XTYPE ='V'  ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getAllFuntionForMSSQL(String dbName) throws Exception {
        String sql = " SELECT  NAME AS ROUTINE_NAME FROM  sysobjects where XTYPE ='FN' ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public Page<Map<String, Object>> getDataForMSSQL(Page<Map<String, Object>> page, String tableName, String dbName) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        if (limitFrom > 0) {
            --limitFrom;
        }

        String orderBy = page.getOrderBy();
        String order = page.getOrder();
        DBUtil.setDbName(dbName);
        List list3 = this.getPrimaryKeyssForMSSQL(dbName, tableName);
        String tem = "";

        Map primaryKey;
        for (Iterator sql = list3.iterator(); sql.hasNext(); tem = tem + primaryKey.get("COLUMN_NAME") + ",") {
            primaryKey = (Map) sql.next();
        }

        String var24 = "";
        if (!tem.equals("")) {
            var24 = tem.substring(0, tem.length() - 1);
        }

        String var25 = "select * from  " + tableName;
        String sql2 = "";
        if (orderBy != null && !orderBy.equals("")) {
            sql2 = "select * from  " + tableName + " order by " + order;
        } else {
            sql2 = "select * from  " + tableName;
        }

        List list = DBUtil.queryForListPage(sql2, pageNo * pageSize, (pageNo - 1) * pageSize);
        int rowCount = DBUtil.executeQueryForCountForPostgreSQL(var25);
        List columns = this.getTableColumnsForMSSQL(dbName, tableName);
        ArrayList tempList = new ArrayList();
        HashMap map1 = new HashMap();
        map1.put("field", "treeSoftPrimaryKey");
        map1.put("checkbox", Boolean.valueOf(true));
        tempList.add(map1);

        HashMap map2;
        for (Iterator jsonfromList = columns.iterator(); jsonfromList.hasNext(); tempList.add(map2)) {
            Map mapper = (Map) jsonfromList.next();
            map2 = new HashMap();
            map2.put("field", mapper.get("column_name"));
            map2.put("title", mapper.get("column_name"));
            map2.put("sortable", Boolean.valueOf(true));
            map2.put("editor", "text");
            if (!mapper.get("data_type").equals("DATETIME") && !mapper.get("data_type").equals("DATE")) {
                if (!mapper.get("data_type").equals("INT") && !mapper.get("data_type").equals("SMALLINT") && !mapper.get("data_type").equals("TINYINT")) {
                    if (mapper.get("data_type").equals("DOUBLE")) {
                        map2.put("editor", "numberbox");
                    } else {
                        map2.put("editor", "text");
                    }
                } else {
                    map2.put("editor", "numberbox");
                }
            } else {
                map2.put("editor", "datebox");
            }
        }

        ObjectMapper var26 = new ObjectMapper();
        String var23 = "[" + var26.writeValueAsString(tempList) + "]";
        page.setTotalCount((long) rowCount);
        page.setResult(list);
        page.setColumns(var23);
        page.setPrimaryKey(var24);
        return page;
    }

    public Page<Map<String, Object>> executeSqlHaveResForMSSQL(Page<Map<String, Object>> page, String sql, String dbName) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        if (limitFrom > 0) {
            --limitFrom;
        }

        String sql2 = " select  * from (" + sql + ")  t1  ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForListPage(sql2, pageNo * pageSize, (pageNo - 1) * pageSize);
        int rowCount = DBUtil.executeQueryForCountForPostgreSQL(sql);
        List columns = this.executeSqlForColumnsForMSSQL(sql, dbName);
        ArrayList tempList = new ArrayList();
        Iterator jsonfromList = columns.iterator();

        while (jsonfromList.hasNext()) {
            Map mapper = (Map) jsonfromList.next();
            HashMap map2 = new HashMap();
            map2.put("field", mapper.get("column_name"));
            map2.put("title", mapper.get("column_name"));
            map2.put("sortable", Boolean.valueOf(true));
            tempList.add(map2);
        }

        ObjectMapper var16 = new ObjectMapper();
        String var17 = "[" + var16.writeValueAsString(tempList) + "]";
        page.setTotalCount((long) rowCount);
        page.setResult(list);
        page.setColumns(var17);
        return page;
    }

    public int deleteRowsNewForMSSQL(String databaseName, String tableName, String primary_key, List<String> condition) throws Exception {
        DBUtil.setDbName(databaseName);
        int y = 0;

        for (int i = 0; i < condition.size(); ++i) {
            String whereStr = (String) condition.get(i);
            String sql = " delete from  " + tableName + " where  1=1 " + whereStr;
            y += DBUtil.updateData(sql);
        }

        return y;
    }

    public String getViewSqlForMSSQL(String databaseName, String tableName) throws Exception {
        String sql = " select  view_definition  from  information_schema.views  where  table_name='" + tableName + "' and table_catalog='" + databaseName + "'  ";
        String str = " ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        if (list.size() == 1) {
            Map map = (Map) list.get(0);
            str = (String) map.get("view_definition");
        }

        return str;
    }

    public int saveRowsForMSSQL(Map<String, String> map, String databaseName, String tableName) throws Exception {
        DBUtil.setDbName(databaseName);
        String sql = " insert into  " + tableName;
        boolean y = false;
        String colums = " ";
        String values = " ";
        String columnType = "";
        Iterator var11 = map.entrySet().iterator();

        while (var11.hasNext()) {
            Entry entry = (Entry) var11.next();
            colums = colums + (String) entry.getKey() + ",";
            columnType = this.selectColumnTypeForPostgreSQL(databaseName, tableName, (String) entry.getKey());
            String str = (String) entry.getValue();
            if (str.equals("")) {
                values = values + " null ,";
            } else if (columnType.equals("DATE")) {
                values = values + " to_date('" + (String) entry.getValue() + "' ,'yyyy-mm-dd hh24:mi:ss') ,";
            } else {
                values = values + "'" + (String) entry.getValue() + "',";
            }
        }

        colums = colums.substring(0, colums.length() - 1);
        values = values.substring(0, values.length() - 1);
        sql = sql + " (" + colums + ") values (" + values + ")";
        int y1 = DBUtil.updateData(sql);
        return y1;
    }

    public int saveDesginColumnForMSSQL(Map<String, String> map, String databaseName, String tableName) throws Exception {
        DBUtil.setDbName(databaseName);
        String sql = " alter table " + tableName + " add  ";
        sql = sql + (String) map.get("COLUMN_NAME") + "  ";
        sql = sql + (String) map.get("DATA_TYPE");
        if (map.get("CHARACTER_MAXIMUM_LENGTH") != null && !((String) map.get("CHARACTER_MAXIMUM_LENGTH")).equals("")) {
            sql = sql + " (" + (String) map.get("CHARACTER_MAXIMUM_LENGTH") + ") ";
        }

        if (map.get("COLUMN_COMMENT") != null && !((String) map.get("COLUMN_COMMENT")).equals("")) {
            sql = sql + " comment '" + (String) map.get("COLUMN_COMMENT") + "'";
        }

        boolean y = false;
        int y1 = DBUtil.updateData(sql);
        return y1;
    }

    public int updateTableColumnForMSSQL(Map<String, Object> map, String databaseName, String tableName, String columnName, String idValues) throws Exception {
        if (columnName != null && !"".equals(columnName)) {
            if (idValues != null && !"".equals(idValues)) {
                DBUtil.setDbName(databaseName);
                String old_field_name = (String) map.get("TREESOFTPRIMARYKEY");
                String column_name = (String) map.get("COLUMN_NAME");
                String data_type = (String) map.get("DATA_TYPE");
                String character_maximum_length = "" + map.get("CHARACTER_MAXIMUM_LENGTH");
                String column_comment = (String) map.get("COLUMN_COMMENT");
                String sql2;
                int y;
                if (!old_field_name.endsWith(column_name)) {
                    sql2 = " ALTER TABLE " + tableName + " RENAME COLUMN " + old_field_name + " to  " + column_name;
                    y = DBUtil.updateData(sql2);
                }

                sql2 = " alter table  " + tableName + " alter column " + column_name + " " + data_type;
                if (character_maximum_length != null && !character_maximum_length.equals("")) {
                    sql2 = sql2 + " (" + character_maximum_length + ")";
                }

                y = DBUtil.updateData(sql2);
                if (column_comment != null && !column_comment.equals("")) {
                    String sql4 = "  comment on column " + tableName + "." + column_name + " is '" + column_comment + "' ";
                    DBUtil.updateData(sql4);
                }

                return y;
            } else {
                throw new Exception("数据不完整,保存失败!");
            }
        } else {
            throw new Exception("数据不完整,保存失败!");
        }
    }

    public int deleteTableColumnForMSSQL(String databaseName, String tableName, String[] ids) throws Exception {
        DBUtil.setDbName(databaseName);
        int y = 0;

        for (int i = 0; i < ids.length; ++i) {
            String sql = " alter table   " + tableName + " drop (" + ids[i] + ")";
            y += DBUtil.updateData(sql);
        }

        return y;
    }

    public int updateTableNullAbleForMSSQL(String databaseName, String tableName, String column_name, String is_nullable) throws Exception {
        String sql4 = "";
        if (column_name != null && !column_name.equals("")) {
            DBUtil.setDbName(databaseName);
            String column_type = this.selectOneColumnTypeForMSSQL(databaseName, tableName, column_name);
            if (is_nullable.equals("true")) {
                sql4 = " alter table  " + tableName + " alter column   " + column_name + " " + column_type + " " + "  null ";
            } else {
                sql4 = " alter table  " + tableName + " alter column   " + column_name + " " + column_type + " " + "  not null ";
            }

            DBUtil.updateData(sql4);
        }

        return 0;
    }

    public int savePrimaryKeyForMSSQL(String databaseName, String tableName, String column_name, String isSetting) throws Exception {
        String sql4 = "";
        if (column_name != null && !column_name.equals("")) {
            DBUtil.setDbName(databaseName);
            List list2 = this.selectTablePrimaryKeyForMSSQL(databaseName, tableName);
            ArrayList list3 = new ArrayList();
            Iterator primaryKey = list2.iterator();

            while (primaryKey.hasNext()) {
                Map tem = (Map) primaryKey.next();
                list3.add((String) tem.get("COLUMN_NAME"));
            }

            if (isSetting.equals("true")) {
                list3.add(column_name);
            } else {
                list3.remove(column_name);
            }

            String tem1 = list3.toString();
            String primaryKey1 = tem1.substring(1, tem1.length() - 1);
            if (list2.size() > 0) {
                String temp = (String) ((Map) list2.get(0)).get("CONSTRAINT_NAME");
                sql4 = " alter table   " + tableName + " drop constraint  " + temp;
                DBUtil.updateData(sql4);
            }

            if (!primaryKey1.equals("")) {
                sql4 = " alter table " + tableName + " add   primary key (" + primaryKey1 + ") ";
                DBUtil.updateData(sql4);
            }
        }

        return 0;
    }

    public List<Map<String, Object>> selectTablePrimaryKeyForMSSQL(String databaseName, String tableName) throws Exception {
        String sql = " select pg_constraint.conname as \"CONSTRAINT_NAME\" ,pg_attribute.attname as \"COLUMN_NAME\" ,pg_type.typname as typename from   pg_constraint  inner join pg_class   on pg_constraint.conrelid = pg_class.oid    inner join pg_attribute on pg_attribute.attrelid = pg_class.oid    and  pg_attribute.attnum = pg_constraint.conkey[1]   inner join pg_type on pg_type.oid = pg_attribute.atttypid    where pg_class.relname = '" + tableName + "'  " + "  and pg_constraint.contype='p' ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        new ArrayList();
        return list;
    }

    public List<Map<String, Object>> getPrimaryKeyssForMSSQL(String databaseName, String tableName) throws Exception {
        String sql = " select  c.name as COLUMN_NAME from sysindexes i   join sysindexkeys k on i.id = k.id and i.indid = k.indid    join sysobjects o on i.id = o.id    join syscolumns c on i.id=c.id and k.colid = c.colid    where o.xtype = 'U'   and exists(select 1 from sysobjects where  xtype = 'PK'  and name = i.name)     and o.name='" + tableName + "' ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        return list;
    }

    public List<Map<String, Object>> getTableColumnsForMSSQL(String dbName, String tableName) throws Exception {
        String sql = "select top 1 * from   " + tableName;
        DBUtil.setDbName(dbName);
        List list = DBUtil.queryForColumnOnly(sql);
        return list;
    }

    public List<Map<String, Object>> executeSqlForColumnsForMSSQL(String sql, String dbName) throws Exception {
        String sql2 = " select top 1 * from (" + sql + ") t  ";
        DBUtil.setDbName(dbName);
        List list = DBUtil.executeSqlForColumns(sql2);
        return list;
    }

    public String selectOneColumnTypeForMSSQL(String databaseName, String tableName, String column_name) throws Exception {
        String sql = " select  ISNULL( c.name +'('+  cast(b.length as varchar(10)) +')' , c.name ) as  column_type  from sysobjects a,syscolumns b,systypes c  where a.id=b.id  and a.name='" + tableName + "'  and  b.name='" + column_name + "' and a.xtype='U'  and b.xtype=c.xtype ";
        DBUtil.setDbName(databaseName);
        List list = DBUtil.queryForList(sql);
        return (String) ((Map) list.get(0)).get("column_type");
    }

    public boolean backupDatabaseExecute(String databaseName, String path) throws Exception {
        try {
            this.backupForMysql(databaseName, path);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return true;
    }

    public void backupForMysql(String databaseName, String path) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = databaseName + "-" + df.format(new Date()) + ".sql";

        try {
            Runtime e = Runtime.getRuntime();
            Properties prop = System.getProperties();
            String os = prop.getProperty("os.name");
            String cmd = "";
            if (!os.startsWith("win") && !os.startsWith("Win")) {
                cmd = "mysqldump -h " + Constants.IP + " -u" + Constants.USER_NAME + " -p" + Constants.PASS_WROD + "  " + Constants.DATABASE_NAME + " > " + path + "backup" + File.separator + fileName;
                e.exec(new String[]{"sh", "-c", cmd});
            } else {
                cmd = "cmd /c " + path + "WEB-INF\\lib\\mysqldump -h " + Constants.IP + " -u" + Constants.USER_NAME + " -p" + Constants.PASS_WROD + "  " + Constants.DATABASE_NAME + " > " + path + "backup" + File.separator + fileName;
                e.exec(cmd);
            }
        } catch (Exception var10) {
            System.out.println(var10.getMessage());
            var10.printStackTrace();
        }

    }

    public boolean deleteBackupFile(String[] ids, String path) throws Exception {
        for (int i = 0; i < ids.length; ++i) {
            File f = new File(path + ids[i]);
            if (f.exists()) {
                f.delete();
            }
        }

        return true;
    }

    public Page<Map<String, Object>> getNoSQLDBForMemcached(Page<Map<String, Object>> page, String NoSQLDbName, String databaseConfigId, String selectKey, String selectValue) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        Map map3 = this.getConfig(databaseConfigId);
        String ip = (String) map3.get("ip");
        String port = (String) map3.get("port");
        String password = (String) map3.get("password");
        new MemcachedUtil();
        Map tempMap = MemcachedUtil.getAllKeyAndValue(pageSize, limitFrom, NoSQLDbName, selectKey, selectValue, ip, port, password);
        page.setTotalCount(((Integer) tempMap.get("rowCount")).intValue());
        page.setResult((List) tempMap.get("dataList"));
        return page;
    }

    public Page<Map<String, Object>> configList(Page<Map<String, Object>> page) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        SqliteUtil du = new SqliteUtil();
        List list = du.getConfigList();
        int rowCount = list.size();
        page.setTotalCount(rowCount);
        page.setResult(list);
        return page;
    }

    public int getDbAmountForRedis(Map<String, Object> map) {
        return RedisUtil.getDbAmountForRedis(map);
    }

    public List<String> getReidsConfig() {
        ArrayList li = new ArrayList();
        String list = RedisUtil.get("test");
        return li;
    }

    public Page<Map<String, Object>> getNoSQLDBForRedis(Page<Map<String, Object>> page, String NoSQLDbName, String databaseConfigId, String selectKey, String selectValue) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        Map map = this.getConfig(databaseConfigId);
        Map tempMap = RedisUtil.getNoSQLDBForRedis(map, pageSize, limitFrom, NoSQLDbName, selectKey, selectValue);
        page.setTotalCount(((Integer) tempMap.get("rowCount")).intValue());
        page.setResult((List) tempMap.get("dataList"));
        return page;
    }

    public List<Map<String, Object>> getConfigAllDataBase() throws Exception {
        String sql = " select id, databaseType , port, ip  from  treesoft_config order by isdefault desc ";
        List list = SqliteUtil.executeSqliteQuery(sql);
        return list;
    }

    public int deleteNoSQLKeyForRedis(String databaseConfigId, String NoSQLDbName, String[] ids) throws Exception {
        byte y = 0;
        Map map = this.getConfig(databaseConfigId);
        RedisUtil.deleteKeys(map, NoSQLDbName, ids);
        return y;
    }

    public int deleteNoSQLKeyForMemcached(String databaseConfigId, String NoSQLDbName, String[] ids) throws Exception {
        int y = 0;
        Map map3 = this.getConfig(databaseConfigId);
        String ip = (String) map3.get("ip");
        String port = (String) map3.get("port");
        String password = (String) map3.get("password");
        MemcachedUtil memcached = new MemcachedUtil();

        for (int i = 0; i < ids.length; ++i) {
            memcached.memcachedDelete(ids[i], ip, port, password);
            ++y;
        }

        return y;
    }

    public boolean saveNotSqlDataForRedis(NotSqlEntity notSqlEntity, String databaseConfigId, String NoSQLDbName) throws Exception {
        Map config = this.getConfig(databaseConfigId);
        boolean bl = RedisUtil.set(config, notSqlEntity, NoSQLDbName);
        return bl;
    }

    public boolean saveNotSqlDataForMemcached(NotSqlEntity notSqlEntity, String databaseConfigId) throws Exception {
        Map map3 = this.getConfig(databaseConfigId);
        String ip = (String) map3.get("ip");
        String port = (String) map3.get("port");
        String password = (String) map3.get("password");
        MemcachedUtil dt = new MemcachedUtil();
        boolean bl = dt.memcachedSet(notSqlEntity, ip, port, password);
        return bl;
    }

    public Map<String, Object> selectNotSqlDataForRedis(String key, String NoSQLDbName, String databaseConfigId) {
        Map config = this.getConfig(databaseConfigId);
        return RedisUtil.get(config, key, NoSQLDbName);
    }

    public Map<String, Object> selectNotSqlDataForMemcached(String key, String databaseConfigId) throws Exception {
        Map map3 = this.getConfig(databaseConfigId);
        String ip = (String) map3.get("ip");
        String port = (String) map3.get("port");
        String password = (String) map3.get("password");
        new MemcachedUtil();
        new HashMap();
        Map map = MemcachedUtil.memcachedGet3(key, ip, port, password);
        return map;
    }

    public Page<Map<String, Object>> selectNoSQLDBStatusForRedis(Page<Map<String, Object>> page, String databaseConfigId) throws Exception {
        ArrayList list = new ArrayList();
        Map map3 = this.getConfig(databaseConfigId);
        String info = RedisUtil.getInfo(map3);
        Properties properties = new Properties();
        ByteArrayInputStream inStream = new ByteArrayInputStream(info.getBytes());
        properties.load(inStream);
        boolean totalKeys = false;
        String parameter = "";
        String value = "";

        HashMap rowCount;
        for (Iterator it = properties.entrySet().iterator(); it.hasNext(); list.add(rowCount)) {
            rowCount = new HashMap();
            Entry entry = (Entry) it.next();
            parameter = (String) entry.getKey();
            value = (String) entry.getValue();
            rowCount.put("parameter", parameter);
            if (parameter.equals("redis_version")) {
                rowCount.put("value", value);
                rowCount.put("content", "redis版本 ");
            } else if (parameter.equals("aof_enabled")) {
                rowCount.put("value", value);
                rowCount.put("content", "Redis是否开启了aof");
            } else if (parameter.equals("used_memory_peak")) {
                rowCount.put("value", value);
                rowCount.put("content", "Redis所用内存的高峰值");
            } else if (parameter.equals("used_memory_peak_human")) {
                rowCount.put("value", value);
                rowCount.put("content", "Redis所用内存的高峰值");
            } else if (parameter.equals("used_memory_human")) {
                rowCount.put("value", value);
                rowCount.put("content", "Redis分配的内存总量");
            } else if (parameter.equals("connected_clients")) {
                rowCount.put("value", value);
                rowCount.put("content", "连接的客户端数量");
            } else if (parameter.equals("mem_fragmentation_ratio")) {
                rowCount.put("value", value);
                rowCount.put("content", "内存碎片比率");
            } else if (parameter.equals("used_memory")) {
                rowCount.put("value", value);
                rowCount.put("content", "Redis分配的内存总量");
            } else if (parameter.equals("total_connections_received")) {
                rowCount.put("value", value);
                rowCount.put("content", "运行以来连接过的客户端的总数量");
            } else if (parameter.equals("role")) {
                rowCount.put("value", value);
                rowCount.put("content", "当前实例的角色master还是slave");
            } else if (parameter.equals("keyspace_misses")) {
                rowCount.put("value", value);
                rowCount.put("content", "没命中key 的次数");
            } else if (parameter.equals("expired_keys")) {
                rowCount.put("value", value);
                rowCount.put("content", "运行以来过期的 key 的数量");
            } else if (parameter.equals("keyspace_hits")) {
                rowCount.put("value", value);
                rowCount.put("content", "命中key 的次数");
            } else if (parameter.equals("last_save_time")) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(value + "000"));
                rowCount.put("value", formatter.format(calendar.getTime()));
                rowCount.put("content", "上次保存RDB文件的时间");
            } else {
                rowCount.put("value", value);
                rowCount.put("content", "");
            }
        }

        int rowCount1 = list.size();
        page.setTotalCount((long) rowCount1);
        page.setResult(list);
        return page;
    }

    public Map<String, Object> queryInfoItemForRedis(String databaseConfigId) throws Exception {
        Map map3 = this.getConfig(databaseConfigId);
        String info = RedisUtil.getInfo(map3);
        Properties properties = new Properties();
        ByteArrayInputStream inStream = new ByteArrayInputStream(info.getBytes());
        properties.load(inStream);
        HashMap map = new HashMap();
        int totalKeys = 0;
        String parameter = "";
        String value = "";
        Iterator it = properties.entrySet().iterator();

        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            parameter = (String) entry.getKey();
            value = (String) entry.getValue();
            if (parameter.indexOf("db") == 0) {
                String ssi = value.substring(5, value.indexOf(","));
                totalKeys += Integer.parseInt(ssi);
            } else {
                map.put(parameter, value);
            }
        }

        map.put("totalKeys", Integer.valueOf(totalKeys));
        return map;
    }

    public Map<String, Object> queryInfoItemForMemcached(String databaseConfigId) throws Exception {
        Map map3 = this.getConfig(databaseConfigId);
        String ip = (String) map3.get("ip");
        String port = (String) map3.get("port");
        String password = (String) map3.get("password");
        new MemcachedUtil();
        Map info = MemcachedUtil.memcachedStatus(ip, port, password);
        String parameter = "";
        String value = "";
        Set set = info.entrySet();
        Iterator i = set.iterator();
        HashMap map = new HashMap();

        while (i.hasNext()) {
            Entry entry1 = (Entry) i.next();
            map.put((String) entry1.getKey(), entry1.getValue());
            if (((String) entry1.getKey()).equals("curr_connections")) {
                map.put("connected_clients", entry1.getValue());
            }

            if (((String) entry1.getKey()).equals("curr_items")) {
                map.put("totalKeys", entry1.getValue());
            }

            if (((String) entry1.getKey()).equals("bytes")) {
                map.put("used_memory", entry1.getValue());
                map.put("used_memory_peak", entry1.getValue());
            }

            if (((String) entry1.getKey()).equals("bytes")) {
                int size = Integer.parseInt((String) entry1.getValue());
                if (size < 1024) {
                    map.put("used_memory_human", size + "Byte");
                } else {
                    size /= 1024;
                    if (size < 1024) {
                        map.put("used_memory_human", size + "KB");
                    } else {
                        size /= 1024;
                        if (size < 1024) {
                            size *= 100;
                            map.put("used_memory_human", size / 100 + "." + size % 100 + "MB");
                        } else {
                            size = size * 100 / 1024;
                            map.put("used_memory_human", size + size / 100 + "." + size % 100 + "GB");
                        }
                    }
                }
            } else if (((String) entry1.getKey()).equals("total_items")) {
                map.put("total_commands_processed", entry1.getValue());
            }
        }

        return map;
    }

    public Page<Map<String, Object>> selectNoSQLDBStatusForMemcached(Page<Map<String, Object>> page, String databaseConfigId) throws Exception {
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int limitFrom = (pageNo - 1) * pageSize;
        ArrayList list = new ArrayList();
        Map map3 = this.getConfig(databaseConfigId);
        String ip = (String) map3.get("ip");
        String port = (String) map3.get("port");
        String password = (String) map3.get("password");
        new MemcachedUtil();
        Map info = MemcachedUtil.memcachedStatus(ip, port, password);
        String parameter = "";
        String value = "";
        Set set = info.entrySet();

        HashMap map;
        for (Iterator i = set.iterator(); i.hasNext(); list.add(map)) {
            Entry rowCount = (Entry) i.next();
            map = new HashMap();
            map.put("parameter", rowCount.getKey());
            map.put("value", rowCount.getValue());
            if (((String) rowCount.getKey()).equals("version")) {
                map.put("content", "Memcached版本 ");
            } else if (((String) rowCount.getKey()).equals("uptime")) {
                map.put("content", "服务器已经运行的秒数");
            } else if (((String) rowCount.getKey()).equals("pointer_size")) {
                map.put("content", "当前操作系统的指针大小（32位系统一般是32bit,64就是64位操作系统） ");
            } else if (((String) rowCount.getKey()).equals("rusage_system")) {
                map.put("content", "进程的累计系统时间 ");
            } else if (((String) rowCount.getKey()).equals("curr_connections")) {
                map.put("content", "当前打开连接数 ");
            } else if (((String) rowCount.getKey()).equals("cmd_flush")) {
                map.put("content", "flush命令请求次数 ");
            } else if (((String) rowCount.getKey()).equals("get_hits")) {
                map.put("content", "总命中次数 ");
            } else if (((String) rowCount.getKey()).equals("cmd_set")) {
                map.put("content", "set命令总请求次数 ");
            } else if (((String) rowCount.getKey()).equals("cmd_get")) {
                map.put("content", "get命令总请求次数 ");
            } else if (((String) rowCount.getKey()).equals("pid")) {
                map.put("content", "进程ID ");
            } else if (((String) rowCount.getKey()).equals("total_connections")) {
                map.put("content", "曾打开的连接总数 ");
            } else if (((String) rowCount.getKey()).equals("connection_structures")) {
                map.put("content", "服务器分配的连接结构数 ");
            } else if (((String) rowCount.getKey()).equals("bytes_read")) {
                map.put("content", "读取字节总数 ");
            } else if (((String) rowCount.getKey()).equals("limit_maxbytes")) {
                map.put("content", "分配的内存数（字节） ");
            } else if (((String) rowCount.getKey()).equals("get_misses")) {
                map.put("content", "总未命中次数 ");
            } else if (((String) rowCount.getKey()).equals("delete_hits")) {
                map.put("content", "delete命令命中次数 ");
            } else if (((String) rowCount.getKey()).equals("incr_misses")) {
                map.put("content", "incr命令未命中次数 ");
            } else if (((String) rowCount.getKey()).equals("threads")) {
                map.put("content", "当前线程数 ");
            } else if (((String) rowCount.getKey()).equals("bytes")) {
                map.put("content", "当前存储占用的字节数 ");
            } else if (((String) rowCount.getKey()).equals("curr_items")) {
                map.put("content", "当前存储的数据总数 ");
            } else if (((String) rowCount.getKey()).equals("total_items")) {
                map.put("content", "启动以来存储的数据总数 ");
            } else {
                map.put("content", " ");
            }
        }

        int rowCount1 = list.size();
        page.setTotalCount((long) rowCount1);
        page.setResult(list);
        return page;
    }

    public boolean backupNotSqlDatabaseForRedis(String databaseConfigId, String path) throws Exception {
        Map map3 = this.getConfig(databaseConfigId);
        String ip = (String) map3.get("ip");
        String port = (String) map3.get("port");
        RedisUtil.bgSave(map3);
        String redisDir = RedisUtil.getConfig(map3, "dir");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = ip + "(" + port + ")-" + df.format(new Date()) + ".rdb";
        File srcFile = new File(redisDir + File.separator + "dump.rdb");
        if (!srcFile.exists()) {
            throw new FileNotFoundException();
        } else {
            File tarFile = new File(path + File.separator + "backup" + File.separator + fileName);
            if (srcFile.exists()) {
                FileUtil.copyFile(srcFile, tarFile);
                return true;
            } else {
                return false;
            }
        }
    }

    public List<String> getAllDataBaseForMemcached(String ip, String port, String password) {
        new MemcachedUtil();
        return MemcachedUtil.getAllDataBaseForMemcached(ip, port, password);
    }

    public Map<String, Object> getConfig(String id) {
        String sql = " select id, databaseType , databaseName, userName , passwrod as password, port, ip ,url ,isdefault from  treesoft_config where id='" + id + "'";
        List list = SqliteUtil.executeSqliteQuery(sql);
        Map map = (Map) list.get(0);
        return map;
    }

    public boolean deleteConfig(String[] ids) throws Exception {
        SqliteUtil db = new SqliteUtil();
        StringBuffer sb = new StringBuffer();

        for (int newStr = 0; newStr < ids.length; ++newStr) {
            sb = sb.append("'" + ids[newStr] + "',");
        }

        String var8 = sb.toString();
        String str3 = var8.substring(0, var8.length() - 1);
        String sql = "  delete  from  treesoft_config where id in (" + str3 + ")";
        boolean bl = db.do_update(sql);
        return bl;
    }

    public boolean flushAllForRedis(String databaseConfigId) throws Exception {
        Map map3 = this.getConfig(databaseConfigId);
        return RedisUtil.flushAllForRedis(map3);
    }

    public boolean authorize() throws Exception {
        String sql = " select id, computer, license,valid  from  treesoft_config   ";
        List list = SqliteUtil.executeSqliteQuery(sql);
        Map map = (Map) list.get(0);
        String computer = (String) map.get("computer");
        String license = (String) map.get("license");
        String valid = (String) map.get("valid");
        return true;
    }
}
