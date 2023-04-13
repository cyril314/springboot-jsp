<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title></title>
<%@ include file="/views/include/easyui.jsp"%>
</head>
<body>
<div>
	<form id="mainform" action="${ctx }/system/permission/i/changePassUpdate" method="post">
		<table class="formTable">
			 <tr>
				<td>旧密码：</td>
				<td><input name="oldPass" type="text"  class="easyui-validatebox" data-options="width: 150,required:'required'"/></td>
			</tr>
			 
			<tr>
				<td>新密码：</td>
				<td><input name="newPass" type="text"  class="easyui-validatebox" data-options="width: 150,required:'required',validType:'length[6,20]'"/></td>
			</tr>
		</table>
		<span>&nbsp;&nbsp;提示：此密码仅为本系统使用，与数据库无关！ </span>
	</form>
</div>

<script type="text/javascript">
$('#mainform').form({
    onSubmit: function(){    
    	var isValid = $(this).form('validate');
		return isValid;	// 返回false终止表单提交
    },    
    success:function(data){   
    	// successTip(data,dg,d);
    	var dataObj=eval("("+data+")");//转换为json对象
    	var status = dataObj.status ;
        if(status == 'success' ){
         	parent.$.messager.show({ title : "提示",msg: dataObj.mess, position: "bottomRight" });
            setTimeout(function () {
              parent.pwd.panel('close');
            }, 2000);
        }else{
          	parent.$.messager.show({ title : "提示",msg: dataObj.mess, position: "bottomRight" });
        }
    }
});    
</script>
</body>
</html>