<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title>TreeNms数据库管理系统</title>
    <meta name="Keywords" content="TreeNms数据库管理系统,treeNMS">
    <meta name="Description" content="TreeNms数据库管理系统,treeNMS">
    <script src="${ctx}/assets/plugins/easyui/jquery/jquery-1.11.1.min.js"></script>
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/bglogin.css"/>
    <link rel="icon" href="${ctx}/favicon.ico" mce_href="${ctx}/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="${ctx}/favicon.ico" mce_href="${ctx}/favicon.ico" type="image/x-icon">
    <script>
        if (window.top != null && window.top.location != window.location) {
            window.top.location = window.location;
        }

        function refreshCaptcha() {
            document.getElementById("img_captcha").src = "${ctx}/assets/img/securityCode.jpg?t=" + Math.random();
        }

        function check() {
            if ($("#username").val() == "") {
                $("#login_main_errortip").html("请输入用户名!");
                return false;
            }
            if ($("#password").val() == "") {
                $("#login_main_errortip").html("请输入密码!");
                return false;
            }
            if ($("#captcha").val() == "") {
                $("#login_main_errortip").html("请输入验证码!");
                return false;
            }

            if ($("#username").val().length > 15) {
                $("#login_main_errortip").html("请输入用户名!");
                return false;
            }
            return true;
        }
    </script>
</head>
<body>
<div>
    <form id="loginForm" action="${ctx}/loginVaildate" method="post">
        <div class="login_top">
            <div class="login_title">
                <span style="margin-left: 10px; margin-top: 10px;color: #fff">
                    <img src="${ctx}/assets/img/logo.png" />TreeNms数据库管理系统
                    <span style="color: #00824D;font-size:16px; font-weight:bold;">&nbsp;TreeNMS</span>
                    <span style="color: #fff;font-size:12px;">V1 </span>
                </span>
            </div>
        </div>
        <div style="float:left;width:100%;">
            <div class="login_main">
                <div class="login_main_top"></div>
                <div class="login_main_errortip" id="login_main_errortip">&nbsp; ${message} </div>
                <div class="login_main_ln">
                    <input type="text" id="username" name="username"/>
                </div>
                <div class="login_main_pw">
                    <input type="password" id="password" name="password"/>
                </div>
                <div class="login_main_yzm">
                    <input type="text" id="captcha" name="captcha"/>
                    <img alt="验证码" src="${ctx}/assets/img/securityCode.jpg" title="点击更换" id="img_captcha" onclick="javascript:refreshCaptcha();"/>
                </div>
                <div class="login_main_remb">
                    <input id="rm" name="rememberMe" type="hidden"/>  <!-- <label for="rm"><span>记住我</span></label> -->
                </div>
                <div class="login_main_submit">
                    <input type="submit" value="" onclick="return check()"/>
                </div>
            </div>
        </div>
        <div style="text-align:center">适用于Redis, Memcached, 推荐使用Chrome, FireFox, IE9+ 浏览器</div>
    </form>
</div>
</body>
</html>
