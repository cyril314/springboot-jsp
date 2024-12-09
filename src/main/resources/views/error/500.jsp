<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%@ page import="org.slf4j.Logger,org.slf4j.LoggerFactory" %>
<%response.setStatus(200);%>
<%
    Throwable ex = null;
    if (exception != null)
        ex = exception;
    if (request.getAttribute("javax.servlet.error.exception") != null)
        ex = (Throwable) request.getAttribute("javax.servlet.error.exception");
    //记录日志
    Logger logger = LoggerFactory.getLogger("500.jsp");
    logger.error(ex.getMessage(), ex);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>500 - System error!</title>
    <link rel="stylesheet" href="${ctx}/assets/css/error.css">
</head>
<body>
<div class="error__container">
    <div class="error__code">
        <p>5</p>
        <p>0</p>
        <p>0</p>
    </div>
    <div class="error__title">System error Page</div>
    <div class="error__description">Internal system error</div>
    <button class="action">Go Home</button>
</div>
</body>
</html>
