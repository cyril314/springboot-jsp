package com.fit.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fit.common.base.BaseController;
import com.fit.common.base.Page;
import com.fit.common.utils.FileUtil;
import com.fit.common.Constants;
import com.fit.common.utils.StringUtil;
import com.fit.entity.Config;
import com.fit.entity.IdsDto;
import com.fit.entity.NotSqlEntity;
import com.fit.entity.TempDto;
import com.fit.service.PermissionService;
import com.fit.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"system/permission"})
public class PermissionController extends BaseController {

    @Autowired
    private PermissionService permissionService;

    @RequestMapping({"i/allDatabaseListForNoSQL/{id}"})
    @ResponseBody
    public List<Map<String, Object>> allDatabaseListForNoSQL(@PathVariable("id") String databaseConfigId) throws Exception {
        Object listDb = new ArrayList();
        Map map = this.permissionService.getConfig(databaseConfigId);
        Constants.DATABASE_TYPE = (String) map.get("databaseType");

        try {
            if (Constants.DATABASE_TYPE.equals("Memcache")) {
                listDb = this.permissionService.getAllDataBaseForMemcached(databaseConfigId);
            }

            if (Constants.DATABASE_TYPE.equals("Redis")) {
                listDb = this.permissionService.getAllDataBaseForReids(databaseConfigId);
            }

            return (List) listDb;
        } catch (Exception var5) {
            return null;
        }
    }

    public Map<String, Object> executeSqlNotRes(String sql, String dbName) {
        String mess = "";
        String status = "";
        Date b1 = new Date();
        int i = 0;

        try {
            i = this.permissionService.executeSqlNotRes(sql, dbName);
            mess = "执行成功！";
            status = "success";
        } catch (Exception var11) {
            mess = var11.getMessage();
            status = "fail";
        }

        Date b2 = new Date();
        long y = b2.getTime() - b1.getTime();
        HashMap map = new HashMap();
        map.put("totalCount", Integer.valueOf(i));
        map.put("time", Long.valueOf(y));
        map.put("mess", mess);
        map.put("status", status);
        return map;
    }

