<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title></title>
    <%@ include file="/views/include/easyui.jsp" %>
    <script src="${ctx}/assets/plugins/My97DatePicker/WdatePicker.js" type="text/javascript"></script>
</head>
<body>
<div id="tb2" style="padding:5px;height:auto">
    <div>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" plain="true" id="addRowButton" onclick="addRow2()"> 添加 </a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" plain="true" id="delButton" onclick="del()">删除</a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-edit" plain="true" id="editRowButton" onclick="editRow2()">修改</a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-hamburg-billing" plain="true" id="editTableButton" onclick="designTable()">设计</a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-standard-arrow-refresh" plain="true" onclick="refresh()">刷新</a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <a href="javascript:void(0)" class="easyui-linkbutton" plain="true">&nbsp;</a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-ok" plain="true" id="saveRowButton" onclick="saveRow()"> 保存 </a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-cancel" plain="true" id="cancelButton" onclick="cancelChange()"> 取消 </a>
        <span class="toolbar-item dialog-tool-separator"></span>
    </div>
</div>
<input type="hidden" id="databaseName" value="${databaseName}">
<input type="hidden" id="tableName" value="${tableName}">
<table id="dg2"></table>
<div id="dlg2"></div>
<div id="dlgg"></div>
<div id="addRow"></div>
<script type="text/javascript">
    var dg, d, primary_key, add, databaseName, tableName, obj;
    var selectRowCount = 0;
    var columnsTemp = new Array(), willChangeRow = new Array();

    $(function() {
        databaseName = $("#databaseName").val();
        tableName = $("#tableName").val();
        query();
    });

    //查询方法
    function query() {
        var url = "${ctx}/system/permission/i/table/" + tableName + "/" + databaseName;
        $.post(url, null, showGrid, "json");
    }

    //处理返回结果，并显示数据表格
    function showGrid(data) {
        //if (data.rows.length == 0) {
        // $.messager.alert("结果", "没有数据!", "info", null);
        // }
        var options = {
            // method: "POST",
            url: "${ctx}/system/permission/i/table/" + tableName + "/" + databaseName,
            rownumbers: true,
            fit: true,
            fitColumns: true,
            border: false,
            striped: true,
            pagination: true,
            pageNumber: 1,
            pageSize: 20,
            pageList: [10, 20, 30, 40, 50],
            singleSelect: false,
            checkOnSelect: true,
            selectOnCheck: true,
            extEditing: false,
            toolbar: '#tb2',
            autoEditing: true,
            //该属性启用双击行时自定开启该行的编辑状态
            singleEditing: true,
            onBeginEdit: function(index, row) {
                obj = JSON.stringify(row);
            },
            onAfterEdit: function(index, row, changes) {
                willChangeRow.push({
                    "oldData": eval('(' + obj + ')'),
                    "changesData": changes
                });

                // submitUpdate(rowData);
            },
            onSelect: function(index, row) {
                selectRowCount++;
                // alert( selectRowCount );
                //修改按钮只有选一行时 才有效
                if (selectRowCount == 1) {
                    $("#editRowButton").linkbutton("enable");
                } else {
                    $("#editRowButton").linkbutton("disable");
                }
            },
            onUnselect: function(index, row) {
                selectRowCount--;
                // alert( selectRowCount );
                //修改按钮只有选一行时 才有效

                // alert( selectRowCount );
                if (selectRowCount == 1) {
                    $("#editRowButton").linkbutton("enable");
                } else {
                    $("#editRowButton").linkbutton("disable");
                }
            },
            onDblClickCell: function(index, field, value) {
                $(this).datagrid('beginEdit', index);
                var ed = $(this).datagrid('getEditor', {
                    index: index,
                    field: field
                });
                $(ed.target).focus();
            }
        };
        options.columns = eval(data.columns); //把返回的数组字符串转为对象，并赋于datagrid的column属性
        options.idField = data.primaryKey;
        primary_key = data.primaryKey;
        dg = $("#dg2");
        dg.datagrid(options); //根据配置选项，生成datagrid
        dg.datagrid("loadData", data.rows); //载入本地json格式的数据
    }

    //确认修改  --delete
    function submitUpdate(row) {
        parent.$.messager.confirm('提示', '确定要修改数据？', function(data) {
            if (data) {
                $.ajax({
                    type: 'get',
                    //url:"${ctx}/system/scheduleJob/"+row.name+"/"+row.group+"/update",
                    url: "${ctx}/system/permission/i/updateRow/" + tableName + "/" + databaseName,
                    data: "cronExpression=" + row.cronExpression,
                    success: function(data) {
                        if (data == 'success') {
                            dg.datagrid('reload');
                            parent.$.messager.show({
                                title: "提示",
                                msg: "操作成功！",
                                position: "bottomRight"
                            });
                        } else {
                            parent.$.messager.alert(data);
                        }
                    }
                });
                d.panel('close');
            } else {
                dg.datagrid('rejectChanges');
            }
        });
    }

    //编辑 一行数据
    function editRow() {
        if (primary_key == "") {
            alert("表主键为空！");
            return;
        }

        var idValues;
        var checkedItems = $('#dg2').datagrid('getChecked');

        if (checkedItems.length == 0) {
            parent.$.messager.show({
                title: "提示",
                msg: "请先选择一行数据！",
                position: "bottomRight"
            });
            return;
        }

        $.each(checkedItems, function(index, item) {
            // id = item.id  ;
            idValues = item[primary_key];
        });

        add = $("#addRow").dialog({
            title: "编辑数据",
            width: 380,
            height: 350,
            //   href:"${ctx}/system/permission/i/editRows/"+tableName+"/"+databaseName +"/"+id+"/"+idValues,
            href: "${ctx}/system/permission/i/editRows/" + tableName + "/" + databaseName + "/" + primary_key + "/" + idValues,
            maximizable: true,
            modal: true,
            buttons: [{
                text: '确认',
                iconCls: 'icon-edit',
                handler: function() {
                    $("#mainform").submit();
                }
            }, {
                text: '取消',
                iconCls: 'icon-cancel',
                handler: function() {
                    add.panel('close');
                }
            }]
        });
    }

    //删除行
    //删除行时,先判断一下有没新增或编辑的数据行,如果有必须先提交才允许删.
    function del() {
        //表的主键字段
        var temp = $('#dg2').datagrid('options');
        var primary_key = temp.idField;
        // alert(  primary_key  );

        var inserted = $('#dg2').datagrid('getChanges', 'inserted');
        var updated = $('#dg2').datagrid('getChanges', 'updated');

        if (inserted.length || updated.length) {
            parent.$.messager.show({
                title: "提示",
                msg: "请先保存变更内容！",
                position: "bottomRight"
            });
            return;
        }

        var checkedItems = $('#dg2').datagrid('getChecked');
        var length = checkedItems.length;

        if (checkedItems.length == 0) {
            parent.$.messager.show({
                title: "提示",
                msg: "请先选择一行数据！",
                position: "bottomRight"
            });
            return;
        }

        $.easyui.messager.confirm("操作提醒", "您确定要删除" + length + "行数据？", function(c) {
            if (c) {
                $.ajax({
                    type: 'POST',
                    contentType: 'application/json;charset=utf-8',
                    url: "${ctx}/system/permission/i/deleteRows",

                    // data: JSON.stringify( { 'databaseName':databaseName, 'tableName':tableName,'primary_key':primary_key, 'ids':ids } ),
                    data: JSON.stringify({
                        'databaseName': databaseName,
                        'tableName': tableName,
                        'primary_key': primary_key,
                        'checkedItems': JSON.stringify(checkedItems)
                    }),

                    datatype: "json",
                    //成功返回之后调用的函数
                    success: function(data) {
                        var status = data.status;
                        if (status == 'success') {
                            $('#dg2').datagrid('reload');
                            $("#dg2").datagrid('clearSelections').datagrid('clearChecked');
                            selectRowCount = 0;
                            parent.$.messager.show({
                                title: "提示",
                                msg: "删除成功！",
                                position: "bottomRight"
                            });
                        } else {
                            parent.$.messager.show({
                                title: "提示",
                                msg: data.mess,
                                position: "bottomRight"
                            });
                        }
                    }
                });
            }
        });
    }

    function refresh() {
        $("#dg2").datagrid('reload');
        $("#dg2").datagrid('clearSelections').datagrid('clearChecked');
    }

    //设计表结构
    function designTable() {
        parent.window.mainpage.mainTabs.addModule("设计" + tableName, '${ctx}/system/permission/i/designTable/' + tableName + '/' + databaseName, 'icon-hamburg-config');
    }

    //保存修改,包括新增行,修改行.
    function saveRow() {
        endEdit();
        var inserted = $('#dg2').datagrid('getChanges', 'inserted');
        // var updated =  $('#dg2').datagrid('getChanges', 'updated');
        var updated = willChangeRow;

        var effectRow = new Object();

        effectRow["databaseName"] = databaseName;
        effectRow["tableName"] = tableName;
        effectRow["primary_key"] = primary_key;

        if (inserted.length) {
            //alert( JSON.stringify(inserted) );
            effectRow["inserted"] = JSON.stringify(inserted);
        }

        if (updated.length) {
            effectRow["updated"] = JSON.stringify(updated);
        }

        if (!inserted.length && !updated.length) {
            parent.$.messager.show({
                title: "提示",
                msg: "没有变更内容！",
                position: "bottomRight"
            });
            return;
        }

        $.post("${ctx}/system/permission/i/saveData", effectRow, function(rsp) {
            willChangeRow = new Array();
            if (rsp.status == "success") {
                parent.$.messager.show({
                    title: "提示",
                    msg: "保存成功！",
                    position: "bottomRight"
                });
                //$.messager.alert("提示", "保存成功！");
                //$('#dg2').datagrid('reload');
                $('#dg2').datagrid('acceptChanges');
            } else {
                $.messager.alert("提示", rsp.mess);
            }
        }, "JSON").error(function() {
            $.messager.alert("提示", "提交错误了！");
        });
    }

    function addRow2() {
        $('#dg2').datagrid('insertRow', {
            index: 0,
            row: {}
        });
        $('#dg2').datagrid("beginEdit", 0);
    }

    //编辑 一行数据
    function editRow2() {
        if (primary_key == "") {
            alert("表主键为空！");
            return;
        }

        var checkedItems = $('#dg2').datagrid('getChecked');
        if (checkedItems.length == 0) {
            parent.$.messager.show({
                title: "提示",
                msg: "请先选择一行数据！",
                position: "bottomRight"
            });
            return;
        }

        $.each(checkedItems, function(index, item) {
            var index = $('#dg2').datagrid("getRowIndex", item);
            $('#dg2').datagrid("beginEdit", index);
        });
    }

    //取消 修改
    function cancelChange() {
        endEdit();
        refresh();
    }

    //关闭编辑
    function endEdit() {
        var rows = $('#dg2').datagrid('getRows');
        for (var i = 0; i < rows.length; i++) {
            $('#dg2').datagrid('endEdit', i);
        }
    }
</script>
</body>
</html>