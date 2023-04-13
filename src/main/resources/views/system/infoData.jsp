<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title></title>
    <%@ include file="/views/include/easyui.jsp" %>
    <%@ include file="/views/include/codemirror.jsp" %>

    <script type="text/javascript" src="${ctx}/assets/js/echarts.js"></script>
    <script type="text/javascript" src="${ctx}/assets/js/codemirror.js"></script>
    <script type="text/javascript" src="${ctx}/assets/js/chart/line.js"></script>
    <script type="text/javascript" src="${ctx}/assets/js/chart/bar.js"></script>

    <%--<script type="text/javascript" src="${ctx}/assets/js/javascript.js"></script> --%>
    <%-- <script type="text/javascript" src="${ctx}/assets/js/esl.js"></script> --%>
    <style>

        .main {
            overflow: hidden;
            height: 250px;
            padding: 0px;
            margin: 10px;
            border: 1px solid #e3e3e3;
            -webkit-border-radius: 4px;
            -moz-border-radius: 4px;
            border-radius: 4px;
            -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
            -moz-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
            box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
        }

        .mainChar {
            overflow: hidden;
            padding: 0px;
            margin: 10px;
            border: 1px solid #e3e3e3;
            -webkit-border-radius: 4px;
            -moz-border-radius: 4px;
            border-radius: 4px;
            -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
            -moz-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
            box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
        }

        .main3 {
            overflow: hidden;
            height: 250px;
            width: 48%;
            float: left;
            padding: 0px;
            margin: 10px;
            border: 1px solid #e3e3e3;
            -webkit-border-radius: 4px;
            -moz-border-radius: 4px;
            border-radius: 4px;
            -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
            -moz-box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
            box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.05);
        }

    </style>
</head>
<body>
<div id="tb2" style="height:auto">
    <input type="hidden" id="databaseConfigId" value="${databaseConfigId}">
    <div class="panel-header panel-header-noborder" style="padding: 5px; height: auto">
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-standard-arrow-refresh" plain="true" onclick="refresh()">刷新</a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <input type="checkbox" checked id="isAuto">自动刷新  </input>
        <span class="toolbar-item dialog-tool-separator"></span>
        <a href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-tip" plain="true" title="刷新间隔10秒"></a>
        <span class="toolbar-item dialog-tool-separator"></span>
        <a href="javascript:void(0)" class="easyui-linkbutton" plain="true">${databaseName} </a>
        <span class="toolbar-item dialog-tool-separator"></span>
    </div>
</div>

<div>
    <div class="mainChar" style="text-align:center">
        <div style="margin:5px;padding:5px;height:auto;width:17%;border:1px solid #95B8E7;float:left; text-align:center">
            <span>used memory </span> <br>
            <span style="font-size:23px" id="usedMemory">0 </span>
        </div>
        <div style="margin:5px;padding:5px;height:auto;width:17%;border:1px solid #95B8E7;float:left; text-align:center">
            <span>total keys </span> <br>
            <span style="font-size:23px" id="totalKeys">0 </span>
        </div>
        <div style="margin:5px;padding:5px;height:auto;width:17%;border:1px solid #95B8E7;float:left; text-align:center">
            <span> Clients </span> <br>
            <span style="font-size:23px" id="connectedClients">0 </span>
        </div>
        <div style="margin:5px;padding:5px;height:auto;width:17%;border:1px solid #95B8E7;float:left; text-align:center">
            <span>commands processed </span> <br>
            <span style="font-size:23px" id="totalCommandsProcessed">0 </span>
        </div>
        <div style="margin:5px;padding:5px;height:auto;width:17%;border:1px solid #95B8E7;float:left; text-align:center">
            <span>used_cpu_sys </span> <br>
            <span style="font-size:23px" id="usedCpuSys">0 </span>
        </div>
    </div>

    <div id="graphic" style="height:auto;width:auto;">
        <div id="main" class="main"></div>
    </div>

    <div id="graphic3" style="height:auto;width:auto;">
        <div id="main3" class="main"></div>
    </div>

    <div id="graphic2" style="height:auto;width:auto;">
        <div id="main2" class="main"></div>
    </div>

    <div id="graphic3" style="height:auto;width:auto;">
        <div id="main4" class="main4" style="display:none"></div>
        <div id="main5" class="main5" style="display:none"></div>
    </div>
</div>

