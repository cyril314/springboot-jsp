<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <meta http-equiv="Expires" content="0">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Cache-control" content="no-cache">
    <meta http-equiv="Cache" content="no-cache">
    <title></title>
    <%@ include file="/views/include/easyui.jsp" %>
</head>
<body>
<div id="tb3" style="padding:5px;height:auto">
    <div>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" plain="true" id="addRowButton" onclick="backupDB()"> 开始备份</a>
        <span class="toolbar-item dialog-tool-separator"></span>

        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-standard-database-edit" plain="true" id="addRowButton" onclick="restoreDB()">
            库还原</a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <%--
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-rar" plain="true" id="rarButton"   onclick="zipFile()">压缩文件</a>
        <span class="toolbar-item dialog-tool-separator"></span>
        --%>

        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" id="delButton" onclick="deleteBackupFile()">删除</a>
        <span class="toolbar-item dialog-tool-separator"></span>

        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-standard-arrow-refresh" plain="true" onclick="refresh()">刷新</a>
        <span class="toolbar-item dialog-tool-separator"></span>

        <a href="javascript:void(0)" class="easyui-linkbutton" plain="true">&nbsp;</a>
        <span class="toolbar-item dialog-tool-separator"></span>

    </div>
</div>
<input type="hidden" id="databaseName" value="${databaseName}">
<table id="dg3"></table>

