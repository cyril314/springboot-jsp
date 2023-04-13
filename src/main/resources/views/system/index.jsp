<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title>TreeNms数据库管理系统</title>
    <meta name="Keywords" content="TreeNms数据库管理系统">
    <meta name="Description" content="TreeNms数据库管理系统">
    <%@ include file="/views/include/easyui.jsp" %>
    <%@ include file="/views/include/codemirror.jsp" %>
    <script src="${ctx}/assets/plugins/My97DatePicker/WdatePicker.js" type="text/javascript"></script>

    <!--导入首页启动时需要的相应资源文件(首页相应功能的 js 库、css样式以及渲染首页界面的 js 文件)-->
    <script src="${ctx}/assets/plugins/easyui/common/index.js" type="text/javascript"></script>
    <script src="${ctx}/assets/plugins/easyui/common/indexSearch.js" type="text/javascript"></script>
    <link href="${ctx}/assets/plugins/easyui/common/index.css" rel="stylesheet"/>
    <script src="${ctx}/assets/plugins/easyui/common/index-startup.js"></script>

    <link type="text/css" rel="stylesheet" href="${ctx}/assets/css/eclipse.css">
    <link type="text/css" rel="stylesheet" href="${ctx}/assets/css/codemirror.css"/>
    <link type="text/css" rel="stylesheet" href="${ctx}/assets/css/show-hint.css"/>
    <link rel="icon" href="${ctx}/favicon.ico" mce_href="${ctx}/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="${ctx}/favicon.ico" mce_href="${ctx}/favicon.ico" type="image/x-icon">
    <script type="text/javascript" src="${ctx}/assets/js/codemirror.js"></script>

    <script type="text/javascript" src="${ctx}/assets/js/sql.js"></script>
    <script type="text/javascript" src="${ctx}/assets/js/show-hint.js"></script>
    <script type="text/javascript" src="${ctx}/assets/js/sql-hint.js"></script>
    <style>
        .CodeMirror {
            border: 1px solid #cccccc;
            height: 98%;
        }
    </style>
</head>
<body>
<!-- 容器遮罩 -->
<div id="maskContainer">
    <div class="datagrid-mask" style="display: block;"></div>
    <div class="datagrid-mask-msg" style="display: block; left: 50%; margin-left: -52.5px;">
        正在加载...
    </div>
</div>

