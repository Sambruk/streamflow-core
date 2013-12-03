<%--


    Copyright 2009-2013 Jayway Products AB

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
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
	<link href="resources/css/external/redmond/jquery-ui-1.8.10.custom.css" rel="stylesheet" type="text/css" >
	<link href="resources/css/style.css" rel="stylesheet">
		
	<!-- favorite and touch icons -->
	<link rel="shortcut icon" href="resources/images/favicon.png">
	<link rel="apple-touch-icon" href="resources/images/app_icons512x512.png">
	<link rel="apple-touch-icon" sizes="57x57" href="resources/images/app_icons57x57.png">
	<link rel="apple-touch-icon" sizes="72x72" href="resources/images/app_icons72x72.png">
	<link rel="apple-touch-icon" sizes="114x114" href="resources/images/app_icons114x114.png">
		
	<script src="resources/js/external/jquery-1.7.1.min.js"></script>
	<script src="resources/js/external/jquery-ui-1.8.10.custom.min.js"></script>
	<script src="resources/js/external/jquery.ui.datepicker2-sv.js"></script>
	<script src="resources/js/external/jquery.flot.min.js"></script>
	<script src="resources/js/external/jquery.flot.resize.min.js"></script>
</head>

<body>

	<div class="navbar navbar-inverse navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="brand" href="count"><img src="resources/images/favicon.png" /> Statistik</a>
				<ul class="nav">
					<li><a href="count">Antal</a></li>
					<li class="active"><a href="variation">Variation</a></li>
				</ul>
			</div>
		</div>
	</div>

	<div class="container">
	<section id="interval" class="well">
			<div class="row-fluid">
				<form class="form-inline" method="POST">
					<div class="span2">
						<a href="#" onclick="{ document.forms[0].action = '';document.forms[0].submit();return false;}" class="btn btn-success"><i class="icon-refresh icon-white"></i> Uppdatera</a>
					</div>
					<div class="span2">
						<label class="control-label" for="fromDate">Från:</label>
						<div class="input-append">
							<input class="input-small" name="fromDate" id="fromDate" type="text" value="<c:out value="${fromDate}"/>">
							<button class="btn" id="fromDateBtn" type="button"><i class="icon-calendar"></i></button>
						</div>
					</div>
					<div class="span2">
						<label class="control-label" for="toDate">Till:</label>
						<div class="input-append">
							<input class="input-small" name="toDate" id="toDate" type="text" value="<c:out value="${toDate}"/>">
							<button class="btn" id="toDateBtn" type="button"><i class="icon-calendar"></i></button>
						</div>
					</div>
					<div class="span3">
						<label class="control-label" for="caseTypeId">Ärendetyp:</label> 
						<select name="caseTypeId" class="input-medium">
							<c:forEach var="caseType" items="${caseTypes}">
								<option <c:if test="${caseTypeId == caseType.id}">selected="selected"</c:if> value="<c:out value="${caseType.id}"/>">
									<c:out value="${caseType.name}" />
								</option>
							</c:forEach>
						</select>
					</div>
					<div class="span3">
						<div class="pull-right">
							<a href="#download"
								onclick="{ document.forms[0].action = 'download';document.forms[0].submit();return false;}"
								class="btn"><i class="icon-download"></i>Excel</a>
						</div>
					</div>
				</form>
			</div>
		</section>
			

		<section id="summary">
			<div class="page-header">
				<h2>Variation</h2>
			</div>

			<div class="row">
				<div class="span12">
					<h4 class="diagram-legend">Timmar</h4>
					<div id="graph-container" style="width: 100%; height: 400px;"></div>
					<div class="pull-right">
						<h4>Datum</h4>
					</div>
				</div>
			</div>
		</section>


		<footer class="footer">
			<p>Powered by Streamflow</p>
		</footer>

	</div>
	<!-- /container -->
	<script type="text/javascript">
		$(function() {
		    $( "#fromDate" ).datepicker({
		      numberOfMonths: 2
		    });
		    $( "#fromDateBtn" ).click(function() {
				$( "#fromDate" ).datepicker("show");
			});
			
		    $( "#toDate" ).datepicker({
		      numberOfMonths: 2
		    });
		    $( "#toDateBtn" ).click(function() {
				$( "#toDate" ).datepicker("show");
			});
		  });
		  
		$(function() {
			var series = [
					<c:forEach var="scatteredValue" items="${result}" >[<c:out value="${scatteredValue.xAxis}" />,
							<c:out value="${scatteredValue.yAxis}" />], </c:forEach> ];

			$.plot($("#graph-container"), [ series ], {
				xaxis : {
					mode : "time"
				},
				series : {
					lines : {
						show : true
					},
					points : {
						show : true
					}
				}
			});
		});
	</script>


</body>
</html>