    @RequestMapping(
            value = {"i/saveSearchHistory"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String saveSearchHistory(HttpServletRequest request) {
        String id = request.getParameter("id");
        String sql = request.getParameter("sql");
        String dbName = request.getParameter("dbName");
        String name = request.getParameter("name");
        sql = sql.replaceAll("\'", "\'\'");
        boolean bool = true;
        if (id != null && !"".equals(id)) {
            bool = this.permissionService.updateSearchHistory(id, name, sql, dbName);
        } else {
            bool = this.permissionService.saveSearchHistory(name, sql, dbName);
        }

        return bool ? "success" : "fail";
    }

    @RequestMapping(
            value = {"i/deleteSearchHistory"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String deleteSearchHistory(@RequestBody TempDto tem, HttpServletRequest request) {
        String id = tem.getId();
        boolean bool = true;
        if (id != null || !"".equals(id)) {
            bool = this.permissionService.deleteSearchHistory(id);
        }

        return bool ? "success" : "fail";
    }

    @RequestMapping(
            value = {"i/selectSearchHistory"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public List<Map<String, Object>> selectSearchHistory() {
        List list = this.permissionService.selectSearchHistory();
        ArrayList list2 = new ArrayList();
        Iterator var4 = list.iterator();

        while (var4.hasNext()) {
            Map map = (Map) var4.next();
            String tempName = (String) map.get("name");
            map.put("name", tempName);
            map.put("pid", "0");
            map.put("icon", "icon-hamburg-zoom");
            list2.add(map);
        }

        return list2;
    }

    @RequestMapping(
            value = {"i/config"},
            method = {RequestMethod.GET}
    )
    public String config(Model model) {
        return "system/configList";
    }

    @RequestMapping(
            value = {"i/addConfigForm"},
            method = {RequestMethod.GET}
    )
    public String addConfigForm(Model model) {
        return "system/configForm";
    }

    @RequestMapping(
            value = {"i/editConfigForm/{id}"},
            method = {RequestMethod.GET}
    )
    public String editConfigForm(@PathVariable("id") String id, Model model) {
        Object map = new HashMap();

        try {
            map = this.permissionService.getConfig(id);
        } catch (Exception var5) {
            ;
        }

        model.addAttribute("config", map);
        return "system/configForm";
    }

    @RequestMapping(
            value = {"i/configUpdate"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> configUpdate(@ModelAttribute @RequestBody Config config, Model model) {
        String databaseType = config.getDatabaseType();
        String ip = config.getIp();
        String port = config.getPort();
        String dbName = config.getDatabaseName();
        String url = "";
        if (databaseType.equals("MySql")) {
            url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName;
        }

        if (databaseType.equals("Oracle")) {
            url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + dbName;
        }

        if (databaseType.equals("PostgreSQL")) {
            url = "jdbc:postgresql://" + ip + ":" + port + "/" + dbName;
        }

        if (databaseType.equals("MSSQL")) {
            url = "jdbc:sqlserver://" + ip + ":" + port + ";database=" + dbName;
        }

        String mess = "";
        String status = "";
        config.setUrl(url);

        try {
            this.permissionService.configUpdate(config);
            mess = "修改成功";
            status = "success";
        } catch (Exception var11) {
            mess = var11.getMessage();
            status = "fail";
        }

        HashMap map = new HashMap();
        map.put("mess", mess);
        map.put("status", status);
        return map;
    }

    @RequestMapping(
            value = {"i/changePass"},
            method = {RequestMethod.GET}
    )
    public String changePass(Model model) {
        return "system/changePass";
    }

    @RequestMapping(
            value = {"i/searchHistory"},
            method = {RequestMethod.GET}
    )
    public String searchHistory(Model model) {
        return "system/searchHistory";
    }

    @RequestMapping(
            value = {"i/changePassUpdate"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> changePassUpdate(HttpServletRequest request) {
        String mess = "";
        String status = "";
        HttpSession session = request.getSession(true);
        String UserByName = (String) session.getAttribute("LOGIN_USER_NAME");
        String oldPass = request.getParameter("oldPass");
        String newPass = request.getParameter("newPass");
        List list = this.permissionService.selectUserByName(UserByName);
        String oldPass2 = "";
        String userId = "";
        if (list.size() > 0) {
            Map map = (Map) list.get(0);
            oldPass2 = (String) map.get("password");
            userId = "" + map.get("id");
        } else {
            mess = "error";
            status = "fail";
        }

        oldPass = StringUtil.MD5(oldPass + "treesoft");
        if (!oldPass.equals(oldPass2)) {
            mess = "旧密码不符！";
            status = "fail";
        } else {
            try {
                this.permissionService.changePassUpdate(userId, StringUtil.MD5(newPass + "treesoft"));
                mess = "修改密码成功";
                status = "success";
            } catch (Exception var12) {
                mess = var12.getMessage();
                status = "fail";
            }
        }

        HashMap map1 = new HashMap();
        map1.put("mess", mess);
        map1.put("status", status);
        return map1;
    }

    @RequestMapping(
            value = {"i/addRow/{tableName}/{databaseName}"},
            method = {RequestMethod.GET}
    )
    public String addRows(@PathVariable("tableName") String tableName, @PathVariable("databaseName") String databaseName, HttpServletRequest request) throws Exception {
        List listAllColumn = this.permissionService.getTableColumns(databaseName, tableName);
        ArrayList listAllColumn2 = new ArrayList();

        for (int i = 0; i < listAllColumn.size(); ++i) {
            Map map2 = (Map) listAllColumn.get(i);
            boolean isAutoIncrement = ((Boolean) map2.get("isAutoIncrement")).booleanValue();
            if (!isAutoIncrement) {
                listAllColumn2.add(map2);
            }
        }

        request.setAttribute("databaseName", databaseName);
        request.setAttribute("tableName", tableName);
        request.setAttribute("listAllColumn", listAllColumn2);
        return "system/addRowOne";
    }

    @RequestMapping(
            value = {"i/saveRows"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> saveRows(HttpServletRequest request) {
        String databaseName = request.getParameter("databaseName");
        String tableName = request.getParameter("tableName");
        HashMap mapResult = new HashMap();
        HashMap maps = new HashMap();
        Map map = request.getParameterMap();
        Set set = map.entrySet();
        Iterator it = set.iterator();
        String column = "";
        String value = "";
        String mess = "";
        String status = "";

        while (it.hasNext()) {
            Entry e = (Entry) it.next();
            column = (String) e.getKey();
            String[] var17;
            int var16 = (var17 = (String[]) e.getValue()).length;

            for (int var15 = 0; var15 < var16; ++var15) {
                String i = var17[var15];
                value = i.replaceAll("\'", "\'\'");
            }

            maps.put(column, value);
        }

        maps.remove("databaseName");
        maps.remove("tableName");

        try {
            this.permissionService.saveRows(maps, databaseName, tableName);
            mess = "新增成功！";
            status = "success";
        } catch (Exception var18) {
            mess = var18.getMessage();
            status = "fail";
        }

        mapResult.put("mess", mess);
        mapResult.put("status", status);
        return mapResult;
    }

    @RequestMapping(
            value = {"i/editRows/{tableName}/{databaseName}/{id}/{idValues}"},
            method = {RequestMethod.GET}
    )
    public String editRows(@PathVariable("tableName") String tableName, @PathVariable("databaseName") String databaseName, @PathVariable("id") String id, @PathVariable("idValues") String idValues, HttpServletRequest request) {
        List listAllColumn = this.permissionService.getOneRowById(databaseName, tableName, id, idValues);
        ArrayList newList = new ArrayList();

        for (int i = 0; i < listAllColumn.size(); ++i) {
            Map map3 = (Map) listAllColumn.get(i);
            String data_type = (String) map3.get("data_type");
            if (data_type.equals("VARCHAR")) {
                String column_value = (String) map3.get("column_value");
                column_value = htmlEscape(column_value);
                map3.put("column_value", column_value);
            }

            newList.add(map3);
        }

        request.setAttribute("databaseName", databaseName);
        request.setAttribute("tableName", tableName);
        request.setAttribute("listAllColumn", newList);
        request.setAttribute("id", id);
        request.setAttribute("idValues", idValues);
        return "system/editRowOne";
    }

    @RequestMapping(
            value = {"i/updateRows"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> updateRows(HttpServletRequest request) {
        String mess = "";
        String status = "";
        HashMap mapResult = new HashMap();
        String databaseName = request.getParameter("databaseName");
        String tableName = request.getParameter("tableName");
        String id = request.getParameter("id");
        String idValues = request.getParameter("idValues");
        HashMap maps = new HashMap();
        Map map = request.getParameterMap();
        Set set = map.entrySet();
        Iterator it = set.iterator();
        String column = "";
        String value = "";

        while (it.hasNext()) {
            Entry e = (Entry) it.next();
            column = (String) e.getKey();
            String[] var19;
            int var18 = (var19 = (String[]) e.getValue()).length;

            for (int var17 = 0; var17 < var18; ++var17) {
                String i = var19[var17];
                value = i;
            }

            value = value.replaceAll("\'", "\'\'");
            maps.put(column, value);
        }

        maps.remove("databaseName");
        maps.remove("tableName");
        maps.remove("id");
        maps.remove("idValues");

        try {
            this.permissionService.updateRows(maps, databaseName, tableName, id, idValues);
            mess = " 更新成功！";
            status = "success";
        } catch (Exception var20) {
            mess = var20.getMessage();
            status = "fail";
        }

        mapResult.put("mess", mess);
        mapResult.put("status", status);
        return mapResult;
    }

    public static String htmlEscape(String strData) {
        if (strData == null) {
            return "";
        } else {
            strData = replaceString(strData, "&", "&amp;");
            strData = replaceString(strData, "<", "&lt;");
            strData = replaceString(strData, ">", "&gt;");
            strData = replaceString(strData, "\'", "&apos;");
            strData = replaceString(strData, "\"", "&quot;");
            return strData;
        }
    }

    public static String replaceString(String strData, String regex, String replacement) {
        if (strData == null) {
            return null;
        } else {
            int index = strData.indexOf(regex);
            String strNew = "";
            if (index < 0) {
                return strData;
            } else {
                while (index >= 0) {
                    strNew = strNew + strData.substring(0, index) + replacement;
                    strData = strData.substring(index + regex.length());
                    index = strData.indexOf(regex);
                }

                strNew = strNew + strData;
                return strNew;
            }
        }
    }

    @RequestMapping(
            value = {"i/contribute"},
            method = {RequestMethod.GET}
    )
    public String contribute(HttpServletRequest request) {
        return "system/contribute";
    }

    @RequestMapping(
            value = {"i/help"},
            method = {RequestMethod.GET}
    )
    public String help(HttpServletRequest request) {
        return "system/help";
    }

    @RequestMapping(
            value = {"i/testConn"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> testConn(@RequestBody Config config) {
        HashMap mapResult = new HashMap();
        String databaseType = config.getDatabaseType();
        String databaseName = config.getDatabaseName();
        String ip = config.getIp();
        String port = config.getPort();
        String user = config.getUserName();
        String pass = config.getPassword();
        String mess = "";
        String status = "";

        boolean bl;
        try {
            bl = this.permissionService.testConn(databaseType, databaseName, ip, port, user, pass);
        } catch (Exception var13) {
            bl = false;
        }

        if (bl) {
            mess = "连接成功！";
            status = "success";
        } else {
            mess = "连接失败！";
            status = "fail";
        }

        mapResult.put("mess", mess);
        mapResult.put("status", status);
        return mapResult;
    }

    @RequestMapping(
            value = {"i/showTableData/{tableName}/{databaseName}"},
            method = {RequestMethod.GET}
    )
    public String showTableData(@PathVariable("tableName") String tableName, @PathVariable("databaseName") String databaseName, HttpServletRequest request) {
        request.setAttribute("databaseName", databaseName);
        request.setAttribute("tableName", tableName);
        return "system/showTableData";
    }

    @RequestMapping(
            value = {"i/showResult/{sqlIndex}"},
            method = {RequestMethod.GET}
    )
    public String showResult(@PathVariable("sqlIndex") String sqlIndex, HttpServletRequest request) {
        request.setAttribute("sqlIndex", sqlIndex);
        return "system/showResult";
    }

    @RequestMapping(
            value = {"i/selectSqlStudy"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public List<Map<String, Object>> selectSqlStudy(HttpServletRequest request) throws Exception {
        new ArrayList();
        List list = this.permissionService.selectSqlStudy();
        return list;
    }

    @RequestMapping(
            value = {"i/updateRow/{tableName}/{databaseName}"},
            method = {RequestMethod.GET}
    )
    public Map<String, Object> updateRow(@PathVariable("tableName") String tableName, @PathVariable("databaseName") String databaseName, HttpServletRequest request) {
        HashMap mapResult = new HashMap();
        String mess = "";
        String status = "";
        mess = "update成功！";
        status = "success";
        mapResult.put("mess", mess);
        mapResult.put("status", status);
        return mapResult;
    }

    @RequestMapping(
            value = {"i/jsonFormat"},
            method = {RequestMethod.GET}
    )
    public String jsonFormat(HttpServletRequest request) throws Exception {
        return "system/jsonFormat";
    }

    @RequestMapping(
            value = {"i/backupDatabase/{databaseName}"},
            method = {RequestMethod.GET}
    )
    public String backupDatabase(@PathVariable("databaseName") String databaseName, HttpServletRequest request) throws Exception {
        request.setAttribute("databaseName", databaseName);
        return "system/backupDatabase";
    }

    @RequestMapping(
            value = {"i/infoData/{databaseConfigId}"},
            method = {RequestMethod.GET}
    )
    public String infoData(@PathVariable("databaseConfigId") String databaseConfigId, HttpServletRequest request) throws Exception {
        request.setAttribute("databaseConfigId", databaseConfigId);
        Map map = this.permissionService.getConfig(databaseConfigId);
        String ip = (String) map.get("ip");
        String port = (String) map.get("port");
        String databaseName = ip + ":" + port;
        request.setAttribute("databaseName", databaseName);
        return "system/infoData";
    }

    @RequestMapping(
            value = {"i/showNoSQLDBData/{NoSQLDbName}/{databaseConfigId}"},
            method = {RequestMethod.GET}
    )
    public String showNoSQLDBData(@PathVariable("NoSQLDbName") String NoSQLDbName, @PathVariable("databaseConfigId") String databaseConfigId, HttpServletRequest request) {
        request.setAttribute("databaseConfigId", databaseConfigId);
        request.setAttribute("NoSQLDbName", NoSQLDbName);
        return "system/showNoSQLDBData";
    }

    @RequestMapping(
            value = {"i/showNoSQLDBValue/{NoSQLDbName}/{databaseConfigId}/{selectKey}/{selectValue}"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public Map<String, Object> showNoSQLDBValue(@PathVariable("NoSQLDbName") String NoSQLDbName, @PathVariable("databaseConfigId") String databaseConfigId, @PathVariable("selectKey") String selectKey, @PathVariable("selectValue") String selectValue, HttpServletRequest request) {
        Page page = this.getPage(request);

        try {
            if (Constants.DATABASE_TYPE.equals("Redis")) {
                page = this.permissionService.getNoSQLDBForRedis(page, NoSQLDbName, databaseConfigId, selectKey, selectValue);
            }

            if (Constants.DATABASE_TYPE.equals("Memcache")) {
                page = this.permissionService.getNoSQLDBForMemcached(page, NoSQLDbName, databaseConfigId, selectKey, selectValue);
            }
        } catch (Exception var8) {
            return this.getEasyUIData(page);
        }

        return this.getEasyUIData(page);
    }

    @RequestMapping({"i/getConfigAllDataBase"})
    @ResponseBody
    public List<Map<String, Object>> getConfigAllDataBase() throws Exception {
        new ArrayList();

        try {
            List listDb = this.permissionService.getConfigAllDataBase();
            return listDb;
        } catch (Exception var3) {
            return null;
        }
    }

    @RequestMapping(
            value = {"i/deleteNoSQLKeys"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> deleteNoSQLKeys(@RequestBody IdsDto tem) {
        String databaseName = tem.getDatabaseName();
        String NoSQLDbName = tem.getNoSQLDbName();
        String databaseConfigId = tem.getDatabaseConfigId();
        String[] ids = tem.getIds();
        byte i = 0;
        String mess = "";
        String status = "";

        try {
            if (Constants.DATABASE_TYPE.equals("Redis")) {
                this.permissionService.deleteNoSQLKeyForRedis(databaseConfigId, NoSQLDbName, ids);
            }

            if (Constants.DATABASE_TYPE.equals("Memcache")) {
                this.permissionService.deleteNoSQLKeyForMemcached(databaseConfigId, NoSQLDbName, ids);
            }

            mess = "删除成功";
            status = "success";
        } catch (Exception var10) {
            mess = var10.getMessage();
            status = "fail";
        }

        HashMap map = new HashMap();
        map.put("totalCount", Integer.valueOf(i));
        map.put("mess", mess);
        map.put("status", status);
        return map;
    }

    @RequestMapping(
            value = {"i/addOrEditNotSql/{NoSQLDbName}/{databaseConfigId}"},
            method = {RequestMethod.GET}
    )
    public String addOrEditNotSql(@PathVariable("NoSQLDbName") String NoSQLDbName, @PathVariable("databaseConfigId") String databaseConfigId, HttpServletRequest request) {
        request.setAttribute("NoSQLDbName", NoSQLDbName);
        request.setAttribute("databaseConfigId", databaseConfigId);
        return "system/addNoSQLDBData";
    }

    @RequestMapping(
            value = {"i/editNotSqlData/{key}/{NoSQLDbName}/{databaseConfigId}"},
            method = {RequestMethod.GET}
    )
    public String editNotSqlData(@PathVariable("key") String key, @PathVariable("NoSQLDbName") String NoSQLDbName, @PathVariable("databaseConfigId") String databaseConfigId, Model model) {
        NotSqlEntity notSqlEntity = new NotSqlEntity();

        try {
            if (Constants.DATABASE_TYPE.equals("Redis")) {
                notSqlEntity = this.permissionService.selectNotSqlDataForRedis(key, NoSQLDbName, databaseConfigId);
            }

            if (Constants.DATABASE_TYPE.equals("Memcache")) {
                notSqlEntity = this.permissionService.selectNotSqlDataForMemcached(key, NoSQLDbName, databaseConfigId);
            }
        } catch (Exception var7) {
            System.out.println(var7.getMessage());
        }

        model.addAttribute("notSqlEntity", notSqlEntity);
        return "system/addNoSQLDBData";
    }

    @RequestMapping(
            value = {"i/saveNotSqlData/{NoSQLDbName}/{databaseConfigId}"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> saveNotSqlData(@ModelAttribute @RequestBody NotSqlEntity notSqlEntity, Model model, @PathVariable("NoSQLDbName") String NoSQLDbName, @PathVariable("databaseConfigId") String databaseConfigId) {
        String mess = "";
        String status = "";

        try {
            if ("".equals(notSqlEntity.getExTime())) {
                notSqlEntity.setExTime("0");
            }

            if (Constants.DATABASE_TYPE.equals("Redis")) {
                this.permissionService.saveNotSqlDataForRedis(notSqlEntity, databaseConfigId, NoSQLDbName);
            }

            if (Constants.DATABASE_TYPE.equals("Memcache")) {
                this.permissionService.saveNotSqlDataForMemcached(notSqlEntity, databaseConfigId);
            }

            mess = "保存成功";
            status = "success";
        } catch (Exception var8) {
            mess = var8.getMessage();
            status = "fail";
        }

        HashMap map = new HashMap();
        map.put("mess", mess);
        map.put("status", status);
        return map;
    }

    @RequestMapping(
            value = {"i/selectNoSQLDBStatus/{databaseConfigId}"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public Map<String, Object> selectNoSQLDBStatus(@PathVariable("databaseConfigId") String databaseConfigId, HttpServletRequest request) {
        Page page = this.getPage(request);
        HashMap map = new HashMap();
        String mess = "";
        String status = "";

        try {
            if (Constants.DATABASE_TYPE.equals("Redis")) {
                page = this.permissionService.selectNoSQLDBStatusForRedis(page, databaseConfigId);
            }

            if (Constants.DATABASE_TYPE.equals("Memcache")) {
                page = this.permissionService.selectNoSQLDBStatusForMemcached(page, databaseConfigId);
            }

            map.put("rows", page.getResult());
            map.put("total", Long.valueOf(page.getTotalCount()));
            map.put("columns", page.getColumns());
            map.put("primaryKey", page.getPrimaryKey());
            mess = "查询成功";
            status = "success";
        } catch (Exception var8) {
            System.out.println(" 系统当前状态参数查询失败,可能参数配置有误！ " + var8.getMessage());
            mess = var8.getMessage();
            status = "fail";
        }

        map.put("mess", mess);
        map.put("status", status);
        return map;
    }

    @RequestMapping(
            value = {"i/showSQLMess"},
            method = {RequestMethod.GET}
    )
    public String showSQLMess(HttpServletRequest request) {
        return "system/showSQLMess";
    }

    @RequestMapping(
            value = {"i/backupDatabaseData"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public Map<String, Object> backupDatabaseData(HttpServletRequest request) throws Exception {
        HashMap map = new HashMap();
        new ArrayList();
        String path = request.getSession().getServletContext().getRealPath("/") + File.separator + "backup";
        List list = this.permissionService.selectBackupList(path);
        map.put("rows", list);
        map.put("total", Integer.valueOf(list.size()));
        return map;
    }

    @RequestMapping(
            value = {"i/configList"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public Map<String, Object> configList(HttpServletRequest request) throws Exception {
        Page page = this.getPage(request);

        try {
            page = this.permissionService.configList(page);
        } catch (Exception var4) {
            return this.getEasyUIData(page);
        }

        return this.getEasyUIData(page);
    }

    @RequestMapping(
            value = {"i/backupNotSqlDatabase"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> backupNotSqlDatabase(@RequestBody IdsDto tem, HttpServletRequest request) {
        String databaseConfigId = tem.getDatabaseConfigId();
        HashMap mapResult = new HashMap();
        String mess = "";
        String status = "";
        String path = request.getSession().getServletContext().getRealPath("/");

        try {
            if (Constants.DATABASE_TYPE.equals("Redis")) {
                this.permissionService.backupNotSqlDatabaseForRedis(databaseConfigId, path);
                Thread.sleep(3000L);
                mess = "备份完成！";
                status = "success";
            }

            if (Constants.DATABASE_TYPE.equals("Memcache")) {
                Thread.sleep(3000L);
                mess = "不支持Memcache备份！";
                status = "success";
            }
        } catch (FileNotFoundException var9) {
            mess = "备份数据库失败,需与redis安装在同一台电脑！";
            status = "fail";
        } catch (Exception var10) {
            System.out.println(var10.getMessage());
            mess = "备份数据库失败！";
            status = "fail";
        }

        mapResult.put("mess", mess);
        mapResult.put("status", status);
        return mapResult;
    }

    @RequestMapping(
            value = {"i/deleteBackupFile"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> deleteBackupFile(@RequestBody IdsDto tem, HttpServletRequest request) {
        String[] ids = tem.getIds();
        String mess = "";
        String status = "";
        String path = request.getSession().getServletContext().getRealPath("/") + "backup\\";

        try {
            this.permissionService.deleteBackupFile(ids, path);
            mess = "删除成功";
            status = "success";
        } catch (Exception var8) {
            mess = var8.getMessage();
            status = "fail";
        }

        HashMap map = new HashMap();
        map.put("mess", mess);
        map.put("status", status);
        return map;
    }

    @RequestMapping(
            value = {"i/backupFileDownload/{fileName:.+}"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public void backupFileDownload(@PathVariable("fileName") String fileName, HttpServletRequest request, HttpServletResponse response) {
        String path = request.getSession().getServletContext().getRealPath("/") + "backup\\";
        File file = new File(path + fileName);
        response.setContentType("multipart/form-data");
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        response.addHeader("Content-Length", "" + file.length());

        try {
            FileInputStream e = new FileInputStream(file);
            ServletOutputStream out = response.getOutputStream();
            boolean b = false;
            byte[] buffer = new byte[1024];
            boolean len = false;

            int len1;
            while ((len1 = e.read(buffer)) > 0) {
                out.write(buffer, 0, len1);
            }

            e.close();
            out.close();
            out.flush();
        } catch (Exception var11) {
            var11.printStackTrace();
        }

    }

    @RequestMapping(
            value = {"i/restoreDBForRedis"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> restoreDBForRedis(@RequestBody IdsDto tem, HttpServletRequest request) {
        String path = request.getSession().getServletContext().getRealPath("/") + "backup\\";
        HashMap map = new HashMap();
        String mess = "";
        String status = "";
        String fileName = "";
        String databaseConfigId = tem.getDatabaseConfigId();
        String[] ids = tem.getIds();
        if (Constants.DATABASE_TYPE.equals("Memcache")) {
            mess = "不支持Memcache还原操作！";
            status = "success";
            map.put("mess", mess);
            map.put("status", status);
            return map;
        } else {
            try {
                fileName = ids[0];
                File e = new File(path + fileName);
                Map map3 = this.permissionService.getConfig(databaseConfigId);
                String redisDir = RedisUtil.getConfig(map3, "dir");
                File tarFile = new File(redisDir + "\\" + "dump.rdb");
                if (tarFile.exists()) {
                    tarFile.delete();
                }

                FileUtil.copyFile(e, tarFile);
                mess = "数据库还原成功,请重新启动Redis!";
                status = "success";
            } catch (Exception var14) {
                System.out.println(var14.getMessage());
                mess = "数据库还原出错!";
                status = "fail";
            }

            map.put("mess", mess);
            map.put("status", status);
            return map;
        }
    }

    @RequestMapping({"i/queryInfoItem/{databaseConfigId}"})
    @ResponseBody
    public Map<String, Object> queryInfoItem(@PathVariable("databaseConfigId") String databaseConfigId) {
        Object map = new HashMap();
        String mess = "";
        String status = "";

        try {
            if (Constants.DATABASE_TYPE.equals("Redis")) {
                map = this.permissionService.queryInfoItemForRedis(databaseConfigId);
            }

            if (Constants.DATABASE_TYPE.equals("Memcache")) {
                map = this.permissionService.queryInfoItemForMemcached(databaseConfigId);
            }

            mess = "查询成功";
            status = "success";
        } catch (Exception var6) {
            mess = var6.getMessage();
            status = "fail";
            return null;
        }

        ((Map) map).put("mess", mess);
        ((Map) map).put("status", status);
        return (Map) map;
    }

    @RequestMapping({"i/getMemoryConsumption/{databaseConfigId}"})
    @ResponseBody
    public Map<String, Object> getMemoryConsumption(@PathVariable("databaseConfigId") String databaseConfigId) {
        new ArrayList();
        Object map = new HashMap();
        String mess = "";
        String status = "";

        try {
            if (Constants.DATABASE_TYPE.equals("Redis")) {
                this.permissionService.getMemoryConsumptionForRedis(databaseConfigId);
            }

            if (Constants.DATABASE_TYPE.equals("Memcache")) {
                map = this.permissionService.queryInfoItemForMemcached(databaseConfigId);
            }

            mess = "查询成功";
            status = "success";
        } catch (Exception var7) {
            mess = var7.getMessage();
            status = "fail";
            return null;
        }

        ((Map) map).put("mess", mess);
        ((Map) map).put("status", status);
        return (Map) map;
    }

    @RequestMapping(
            value = {"i/deleteConfig"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map<String, Object> deleteConfig(@RequestBody IdsDto tem, HttpServletRequest request) {
        String[] ids = tem.getIds();
        String mess = "";
        String status = "";

        try {
            this.permissionService.deleteConfig(ids);
            mess = "删除成功";
            status = "success";
        } catch (Exception var7) {
            mess = var7.getMessage();
            status = "fail";
        }

        HashMap map = new HashMap();
        map.put("mess", mess);
        map.put("status", status);
        return map;
    }
}
