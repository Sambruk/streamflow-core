<%--


    Copyright 2009-2012 Jayway Products AB

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
	<script src="resources/js/labels.js"></script>
</head>

<body data-spy="scroll" data-target=".subnav" data-offset="60">
	<div class="navbar navbar-inverse navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="brand" href="count"><img src="resources/images/favicon.png" />Statistik</a>
				<ul class="nav">
					<li class="active"><a href="count">Antal</a></li>
					<li><a href="variation">Variation</a></li>
				</ul>
			</div>
		</div>
	</div>
	<div class="subnav subnav-fixed">
		<div class="container">
			<ul class="nav nav-pills">
				<li class="active"><a href="#summary">Summering</a></li>
				<li><a href="#topou">Huvudenhet</a></li>
				<li><a href="#owner">Ägare</a></li>
				<li><a href="#casetype">Ärendetyp</a></li>
			</ul>
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
						<label class="control-label" for="perodicity">Period:</label> 
						<select name="periodicity" class="input-medium">
							<option <c:if test="${periodicity == 'monthly'}">selected="selected"</c:if> value="monthly">Månadsvis</option>
							<option <c:if test="${periodicity == 'weekly'}">selected="selected"</c:if> value="weekly">Veckovis</option>
							<option <c:if test="${periodicity == 'yearly'}">selected="selected"</c:if> value="yearly">Årsvis</option>
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
				<h2>Summering</h2>
			</div>

			<div class="row">
				<div class="span12">
					<table class="table table-condensed table-striped table-stats">
						<thead>
							<tr>
								<th></th>
								<th>Totalt</th>
								<c:forEach var="period" items="${periods}">
									<th><c:out value="${period}" /></th>
								</c:forEach>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="caseCount" items="${result.caseCountSummary}">

								<tr>
									<td class="stats-label"><c:out value="${caseCount.name}" /></td>
									<td><c:out value="${caseCount.total}" /></td>
									<c:forEach var="period" items="${caseCount.values}">
										<td><c:out value="${period.count}" /></td>
									</c:forEach>
								</tr>

							</c:forEach>
						</tbody>
					</table>
				</div>
			</div>
		</section>

		<section id="topou">
			<div class="page-header">
				<h2>
					Huvudenhet <small>Ärenden fördelade på översta
						organisatorisk enhet som ärendetypen tillhör</small>
				</h2>
			</div>
			<div class="row">
				<div class="span12">
					<table class="table table-condensed table-striped table-stats">
						<thead>
							<tr>
								<th></th>
								<th>Totalt</th>
								<c:forEach var="period" items="${periods}">
									<th><c:out value="${period}" /></th>
								</c:forEach>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="caseCount"
								items="${result.caseCountByTopOuOwner}" varStatus="status">

								<tr <c:if test="${status.last}">class="stats-summary"</c:if>>
									<td class="stats-label"><c:out value="${caseCount.name}" /></td>
									<td><c:out value="${caseCount.total}" /></td>
									<c:forEach var="period" items="${caseCount.values}">
										<td><c:out value="${period.count}" /></td>
									</c:forEach>
								</tr>

							</c:forEach>
						</tbody>
					</table>
				</div>
			</div>
		</section>

		<section id="owner">
			<div class="page-header">
				<h2>
					Ägare <small>Ärenden fördelade på enhet som ärendetypen
						tillhör</small>
				</h2>
			</div>

			<div class="row">
				<div class="span12">
					<table class="table table-condensed table-striped table-stats">
						<thead>
							<tr>
								<th></th>
								<th>Totalt</th>
								<c:forEach var="period" items="${periods}">
									<th><c:out value="${period}" /></th>
								</c:forEach>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="caseCount" items="${result.caseCountByOuOwner}"
								varStatus="status">

								<tr <c:if test="${status.last}">class="stats-summary"</c:if>>
									<td class="stats-label"><c:out value="${caseCount.name}" /></td>
									<td><c:out value="${caseCount.total}" /></td>
									<c:forEach var="period" items="${caseCount.values}">
										<td><c:out value="${period.count}" /></td>
									</c:forEach>
								</tr>

							</c:forEach>
						</tbody>
					</table>
				</div>
			</div>
		</section>

		<section id="casetyp">
			<div class="page-header">
				<h2>
					Ärendetyp och etiketter <small>Ärenden fördelade på
						ärendetyp och etiketter</small>
				</h2>
			</div>

			<div class="row">
				<div class="span12">
					<table class="table table-condensed table-striped table-stats">
						<thead>
							<tr>
								<th class="stats-label"><span class="expand-all-labels"
									title="Visa alla etiketter">+</span></th>
								<th class="stats-label">Ärendetyp</th>
								<th>Totalt</th>
								<c:forEach var="period" items="${periods}">
									<th><c:out value="${period}" /></th>
								</c:forEach>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="caseCount" items="${result.caseCountByCaseType}"
								varStatus="status">
								
								<c:choose>
      								<c:when test="${status.last}">
									<tr class="casetype-stats stats-summary">
										<td></td>
										<td class="stats-label" title="Ärendetyp"><c:out value="${caseCount.name}" /></td>
										<td><c:out value="${caseCount.total}" /></td>
										<c:forEach var="period" items="${caseCount.values}">
										<td><c:out value="${period.count}" /></td>
										</c:forEach>
									</tr>
									</c:when>
									<c:otherwise>
										<tr class="casetype-stats">
											<td class="table-stats-plus">
												<c:if test="${not empty result.caseCountByLabelPerCaseType[caseCount.name]}">
													<span class="expand-labels" title="Visa etiketter">+</span>
												</c:if>
											</td>
											<td class="stats-label" title="Ärendetyp"><c:out value="${caseCount.name}" /></td>
											<td><c:out value="${caseCount.total}" /></td>
											<c:forEach var="period" items="${caseCount.values}">
											<td><c:out value="${period.count}" /></td>
											</c:forEach>
										</tr>
										<c:forEach var="labelCaseCount"
											items="${result.caseCountByLabelPerCaseType[caseCount.name]}">
		
											<tr class="label-stats" style="display: none;">
												<td></td>
												<td class="stats-label" title="Etikett"><c:out
														value="${labelCaseCount.name}" /></td>
												<td><c:out value="${labelCaseCount.total}" /></td>
												<c:forEach var="period" items="${labelCaseCount.values}">
													<td><c:out value="${period.count}" /></td>
												</c:forEach>
											</tr>
										</c:forEach>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</div>
		</section>

		<footer class="footer">
			<p>Powered by Streamflow</p>
		</footer>

	</div>
	<!-- /container -->

	<script src="resources/js/external/bootstrap-scrollspy.js"></script>
	<script>
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
  </script>
</body>


</html>