<script type="text/javascript">
    var dgg;
    var tableName;
    var databaseName;

    $(function () {
        databaseName = $("#databaseName").val();
        tableName = $("#tableName").val();
        initData();
    });

    function initData() {
        dgg = $('#dg3').datagrid({
            method: "get",
            url: '${ctx}/system/permission/i/backupDatabaseData',
            fit: true,
            fitColumns: true,
            border: false,
            idField: 'fileName',
            striped: true,
            pagination: true,
            rownumbers: true,
            pageNumber: 1,
            pageSize: 20,
            pageList: [10, 20, 30, 40, 50],
            singleSelect: false,
            columns: [[
                {field: 'TREESOFTPRIMARYKEY', checkbox: true},
                {field: 'fileName', title: '文件名', sortable: true, width: 100},
                {field: 'fileLength', title: '文件大小', sortable: true, width: 100},
                {field: 'fileModifiedDate', title: '创建时间', sortable: true, width: 100, tooltip: true},
                {
                    field: 'action', title: '下载',
                    formatter: function (value, row, index) {
                        //return '<a href="${ctx}/backup/'+row.fileName+'" target="_blank"   ><div class="icon-hamburg-down" style="width:16px;height:16px" title="下载"></div></a>';
                        return '<a href="javascript:downloadFile3(\'' + row.fileName + '\')"   ><div class="icon-hamburg-down" style="width:16px;height:16px" title="下载"></div></a>';
                    }
                }

            ]],
            checkOnSelect: true,
            selectOnCheck: true,
            extEditing: false,
            toolbar: '#tb3',
            autoEditing: false,     //该属性启用双击行时自定开启该行的编辑状态
            singleEditing: false
        });
    }

    function backupDB() {
        // alert( parent.databaseConfigId );
        var databaseConfigId = parent.databaseConfigId;
        parent.$.messager.show({title: "提示", msg: "备份中，请稍等！", position: "bottomRight"});
        $.ajax({
            type: 'POST',
            contentType: 'application/json;charset=utf-8',
            url: "${ctx}/system/permission/i/backupNotSqlDatabase/",
            data: JSON.stringify({'databaseConfigId': databaseConfigId}),
            datatype: "json",
            //成功返回之后调用的函数
            success: function (data) {
                var status = data.status;
                if (status == 'success') {
                    $('#dg3').datagrid('reload');
                    parent.$.messager.show({title: "提示", msg: data.mess, position: "bottomRight"});
                    // window.setTimeout(function () { $('#dg3').datagrid('reload'); }, 1000);
                    // $('#dg3').datagrid('reload')
                } else {
                    parent.$.messager.show({title: "提示", msg: data.mess, position: "bottomRight"});
                }
            }
        });
    }

    function downloadFile3(fileName) {
        //alert( fileName  );
        var url = "${ctx}/system/permission/i/backupFileDownload/" + fileName;
        window.open(url);
    }


    //删除文件
    function deleteBackupFile() {

        var checkedItems = $('#dg3').datagrid('getChecked');
        var length = checkedItems.length;

        if (checkedItems.length == 0) {
            parent.$.messager.show({title: "提示", msg: "请先选择一行数据！", position: "bottomRight"});
            return;
        }

        var ids = [];
        $.each(checkedItems, function (index, item) {
            ids.push(item["fileName"]);
        });

        $.easyui.messager.confirm("操作提示", "您确定要删除" + length + "个文件吗？", function (c) {
            if (c) {

                $.ajax({
                    type: 'POST',
                    contentType: 'application/json;charset=utf-8',
                    url: "${ctx}/system/permission/i/deleteBackupFile",
                    data: JSON.stringify({'databaseName': databaseName, 'tableName': tableName, 'ids': ids}),
                    datatype: "json",
                    //成功返回之后调用的函数
                    success: function (data) {
                        var status = data.status;
                        if (status == 'success') {
                            $('#dg3').datagrid('reload');
                            $("#dg3").datagrid('clearSelections').datagrid('clearChecked');
                            parent.$.messager.show({title: "提示", msg: "删除成功！", position: "bottomRight"});
                        } else {
                            parent.$.messager.show({title: "提示", msg: data.mess, position: "bottomRight"});
                        }
                    }
                });
            }
        });
    }

    //压缩文件
    function zipFile() {

        var checkedItems = $('#dg3').datagrid('getChecked');
        var length = checkedItems.length;

        if (checkedItems.length == 0) {
            parent.$.messager.show({title: "提示", msg: "请先选择一行数据！", position: "bottomRight"});
            return;
        }

        var ids = [];
        $.each(checkedItems, function (index, item) {
            ids.push(item["fileName"]);
        });

        $.ajax({
            type: 'POST',
            contentType: 'application/json;charset=utf-8',
            url: "${ctx}/system/permission/i/zipFile",
            data: JSON.stringify({'ids': ids}),
            datatype: "json",
            //成功返回之后调用的函数
            success: function (data) {
                var status = data.status;
                if (status == 'success') {
                    $('#dg3').datagrid('reload');
                    $("#dg3").datagrid('clearSelections').datagrid('clearChecked');
                    parent.$.messager.show({title: "提示", msg: "删除成功！", position: "bottomRight"});
                } else {
                    parent.$.messager.show({title: "提示", msg: data.mess, position: "bottomRight"});
                }
            }
        });

    }


    function refresh() {
        $('#dg3').datagrid('reload');
        $("#dg3").datagrid('clearSelections').datagrid('clearChecked');
    }


    function restoreDB() {
        var databaseConfigId = parent.databaseConfigId;
        var checkedItems = $('#dg3').datagrid('getChecked');
        var length = checkedItems.length;

        if (checkedItems.length == 0) {
            parent.$.messager.show({title: "提示", msg: "请先选择一行数据！", position: "bottomRight"});
            return;
        }

        if (checkedItems.length > 1) {
            parent.$.messager.show({title: "提示", msg: "您只需选择一行备份数据！", position: "bottomRight"});
            return;
        }

        var ids = [];
        $.each(checkedItems, function (index, item) {
            ids.push(item["fileName"]);
        });

        databaseName = $("#databaseSelect", window.parent.document).val();
        // alert( databaseName );

        $.easyui.messager.confirm("操作提示", "您确定要还原数据库吗？", function (c) {
            if (c) {
                parent.$.messager.show({title: "提示", msg: "数据库还原中，请稍侯查看！", position: "bottomRight"});
                $.ajax({
                    type: 'POST',
                    contentType: 'application/json;charset=utf-8',
                    url: "${ctx}/system/permission/i/restoreDBForRedis/",
                    data: JSON.stringify({'databaseConfigId': databaseConfigId, 'ids': ids}),
                    datatype: "json",
                    //成功返回之后调用的函数
                    success: function (data) {
                        var status = data.status;
                        if (status == 'success') {
                            $('#dg3').datagrid('reload');
                            parent.$.messager.show({title: "提示", msg: data.mess, position: "bottomRight"});
                        } else {
                            parent.$.messager.show({title: "提示", msg: data.mess, position: "bottomRight"});
                        }
                    }
                });
            }
        });
    }


</script>

</body>
</html>