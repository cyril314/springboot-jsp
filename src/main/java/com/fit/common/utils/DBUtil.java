package com.fit.common.utils;

import com.fit.common.Constants;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @className: DBUtil
 * @description: 数据库操作工具类
 * @author: Aim
 * @date: 2023/4/11
 **/
@Slf4j
public class DBUtil {

    private static String driver = Constants.DRIVER;
    private static String databaseType = Constants.DATABASE_TYPE;
    private static String databaseName = Constants.DATABASE_NAME;
    private static String url = Constants.URL;
    private static String userName = Constants.USER_NAME;
    private static String passwd = Constants.PASS_WROD;
    private static String port = Constants.PORT;
    private static String ip = Constants.IP;

    private static Connection conn = null;

    public static void setDbName(String dbName) {
        if (Constants.DATABASE_TYPE.equals("MySql")) {
            url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?characterEncoding=utf8&tinyInt1isBit=false";
        }

        if (Constants.DATABASE_TYPE.equals("Oracle")) {
            driver = "oracle.jdbc.driver.OracleDriver";
            url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + dbName;
        }

        if (Constants.DATABASE_TYPE.equals("PostgreSQL")) {
            driver = "org.postgresql.Driver";
            url = "jdbc:postgresql://" + ip + ":" + port + "/" + dbName;
        }

        if (Constants.DATABASE_TYPE.equals("MSSQL")) {
            driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            url = "jdbc:sqlserver://" + ip + ":" + port + ";database=" + dbName;
        }
    }

    public static boolean testConnection(String dbType, String dbName, String tIp, String tPort, String tUser, String tPass) {
        try {
            String e = "";
            if (dbType.equals("MySql")) {
                Class.forName("com.mysql.jdbc.Driver");
                e = "jdbc:mysql://" + tIp + ":" + tPort + "/" + dbName + "?characterEncoding=utf8";
            }

            if (dbType.equals("Oracle")) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                e = "jdbc:oracle:thin:@" + tIp + ":" + tPort + ":" + dbName;
            }

            if (dbType.equals("PostgreSQL")) {
                Class.forName("org.postgresql.Driver");
                e = "jdbc:postgresql://" + tIp + ":" + tPort + "/" + dbName;
            }

            if (dbType.equals("MSSQL")) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                e = "jdbc:sqlserver://" + tIp + ":" + tPort + ";database=" + dbName;
            }

            Connection conn = DriverManager.getConnection(e, tUser, tPass);
            return conn != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static int updateData(String sql) {
        Statement stmt = null;
        int stats;
        try {
            hasConnect();
            stmt = conn.createStatement();
            stats = stmt.executeUpdate(sql);
        } catch (Exception e) {
            log.error("执行更新失败: {}，\n执行SQL: {}", e.getMessage(), sql);
            throw new RuntimeException(e.getMessage());
        } finally {
            dbClose(conn, stmt);
        }

        return stats;
    }

    public static List<Map<String, Object>> queryForList(String sql) {
        return queryForListPage(sql, 0, 0);
    }

    public static List<Map<String, Object>> queryForList(String sql, int maxRow, int beginIndex) {
        return queryForListPage(sql, maxRow, beginIndex);
    }

