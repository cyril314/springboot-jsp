<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title></title>
<%@ include file="/views/include/easyui.jsp"%>
</head>
<body>
<div>
	<form id="mainform" action="${ctx }/system/permission/i/saveSearchHistory" method="post" accept-charset="utf-8" >
              <input type="hidden" id="id"  name="id" />
              <input type="hidden" id="sql"  name="sql"  />
              <input type="hidden" id="dbName" name="dbName" />
		<table class="formTable">
			 <tr>
				<td>名称：</td>
				<td><input id="name" name="name" type="text"  class="easyui-validatebox" data-options="width: 300,required:'required'"/></td>
			</tr>
			 
			 <tr>
				<td> </td>
				<td>*请输入便于识别的查询条件名称。 </td>
			</tr>
		</table>
	</form>
</div>
<script type="text/javascript">
	$("#name").val($("#searchHistoryName", window.parent.document).val());
	//提交表单
	$('#mainform').form({
		onSubmit: function() {
			var id = $("#searchHistoryId", window.parent.document).val();
			var sql = $("#sqltextarea", window.parent.document).val();
			var dbName = $("#databaseSelect", window.parent.document).val();
			// alert( sql );
			if (sql == null) {
				return false;
			}

			$('#id').val(id);
			$('#sql').val(sql);
			$('#dbName').val(dbName);

			var isValid = $(this).form('validate');
			return isValid; // 返回false终止表单提交
		},
		success: function(data) {
			parent.$.messager.show({
				title: "提示",
				msg: "操作成功！",
				position: "bottomRight"
			});
			setTimeout(function() {
				parent.saveSearch.panel('close');
			}, 2000);
			searchBG.treegrid('reload');
		}
	});
</script>
</body>
</html>