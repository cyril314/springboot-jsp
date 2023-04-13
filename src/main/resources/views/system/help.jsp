<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title></title>
    <style>
        .table_border {
            border: solid 1px #B4B4B4;
            border-collapse: collapse;
        }

        .table_border tr th {
            padding-left: 4px;
            height: 27px;
            border: solid 1px #B4B4B4;
        }

        .table_border tr td {
            height: 25px;
            padding: 4px;
            border: solid 1px #B4B4B4;
        }
    </style>
</head>
<body>
<div>
    <table class="table_border">
        <tbody>
        <th align="left" valign="top"><b> 说明 </b></th>
        <tr>
            <td align="left" valign="top">1、当Memcached的value值为乱码，是由于数据序列化保存，属于正常现象。</td>
        </tr>
        <tr>
            <td align="left" valign="top">2、需要将本软件与Redis布在同一台服务器上，才能实现Redis的备份。</td>
        </tr>
        <tr>
            <td align="left" valign="top">3、数据库的实时监控数据暂不保存。</td>
        </tr>
        <tr>
            <td align="left" valign="top">4、可同时配置redis, memcached数据库。</td>
        </tr>
        </tbody>
    </table>
</div>
</body>
</html>