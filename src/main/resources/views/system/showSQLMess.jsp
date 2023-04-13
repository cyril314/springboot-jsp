<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title></title>
    <%@ include file="/views/include/easyui.jsp" %>
</head>
<body>
<div id="showPanel">
    <div id="operator" class="panel-header panel-header-noborder  " style="padding:5px;height:auto">
        <div>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-redo" plain="true" onclick="clearSQL()">清空</a>
            <span class="toolbar-item dialog-tool-separator"></span>
        </div>
    </div>
    <div style="width:100%;height:500px ">
        <input type="hidden" id="messId" value="${messId}">
        <textarea id="sqltextarea" name="sqltextarea" style="margin:10px; font-size:14px;font-family: '微软雅黑';width:97%;height:75%; "></textarea>
        <input type="text" id="ddd" name="ddd"/>
    </div>
</div>
<script type="text/javascript">
    var messId;

    $(function () {
        messId = $("#messId").val();
        // query();
    });

    // delete
    function query() {
        //alert("dddd");
        $.ajax({
            type: 'GET',
            contentType: 'application/json;charset=utf-8',
            url: "${ctx}/system/permission/i/getSQLMess/" + messId,
            datatype: "json",
            //成功返回之后调用的函数
            success: function (data) {
                var status = data.status;
                if (status == 'success') {
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

    function clearSQL() {
        $('#sqltextarea').val("");
    }
</script>
</body>
</html>