<div id="mainLayout" class="easyui-layout hidden" data-options="fit: true">
    <div id="northPanel" data-options="region: 'north', border: false" style="height: 80px; overflow: hidden;">
        <div id="topbar" class="top-bar" style="width: 100%;height:52px; background: #0092dc url('${ctx}/assets/images/mosaic-pattern.png') repeat;">
            <div class="top-bar-left">
                <h1 style="margin-left: 10px; margin-top: 10px;color: #fff">
                    <img src="${ctx}/assets/img/logo.png">TreeNms数据库管理系统
                    <span style="color:#00824D;font-size:14px; font-weight:bold;">&nbsp;TreeNMS</span>
                    <span style="color: #fff;font-size:12px;">V1.6.4</span>
                </h1>
            </div>

            <div class="top-bar-right" style="width:520px">
                <div id="timerSpan">
                    <div id="operator" style="padding:5px;height:auto">
                        <div style="padding-right:10px;height:auto">
                            <div style="padding-right:20px; display:inline; cursor:pointer;">
                                <img src="${ctx}/assets/img/alarm.gif" onclick="javascript:infoData()" title="实时状态监控"/>
                            </div>
                            <div style="padding-right:20px; display:inline; cursor:pointer;">
                                <img src="${ctx}/assets/img/btn_hd_backup.gif" onclick="javascript:backupDatabase()" title="备份/还原"/>
                            </div>
                            <div style="padding-right:20px; display:inline; cursor:pointer;">
                                <img src="${ctx}/assets/img/btn_json.gif" onclick="javascript:jsonFormat()" title="Json格式化"/>
                            </div>
                            <div style="padding-right:20px; display:inline; cursor:pointer;">
                                <img src="${ctx}/assets/img/btn_hd_support.gif" onclick="javascript:ShowConfigPage()" title="数据库配置"/>
                            </div>
                            <div style="padding-right:20px; display:inline;cursor:pointer;">
                                <img src="${ctx}/assets/img/btn_hd_pass.gif" onclick="javascript:ShowPasswordDialog()" title="修改用户密码"/>
                            </div>
                            <div style="padding-right:20px; display:inline;cursor:pointer;">
                                <img src="${ctx}/assets/img/btn_hd_help.gif" onclick="javascript:help()" title="帮助"/>
                            </div>
                            <div style=" display:inline;cursor:pointer; ">
                                <img id="btnExit" src="${ctx}/assets/img/btn_hd_exit.gif" title="注销"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div id="themeSpan">
                    <a id="btnHideNorth" class="easyui-linkbutton" data-options="plain: true, iconCls: 'layout-button-up'"> </a>
                </div>
            </div>
        </div>

        <div id="toolbar" class="panel-header panel-header-noborder top-toolbar">
            <div id="infobar">
                <span class="icon-hamburg-user" style="padding-left: 25px; background-position: left center;">${username}，您好</span>
            </div>

            <div id="buttonbar">
                <a href="javascript:void(0);" id="btnFullScreen" class="easyui-linkbutton easyui-tooltip" title="全屏切换"
                   data-options="plain: true, iconCls: 'icon-standard-arrow-inout'">全屏切换</a>
                <span>更换皮肤：</span>
                <select id="themeSelector"></select>
                <a id="btnShowNorth" class="easyui-linkbutton" data-options="plain: true, iconCls: 'layout-button-down'" style="display: none;"></a>
            </div>
        </div>
    </div>

    <div data-options="region: 'west', title: '数据库选择', iconCls: 'icon-standard-map', split: true, minWidth: 200, maxWidth: 400"
         style="width: 220px; padding: 1px;">

        <div id="eastLayout" class="easyui-layout" data-options="fit: true">
            <div data-options="region: 'north', split: false, border: false" style="height: 34px;">
                <select class="combobox-f combo-f" style="width:180px;margin:5px; " id="databaseSelect"> </select>
            </div>

            <div data-options="region: 'center', border: false, title: '数据库', iconCls: 'icon-hamburg-database', tools: [{ iconCls: 'icon-hamburg-refresh', handler: function () {  dg.treegrid('reload'); } }]">
                <input id="pid" name="pid"/>
            </div>
        </div>

    </div>

    <div data-options="region: 'center'">
        <div id="mainTabs_tools" class="tabs-tool">
            <table>
                <tr>
                    <td>
                        <a id="mainTabs_jumpHome" class="easyui-linkbutton easyui-tooltip" title="跳转至主页选项卡"
                           data-options="plain: true, iconCls: 'icon-hamburg-home'"></a>
                    </td>
                    <td>
                        <div class="datagrid-btn-separator"></div>
                    </td>
                    <td>
                        <a id="mainTabs_toggleAll" class="easyui-linkbutton easyui-tooltip" title="展开/折叠面板使选项卡最大化"
                           data-options="plain: true, iconCls: 'icon-standard-arrow-out'"></a></td>
                    <td>
                        <div class="datagrid-btn-separator"></div>
                    </td>
                    <td>
                        <a id="mainTabs_refTab" class="easyui-linkbutton easyui-tooltip" title="刷新当前选中的选项卡"
                           data-options="plain: true, iconCls: 'icon-standard-arrow-refresh'"></a></td>
                    <td>
                        <div class="datagrid-btn-separator"></div>
                    </td>
                    <td>
                        <a id="mainTabs_closeTab" class="easyui-linkbutton easyui-tooltip" title="关闭当前选中的选项卡"
                           data-options="plain: true, iconCls: 'icon-standard-application-form-delete'"></a></td>
                </tr>
            </table>
        </div>

        <div id="mainTabs" class="easyui-tabs"
             data-options="fit: true, border: false, showOption: true, enableNewTabMenu: true, tools: '#mainTabs_tools', enableJumpTabMenu: true">
            <div id="homePanel" data-options="title: '主页-系统状态', iconCls: 'icon-hamburg-home'">
                <div id="tb" style="padding:5px;height:auto">
                    <div>
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-standard-bricks" plain="true" onclick="infoData()">实时状态监控</a>
                        <span class="toolbar-item dialog-tool-separator"></span>
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-standard-arrow-refresh" plain="true" onclick="refresh2()">刷新</a>
                        <span class="toolbar-item dialog-tool-separator"></span>
                    </div>
                </div>

                <table id="dg2"></table>
            </div>
        </div>
    </div>

    <div data-options="region: 'east', title: '常用SQL', iconCls: 'icon-standard-book', split: true,collapsed: true, minWidth: 160, maxWidth: 500"
         style="width: 220px;">
        <div id="sqlLayout" class="easyui-layout" data-options="fit: true">
            <div data-options="region: 'north', split: true, border: false" style="height:800px">
                <input id="sqlStudyList"/>
            </div>
            <%--<div id="searchHistoryPanel" data-options="region: 'center', split: true,  border: false, title: '我的键值设置', iconCls: 'icon-standard-book-key', tools: [{ iconCls: 'icon-hamburg-refresh', handler: function () {  searchBG.treegrid('reload'); } }]">
                   <input id="searchHistoryList"   />
            </div>  --%>
        </div>
    </div>

    <div data-options="region: 'south', title: '关于...', iconCls: 'icon-standard-information', collapsed: true, border: false"
         style="height: 70px;">
        <div style="color: #4e5766; padding: 6px 0px 0px 0px; margin: 0px auto; text-align: center; font-size:12px; font-family:微软雅黑;">
            <img src="http://www.treesoft.cn/picture/logo.png" onerror="imgerror(this)"> CopyRight@2016 版权所有
            <a href="http://www.treesoft.cn" target="_blank" style="text-decoration:none;"> www.XXX.cn </a> &nbsp;
        </div>
    </div>
