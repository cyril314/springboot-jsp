package com.fit.common.utils;

import com.fit.common.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class SqliteUtil {

    private static String FILE_PATH = System.getProperty("user.dir") + File.separator + "TreeNms.db";
    private static Connection conn = null;

    public static boolean do_update(String sql) {
        Statement stat = null;
        try {
            hasConnect();
            stat = conn.createStatement();
            stat.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            log.error("无法找到配置文件，软件安装路径不能包含中文,空隔！");
        } finally {
            dbClose(stat);
        }
        return false;
    }

    public static List<Map<String, Object>> executeSqliteQuery(String sql) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            hasConnect();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= columnCount; ++i) {
                    Object o = rs.getObject(i);
                    if ("Date".equalsIgnoreCase(rsmd.getColumnTypeName(i)) && o != null) {
                        map.put(rsmd.getColumnName(i), formatter.format(o));
                    } else {
                        map.put(rsmd.getColumnName(i), o == null ? "" : o);
                    }
                }

                rows.add(map);
            }
        } catch (Exception e) {
            log.error("executeSqliteQuery-查询所有字段信息失败: {}", e.getMessage());
        } finally {
            dbClose(pstmt, rs);
        }
        return rows;
    }

    public static int insertData(String sql) {
        Statement stmt = null;
        int stats;
        try {
            hasConnect();
            stmt = conn.createStatement();
            stats = Integer.valueOf(stmt.executeUpdate(sql));
        } catch (SQLException e) {
            log.info("执行插入数据失败，{}，{}", "insertData", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            dbClose(stmt);
        }

        return stats;
    }

    public static boolean initDbConfig() {
        boolean bl = true;
        List<Map<String, Object>> list = SqliteUtil.executeSqliteQuery(" select * from  treesoft_config ");
        Map<String, Object> map = list.get(0);
        Constants.DATABASE_TYPE = (String) map.get("databaseType");
        Constants.DRIVER = (String) map.get("driver");
        Constants.URL = (String) map.get("url");
        Constants.DATABASE_NAME = (String) map.get("databaseName");
        Constants.USER_NAME = (String) map.get("userName");
        Constants.PASS_WROD = (String) map.get("passwrod");
        Constants.PORT = (String) map.get("port");
        Constants.IP = (String) map.get("ip");
        return bl;
    }

    public List<Map<String, Object>> getConfigList() {
        return SqliteUtil.executeSqliteQuery(" select * from  treesoft_config ");
    }

    private static void hasConnect() {
        if (conn == null) {
            conn = getConnection();
        } else {
        }
    }

    private static void dbClose(Statement stmt, ResultSet rs) {
        dbClose(rs);
        dbClose(stmt);
    }

    private static void dbClose(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error("Sqlite关闭ResultSet失败");
        }
    }

    private static void dbClose(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            log.error("Sqlite关闭Statement失败");
        }
    }

    private static void dbClose(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Sqlite关闭Connection连接失败");
            }
        }
    }

    public static final synchronized Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + FILE_PATH);
        } catch (Exception e) {
            log.error("SqliteUtil创建连接失败: {}", e.getMessage());
            return null;
        }
    }
}
