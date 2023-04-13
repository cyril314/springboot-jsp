<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title>config</title>
<%@ include file="/views/include/easyui.jsp"%>
</head>
<body>
<div>
	<form id="mainform" action="${ctx }/system/permission/i/configUpdate" method="post">
	    <input id="id" name="id" type="hidden" value="${config.id }"   />
		<table class="formTable">
			<tr>
				<td>数据库类型： </td>
				<td>
				<select id="databaseType" name="databaseType" style="font-size:12px;" class="easyui-validatebox"   data-options="width: 150"   >
				   <option value="Redis" <c:if test="${config.databaseType=='Redis' }"> selected </c:if> >Redis </option>  
				   <option value="Memcache" <c:if test="${config.databaseType == 'Memcache' }">selected</c:if>  >Memcache</option>
				</select>
				</td>
			</tr>
			
			<%--			 
			 <tr>
				<td>名称：</td>
				<td><input id="databaseName"  name="databaseName" type="text" value="${config.databaseName }"   class="easyui-validatebox"   data-options="width: 150,required:'required'"/>
				</td>
			</tr>			 
			--%>
			
			<tr>
				<td>IP地址：</td>
				<td><input id="ip" name="ip" type="text" value="${config.ip }" class="easyui-validatebox" data-options="width: 150,required:'required',validType:'length[3,200]'"/></td>
			</tr>
			<tr>
				<td>端口：</td>
				<td><input  id="port" name="port" type="text" value="${config.port }" class="easyui-validatebox"   data-options="width: 150,required:'required'"    /> 
				<a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-tip" plain="true" title=" redis 默认端口为:6379 &#13;memcache默认端口为:11211 &#13; "></a>
				  </td>
			</tr>
			
			<%--
			<tr>
				<td>用户名：</td>
				<td><input id="userName"  name="userName" type="text" value="${config.userName }" class="easyui-validatebox"  data-options="width: 150,required:'required'" /></td>
			</tr>
			--%>
			<tr>
				<td>密   码：</td>
				<td><input id="password" name="password" type="password" value="${config.password }" class="easyui-validatebox"  data-options="width: 150"  /></td>
			</tr>
			 
			 <tr>
				<td>是否默认：</td>
				<td><input id="isdefault" name="isdefault" value="1" type="checkbox"  <c:if test="${config.isdefault == '1' }">checked</c:if> /></td>
			</tr>
			 
			 <tr>
				<td> </td>
				<td> <span id="mess2">  </span> </td>
			</tr>
		</table>
	</form>
</div>

<script type="text/javascript">
var connSuccess = false;
$('#mainform').form({
    onSubmit: function(){    
    	var isValid = $(this).form('validate');
		return isValid;	// 返回false终止表单提交
    },    
    success:function(data){  
    	var obj = eval('(' + data + ')');
    	parent.$.messager.show({ title : "提示",msg: obj.mess , position: "bottomRight" });
    	setTimeout(function () {
           config.panel('close');
           $("#dg3").datagrid('reload');
            
        }, 2000);
    }
});   

//测试连接
 function  testConn(){
	 $("#mess2").html("连接测试中..." );
	  $.ajax({
			type:'POST',
		   	contentType:'application/json;charset=utf-8',
            url:"${ctx}/system/permission/i/testConn",
            data: JSON.stringify({ 'databaseType':$("#databaseType").val(),'databaseName':$("#databaseName").val(),'port':$("#port").val(),'password':$("#password").val(),'ip':$("#ip").val() } ),
            datatype: "json",
            //成功返回之后调用的函数             
            success:function(data){
            	var status = data.status ;
            	if(status == 'success' ){
            		$("#mess2").html(data.mess );
            		connSuccess = true;
            	}else{
            		connSuccess= false;
            		$("#mess2").html("连接失败！" );
            	}
            }  
     });
 }
</script>
</body>
</html>