    /**
     * 分页查询
     *
     * @param sql        查询一句
     * @param maxRow     最大行数
     * @param beginIndex 开始行数
     * @return
     */
    public static List<Map<String, Object>> queryForListPage(String sql, int maxRow, int beginIndex) {
        ArrayList rows = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            hasConnect();
            ps = conn.prepareStatement(sql, 1005, 1008);
            ps.setMaxRows(maxRow);//最大行数限制设置为给定的数目
            rs = ps.executeQuery();
            rs.absolute(beginIndex);//移到的行号

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ResultSetMetaData rsd = rs.getMetaData();
            int columnCount = rsd.getColumnCount();
            while (null != rs && rs.next()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                for (int i = 0; i < columnCount; i++) {
                    String name = rsd.getColumnLabel(i + 1);
                    if ("java.sql.Timestamp".equals(rsd.getColumnClassName(i + 1))) {
                        map.put(name, sdf.format(rs.getTimestamp(name)));
                    } else {
                        map.put(name, rs.getObject(name));
                    }

                }
                rows.add(map);
            }
        } catch (SQLException e) {
            log.error("分页查询失败，{}", e.getMessage());
        } finally {
            dbClose(conn, ps, rs);
        }
        return rows;
    }

    public static List<Map<String, Object>> queryForListWithType(String sql) {
        return queryForColumns(sql, true, true);
    }

    public static List<Map<String, Object>> queryForColumnOnly(String sql) {
        return queryForColumns(sql, false, true);
    }

    public static List<Map<String, Object>> executeSqlForColumns(String sql) {
        return queryForColumns(sql, false, false);
    }

    public static List<Map<String, Object>> queryForColumns(String sql, boolean hasVal, boolean hasOther) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            hasConnect();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (null != rs && rs.next()) {
                ResultSetMetaData e = rs.getMetaData();
                int columnCount = e.getColumnCount();
                for (int i = 1; i < columnCount + 1; ++i) {
                    Map<String, Object> map = new LinkedHashMap<String, Object>();
                    map.put("column_name", e.getColumnName(i));
                    if (hasVal) {
                        map.put("column_value", rs.getObject(e.getColumnName(i)));
                    }
                    map.put("data_type", e.getColumnTypeName(i));
                    if (hasOther) {
                        map.put("precision", Integer.valueOf(e.getPrecision(i)));
                        map.put("isAutoIncrement", Boolean.valueOf(e.isAutoIncrement(i)));
                        map.put("is_nullable", Integer.valueOf(e.isNullable(i)));
                        map.put("isReadOnly", Boolean.valueOf(e.isReadOnly(i)));
                    }
                    rows.add(map);
                }
            }
        } catch (Exception e) {
            log.error("queryForColumns查询所有字段信息失败: {}", e.getMessage());
        } finally {
            dbClose(conn, pstmt, rs);
        }

        return rows;
    }

    public static int executeQueryForCount(String sql) {
        int rowCount = 0;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            hasConnect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            rs.last();
            rowCount = rs.getRow();
        } catch (Exception e) {
            log.info("根据SQL查询数量失败，{}，{}", "executeQueryForCount", e.getMessage());
        } finally {
            dbClose(conn, stmt, rs);
        }

        return rowCount;
    }

    public static boolean executeQuery(String sql) {
        boolean bl = false;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            hasConnect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                bl = true;
            }
        } catch (Exception e) {
            log.info("根据SQL查询失败，{}，{}", "executeQuery", e.getMessage());
        } finally {
            dbClose(conn, stmt, rs);
        }

        return bl;
    }

    public static boolean testConn() {
        boolean bl = false;
        Connection conn = getConnection();
        if (conn != null) {
            bl = true;
        }
        dbClose(conn);

        return bl;
    }

    public static String getPrimaryKey(String databaseName, String tableName) {
        List<String> primaryKeys = getPrimaryKeys(databaseName, tableName);
        if (primaryKeys.size() > 0) {
            return primaryKeys.get(0);
        }
        return "";
    }

    public static List<String> getPrimaryKeys(String databaseName, String tableName) {
        List<String> rows = new ArrayList<String>();
        ResultSet rs = null;
        try {
            hasConnect();
            DatabaseMetaData e = conn.getMetaData();
            rs = e.getPrimaryKeys(databaseName, null, tableName);
            while (rs.next()) {
                log.info("主键名称: {}", rs.getString(4));
                rows.add(rs.getString(4));
            }
        } catch (Exception e) {
            log.info("获取数据库中表的主键失败，{}，{}", "getPrimaryKeys", e.getMessage());
        } finally {
            dbClose(conn);
        }

        return rows;
    }

    public static int executeQueryForCountForOracle(String sql) {
        return executeQueryForCountForSQL(sql);
    }

    public static int executeQueryForCountForPostgreSQL(String sql) {
        return executeQueryForCountForSQL(sql);
    }

    public static int executeQueryForCountForSQL(String sql) {
        int rowCount = 0;
        Statement stmt = null;
        ResultSet rs = null;
        String conSql = " select count(*) as count from  (" + sql + ") t ";
        try {
            hasConnect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(conSql);
            if (rs != null && rs.next()) {
                rowCount = rs.getInt("count");
            }
        } catch (Exception e) {
            log.error("根据SQL查询数据数量失败: {}", e.getMessage());
        } finally {
            dbClose(conn, stmt, rs);
        }

        return rowCount;
    }

    private static void hasConnect() {
        if (conn == null) {
            conn = getConnection();
        }
    }

    private static void dbClose(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            dbClose(conn, stmt);
        } catch (SQLException e) {
            log.error("关闭数据库连接失败");
        }
    }

    private static void dbClose(Connection conn, Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
            dbClose(conn);
        } catch (SQLException e) {
            log.error("关闭数据库连接失败");
        }
    }

    private static void dbClose(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("关闭Connection连接失败");
            }
        }
    }

    public static final synchronized Connection getConnection() {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, userName, passwd);
        } catch (Exception e) {
            log.error("DBUtil创建连接失败: {}", e.getMessage());
            return null;
        }
    }
}
