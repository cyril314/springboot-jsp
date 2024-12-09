<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%response.setStatus(200);%>

<!DOCTYPE html>
<html>
<head>
    <title>403 - 用户权限不足</title>
</head>
<body>
<h2>403 - 用户权限不足</h2>
</body>
</html>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>403 - Permission denied!</title>
    <link rel="stylesheet" href="${ctx}/assets/css/error.css">
</head>
<body>
<div class="error__container">
    <div class="error__code">
        <p>4</p>
        <p>0</p>
        <p>3</p>
    </div>
    <div class="error__title">Permission denied</div>
    <div class="error__description"> Insufficient user permissions.</div>
    <button class="action">Go Home</button>
</div>
</body>
</html>