<script type="text/javascript">
    var timeTicket
    var lastData = 11;
    var axisData;
    var databaseConfigId;

    $(function () {
        clearInterval(timeTicket);
        timeTicket = setInterval(function () {
            mainAddData();
        }, 10000);

        queryInfoItem();

        $("#isAuto").change(function () {
            if ($(this).is(':checked')) {

                timeTicket = setInterval(function () {
                    mainAddData();
                }, 10000);
            } else {
                clearInterval(timeTicket);
            }
        });
    });

    function refresh() {
        queryInfoItem();
    }

    //查询状态参数
    function queryInfoItem() {
        databaseConfigId = $('#databaseConfigId').val();
        $.ajax({
            type: 'get',
            url: "${ctx}/system/permission/i/queryInfoItem/" + databaseConfigId,
            success: function (data) {
                // alert( data.connected_clients    );
                $("#usedMemory").html(data.used_memory_human);
                $("#connectedClients").html(data.connected_clients);
                $("#totalKeys").html(data.totalKeys);
                $("#totalCommandsProcessed").html(data.total_commands_processed);
                $("#usedCpuSys").html(data.used_cpu_sys);
            }
        });
    }
    var myChart;
    var total_commands_processed1 = 0;
    var qps = 0;
    var myChart2;
    var myChart3;
    // 使用
    require(
        [
            'echarts',
            'echarts/chart/line',
            'echarts/chart/bar' // 使用柱状图就加载bar模块，按需加载  
        ],
        function (ec) {
            // 基于准备好的dom，初始化echarts图表  
            myChart = ec.init(document.getElementById('main'));
            var option = {
                title: {
                    text: 'used memory (Byte)',
                    subtext: ''
                },
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    data: ['最高值', '当前值']
                },
                toolbox: {
                    show: true,
                    feature: {
                        mark: {show: false},
                        dataView: {show: true, readOnly: true},
                        magicType: {show: true, type: ['line', 'bar']},
                        restore: {show: true},
                        saveAsImage: {show: true}
                    }
                },
                xAxis: [
                    {
                        type: 'category',
                        boundaryGap: false,
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    }
                ],
                series: [
                    {
                        name: '最高值',
                        type: 'line',
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    },
                    {
                        name: '当前值',
                        type: 'line',
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    }
                ]
            };

            // 为echarts对象加载数据
            myChart.setOption(option);
        }
    );

    function mainAddData() {
        var datetime = new Date();
        axisData = datetime.getHours() + ":" + datetime.getMinutes() + ":" + datetime.getSeconds();
        $.ajax({
            type: "get",
            url: "${ctx}/system/permission/i/queryInfoItem/" + databaseConfigId,
            dataType: "json",
            success: function (data) {
                // myChart.hideLoading();
                // 动态数据接口 addData
                myChart.addData([
                    [
                        0,        // 系列索引
                        data.used_memory_peak, // 新增数据
                        false,     // 新增数据是否从队列头部插入
                        false     // 是否增加队列长度，false则自定删除原有数据，队头插入删队尾，队尾插入删队头
                    ],
                    [
                        1,        // 系列索引
                        data.used_memory, // 新增数据
                        false,    // 新增数据是否从队列头部插入
                        false,    // 是否增加队列长度，false则自定删除原有数据，队头插入删队尾，队尾插入删队头
                        axisData  // 坐标轴标签, For example 18:10:12
                    ]
                ]);

                myChart2.addData([
                    [
                        0,        // 系列索引
                        data.used_cpu_sys, // 新增数据
                        false,    // 新增数据是否从队列头部插入
                        false,    // 是否增加队列长度，false则自定删除原有数据，队头插入删队尾，队尾插入删队头
                        axisData  // 坐标轴标签, For example 18:10:12
                    ]
                ]);

                if (total_commands_processed1 != 0) {
                    qps = (data.total_commands_processed - total_commands_processed1) / 10;
                    myChart3.addData([
                        [
                            0,        // 系列索引
                            qps, // 新增数据
                            false,    // 新增数据是否从队列头部插入
                            false,    // 是否增加队列长度，false则自定删除原有数据，队头插入删队尾，队尾插入删队头
                            axisData  // 坐标轴标签, For example 18:10:12
                        ]
                    ]);
                }

                total_commands_processed1 = data.total_commands_processed;

                $("#usedMemory").html(data.used_memory_human);
                $("#connectedClients").html(data.connected_clients);
                $("#totalKeys").html(data.totalKeys);
                $("#totalCommandsProcessed").html(data.total_commands_processed);
                $("#usedCpuSys").html(data.used_cpu_sys);
            },
            error: function (errorMsg) {
                //  alert("不好意思，图表请求数据失败啦！");
            }
        });
    }

    // 使用
    require(
        [
            'echarts',
            'echarts/chart/line',
            'echarts/chart/bar' // 使用柱状图就加载bar模块，按需加载  
        ],
        function (ec) {
            // 基于准备好的dom，初始化echarts图表  
            myChart2 = ec.init(document.getElementById('main2'));

            var option = {
                title: {
                    text: 'used_cpu_sys',
                    subtext: ''
                },
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    data: ['used_cpu_sys']
                },
                toolbox: {
                    show: true,
                    feature: {
                        mark: {show: false},
                        dataView: {show: true, readOnly: false},
                        magicType: {show: true, type: ['line', 'bar']},
                        restore: {show: true},
                        saveAsImage: {show: true}
                    }
                },
                calculable: true,
                xAxis: [
                    {
                        type: 'category',
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    }
                ],
                series: [
                    {
                        name: 'used_cpu_sys',
                        type: 'line',
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    }
                ]
            };
            // 为echarts对象加载数据
            myChart2.setOption(option);
        }
    );

    // 使用
    require(
        [
            'echarts',
            'echarts/chart/line',
            'echarts/chart/bar' // 使用柱状图就加载bar模块，按需加载  
        ],
        function (ec) {
            // 基于准备好的dom，初始化echarts图表  
            myChart3 = ec.init(document.getElementById('main3'));

            var option = {
                title: {
                    text: 'QPS',
                    subtext: ''
                },
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    data: ['QPS']
                },
                toolbox: {
                    show: true,
                    feature: {
                        mark: {show: false},
                        dataView: {show: true, readOnly: false},
                        magicType: {show: true, type: ['line', 'bar']},
                        restore: {show: true},
                        saveAsImage: {show: true}
                    }
                },
                calculable: true,
                xAxis: [
                    {
                        type: 'category',
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    }
                ],
                series: [
                    {
                        name: 'QPS',
                        type: 'line',
                        data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    }
                ]
            };

            // 为echarts对象加载数据
            myChart3.setOption(option);
        }
    );

    // 使用
    require(
        [
            'echarts',
            'echarts/chart/line',
            'echarts/chart/bar' // 使用柱状图就加载bar模块，按需加载  
        ],
        function (ec) {
            // 基于准备好的dom，初始化echarts图表  
            var myChart = ec.init(document.getElementById('main4'));

            var option = {
                title: {
                    text: '内存占用率',
                    subtext: ''
                },
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    data: ['2015年']
                },
                toolbox: {
                    show: true,
                    feature: {
                        mark: {show: false},
                        dataView: {show: true, readOnly: false},
                        magicType: {show: true, type: ['line', 'bar']},
                        restore: {show: true},
                        saveAsImage: {show: true}
                    }
                },
                calculable: true,
                xAxis: [
                    {
                        type: 'category',
                        data: ['1月', '2月', '3月', '4月', '5月', '总']


                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        boundaryGap: [0, 0.01]
                    }
                ],
                series: [
                    {
                        name: '2015年',
                        type: 'bar',
                        data: [48203, 53489, 119034, 184970, 231744, 630230]
                    }
                ]
            };

            // 为echarts对象加载数据
            myChart.setOption(option);
        }
    );

    // 使用
    require(
        [
            'echarts',
            'echarts/chart/line',
            'echarts/chart/bar' // 使用柱状图就加载bar模块，按需加载  
        ],
        function (ec) {
            // 基于准备好的dom，初始化echarts图表  
            var myChart = ec.init(document.getElementById('main5'));

            var option = {
                title: {
                    text: '内存占用率',
                    subtext: ''
                },
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    data: ['2015年']
                },
                toolbox: {
                    show: true,
                    feature: {
                        mark: {show: false},
                        dataView: {show: true, readOnly: false},
                        magicType: {show: true, type: ['line', 'bar']},
                        restore: {show: true},
                        saveAsImage: {show: true}
                    }
                },
                calculable: true,
                xAxis: [
                    {
                        type: 'category',
                        data: ['1月', '2月', '3月', '4月', '5月', '总']
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        boundaryGap: [0, 0.01]

                    }
                ],
                series: [
                    {
                        name: '2015年',
                        type: 'bar',
                        data: [48203, 53489, 119034, 184970, 231744, 630230]
                    }
                ]
            };
            // 为echarts对象加载数据
            myChart.setOption(option);
        }
    );
</script>
</body>
</html>