</div>

<div id='tb3' style='padding:5px;height:auto'>
    <div>
        <a href='#' class='easyui-linkbutton' iconCls='icon-add' plain='true'></a>
        <a href='#' class='easyui-linkbutton' iconCls='icon-edit' plain='true'></a>
    </div>
</div>

<div id="dlgg"></div>
<div id="addRow"></div>
<input type="hidden" id="currentTableName">

<script>
    var dg, dg2, d, pwd, tableName, NoSQLDbName, type, rowtype, searchBG, databaseName, add, primary_key, saveSearch;
    var colums = "";
    var selectRowCount = 0;
    var heightStr = 300;  //新增 ，编辑 对话框的高度。
    var sqlArray = new Array();
    var columnsTemp = new Array();
    var index = 0;
    var messTemp = "";
    var databaseConfigId;

    //左侧菜单
    $(function () {
        initSqlStudyTree();
        $.ajax({
            type: 'get',
            url: "${ctx}/system/permission/i/getConfigAllDataBase",
            success: function (data) {
                $.each(data, function (index, value) {
                    $("#databaseSelect").append("<option value='" + data[index].id + "'>" + data[index].ip + ":" + data[index].port + " </option>");
                });
                initDataBase();
                query();
            }
        });
    });

    //更改当前 数据库
    $("#databaseSelect").change(function () {
        databaseConfigId = $('#databaseSelect').val();
        window.mainpage.mainTabs.jumpHome();
        //alert( databaseConfigId );
        initDataBase();
        query();
    })

    //主页-系统状态参数
    function query() {
        dg2 = $('#dg2').datagrid({
            method: "get",
            url: "${ctx}/system/permission/i/selectNoSQLDBStatus/" + databaseConfigId,
            fit: true,
            fitColumns: true,
            border: false,
            striped: true,
            idField: 'key',
            pagination: true,
            rownumbers: true,
            pageNumber: 1,
            pageSize: 50,
            pageList: [10, 20, 30, 40, 50],
            columns: [[
                {field: 'parameter', title: '属性', sortable: true, width: 80},
                {field: 'value', title: '值', sortable: true, width: 80},
                {field: 'content', title: '说明', sortable: true, width: 100}
            ]],
            enableHeaderClickMenu: true,
            enableHeaderContextMenu: true,
            enableRowContextMenu: false,
            rowTooltip: true,
            toolbar: '#tb'
        });
    }

    //左侧菜单 库表 展示
    function initDataBase() {
        databaseConfigId = $('#databaseSelect').val();
        dg = $('#pid').treegrid({
            method: "GET",
            url: "${ctx}/system/permission/i/allDatabaseListForNoSQL/" + databaseConfigId,
            fit: true,
            fitColumns: true,
            border: false,
            idField: 'id',
            treeField: 'name',
            parentField: 'pid',
            iconCls: 'icon',
            animate: true,
            rownumbers: false,
            singleSelect: true,
            striped: true,
            columns: [[
                {field: 'name', title: '&nbsp;&nbsp;详情', width: 210}
            ]],
            enableHeaderClickMenu: false,
            enableHeaderContextMenu: false,
            enableRowContextMenu: true,

            dataPlain: false,
            onClickRow: function (rowData) {
                NoSQLDbName = rowData.name;
                type = rowData.type;
                columnsTemp.length = 0;
                var rootNode = $('#pid').treegrid('getRoot', rowData.id);
                var dbName = rootNode.name;
                if (type == 'table') {//表
                    selectRowCount = 0;
                    // $("#currentTableName").val(redisDbName );
                    clickNoSQLDbName(NoSQLDbName);
                }
            }
        });
    }

    function clickNoSQLDbName(NoSQLDbName) {
        window.mainpage.mainTabs.addModule(NoSQLDbName, '${ctx}/system/permission/i/showNoSQLDBData/' + NoSQLDbName + '/' + databaseConfigId, 'icon-berlin-calendar');
    }

    function initSqlStudyTree() {
        $('#sqlStudyList').treegrid({
            method: "GET",
            url: "${ctx}/system/permission/i/selectSqlStudy",
            fit: true,
            fitColumns: true,
            border: false,
            idField: 'id',
            treeField: 'title',
            parentField: 'pid',
            iconCls: 'icon',
            animate: true,
            rownumbers: false,
            singleSelect: true,
            striped: true,
            columns: [[
                {field: 'title', title: '&nbsp;&nbsp;详情', width: 210}
            ]],
            enableHeaderClickMenu: false,
            enableHeaderContextMenu: false,
            enableRowContextMenu: true,

            dataPlain: false,
            onClickRow: function (rowData) {
                // $("#mainTabs").tabs("select", 0 ); //TAB切换到第一项
                var content = rowData.content;
                showSQLMess(content);
            }
        });
    }

    function showSQLMess(content) {
        $.messager.show({
            title: 'SQL Mess',
            width: 600,
            height: 200,
            msg: content,
            timeout: 5000,
            showType: 'slide'
        });
    }

    function showSQLMess2(messId) {
        if ($("#mainTabs").tabs('exists', 'show SQL')) {
            $("#mainTabs").tabs("select", 'show SQL');
        } else {
            window.mainpage.mainTabs.addModule('show SQL', '${ctx}/system/permission/i/showSQLMess/', 'icon-berlin-calendar');
        }
    }

    function jsonFormat() {
        window.mainpage.mainTabs.addModule('Json格式化', '${ctx}/system/permission/i/jsonFormat', 'icon-berlin-calendar');
    }

    //库备份
    function backupDatabase() {
        window.mainpage.mainTabs.addModule('备份', '${ctx}/system/permission/i/backupDatabase/' + databaseName, 'icon-berlin-calendar');
    }

    // 数据库配置 列表
    function ShowConfigPage() {
        window.mainpage.mainTabs.addModule('数据库配置', '${ctx}/system/permission/i/config', 'icon-berlin-calendar');
    }

    function ShowPasswordDialog() {
        pwd = $("#dlgg").dialog({
            title: '修改密码',
            width: 380,
            height: 160,
            href: '${ctx}/system/permission/i/changePass',
            maximizable: true,
            modal: true,
            //openAnimation:'fade',
            //closeAnimation:'slide',
            //closeDuration:900,
            buttons: [
                {
                    text: '确认',
                    iconCls: 'icon-edit',
                    handler: function () {
                        $("#mainform").submit();
                    }
                }, {
                    text: '取消',
                    iconCls: 'icon-cancel',
                    handler: function () {
                        pwd.panel('close');
                    }
                }]
        });
    }

    function refresh2() {
        $("#dg2").datagrid('reload');
    }

    function clearSQL() {
        $('#cacheKey').val("");
        $('#cacheValue').val("");
    }

    function contribute() {
        $("#addRow").dialog({
            title: "捐赠",
            width: 480,
            height: 500,
            href: "${ctx}/system/permission/i/contribute",
            maximizable: true,
            modal: true,
            buttons: [
                {
                    text: '关闭',
                    iconCls: 'icon-cancel',
                    handler: function () {
                        $("#addRow").panel('close');
                    }
                }]
        });
    }

    function help() {
        $("#addRow").dialog({
            title: "帮助",
            width: 500,
            height: 300,
            href: "${ctx}/system/permission/i/help",
            maximizable: true,
            modal: true,
            buttons: [
                {
                    text: '关闭',
                    iconCls: 'icon-cancel',
                    handler: function () {
                        $("#addRow").panel('close');
                    }
                }]
        });
    }

    function selectTheme(theme) {
        editor.setOption("theme", theme);
        executeMessage.setOption("theme", theme);
    }

    function selectTheme() {
        var input = document.getElementById("codeThemeSelector");
        var theme = input.options[input.selectedIndex].textContent;
        editor.setOption("theme", theme);
        executeMessage.setOption("theme", theme);
    }

    var obj;
    var willChangeRow = new Array();

    function refresh(index) {
        $('#selectDg' + index).datagrid('reload');
        $('#selectDg' + index).datagrid('clearSelections').datagrid('clearChecked');
    }

    //取消 修改
    function cancelChange(index) {
        endEdit(index);
        refresh(index);
    }

    function refresh(index) {
        $('#selectDg' + index).datagrid('reload');
        $('#selectDg' + index).datagrid('clearSelections').datagrid('clearChecked');
    }

    function imgerror(img) {
        img.src = "${ctx}/assets/images/logo.png";
        img.onerror = null;
    }

    //实时状态监控
    function infoData() {
        // alert( databaseConfigId );
        parent.window.mainpage.mainTabs.addModule("实时状态监控", '${ctx}/system/permission/i/infoData/' + databaseConfigId, 'icon-standard-bricks');
    }

    window.onbeforeunload = function (event) {
        event.preventDefault();
    }
</script>
</body>
</html>
