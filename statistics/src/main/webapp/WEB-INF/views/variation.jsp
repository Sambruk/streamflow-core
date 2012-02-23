<%--


    Copyright 2009-2012 Streamsource AB

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Streamflow Statistik</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Streamflow Statistik">
    <meta name="author" content="Henrik Reinhold & Arvid Huss, Jayway AB">

    <!-- HTML5 shim, for IE6-8 support of HTML elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    
  	<!--[if lte IE 8]>
  	<script language="javascript" type="text/javascript" src="resources/js/external/excanvas.min.js"></script>
  	<![endif]-->

    <!-- Styles -->
    <link href="resources/css/external/bootstrap.css" rel="stylesheet">
    <link href="resources/css/external/bootstrap-responsive.css" rel="stylesheet">
    <link href="resources/css/style.css" rel="stylesheet">

    <!-- favorite and touch icons -->
	<link rel="shortcut icon" href="resources/images/favicon.png">
	<link rel="apple-touch-icon" href="resources/images/app_icons512x512.png">
	<link rel="apple-touch-icon" sizes="57x57" href="resources/images/app_icons57x57.png">
	<link rel="apple-touch-icon" sizes="72x72" href="resources/images/app_icons72x72.png">
	<link rel="apple-touch-icon" sizes="114x114" href="resources/images/app_icons114x114.png">
	
    <script type="text/javascript" src="resources/js/external/jquery-1.7.1.js"></script>
    <script type="text/javascript" src="resources/js/external/jquery.flot.min.js"></script>
</head>

<body>

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <a class="brand" href="#">Streamflow Statistik</a>
            <ul class="nav">
                <li><a href="index">Antal</a></li>
                <li class="active"><a href="varation">Variation</a></li>
            </ul>
        </div>
    </div>
</div>

<div class="container">

<section id="interval" class="well">
    <div class="row">
        <div class="span10">
            <form class="form-inline" method="POST">
                <label class="control-label" for="fromDate">Från:</label>
                <input class="date focused" name="fromDate" type="date" value="<c:out value="${fromDate}"/>">
                <label class="control-label" for="toDate">Till:</label>
                <input class="date focused" name="toDate" type="date" value="<c:out value="${toDate}"/>">
                <label class="control-label" for="perodicity">Ärendetyp:</label>
                <select name="caseTypeId" class="span2">
                    <option value="03d79ae7-e801-4338-8441-abb5b9d27522-1c1">Asfaltering</option>
                </select>
                <a href="#" onclick="{ document.forms[0].action = '';document.forms[0].submit();return false;}" class="btn btn-success">Uppdatera</a>
            </form>
        </div>
        <div class="span1">
            <div class="pull-right">
                <a href="#download" onclick="{ document.forms[0].action = 'download';document.forms[0].submit();return false;}" class="btn"><i class="icon-download"></i>Excel</a>
            </div>
        </div>
    </div>
</section>

<section id="summary">
    <div class="page-header">
        <h2>Variation</h2>
    </div>

    <div class="row">
        <div class="span12">
            <div id="graph-container" style="width:100%; height:400px;"></div>
        </div>
    </div>
</section>


<footer class="footer">
    <p>Powered by Streamflow</p>
</footer>

</div><!-- /container -->

<script type="text/javascript">
$(function () {
	var series = [
	              <c:forEach var="scatteredValue" items="${result}" >
                    [<c:out value="${scatteredValue.xAxis}" />,<c:out value="${scatteredValue.yAxis}" />],
                 </c:forEach>
                ];

    
    $.plot($("#graph-container"), [ series ],{
    xaxis: { mode: "time" },
    series: {
            lines: { show: false },
            points: { show: true }
    }});
});
</script>


</body>
</html>