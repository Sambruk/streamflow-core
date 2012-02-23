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
    <title>Surface Statistik</title>
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
    <link href="resources/css/style.css" rel="stylesheet">

    <!-- favorite and touch icons -->
	<link rel="shortcut icon" href="resources/images/favicon.png">
	<link rel="apple-touch-icon" href="resources/images/app_icons512x512.png">
	<link rel="apple-touch-icon" sizes="57x57" href="resources/images/app_icons57x57.png">
	<link rel="apple-touch-icon" sizes="72x72" href="resources/images/app_icons72x72.png">
	<link rel="apple-touch-icon" sizes="114x114" href="resources/images/app_icons114x114.png">
	
    <script type="text/javascript" src="resources/js/external/jquery-1.7.1.js"></script>
</head>

<body data-spy="scroll" data-target=".subnav" data-offset="85">

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <a class="brand" href="#">Streamflow Statistik</a>
            <ul class="nav">
                <li class="active"><a href="index.jsp">Antal</a></li>
                <li><a href="#">Variation</a></li>
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
    <div class="row">
        <div class="span10">
            <form class="form-inline" method="POST">
                <label class="control-label" for="fromDate">Från:</label>
                <input class="date focused" name="fromDate" type="date" value="<c:out value="${fromDate}"/>">
                <label class="control-label" for="toDate">Till:</label>
                <input class="date focused" name="toDate" type="date" value="<c:out value="${toDate}"/>">
                <label class="control-label" for="perodicity">Period:</label>
                <select name="periodicity" class="span2">
                    <option <c:if test="${periodicity == 'monthly'}">selected="selected"</c:if> value="monthly">Månadsvis</option>
                    <option <c:if test="${periodicity == 'weekly'}">selected="selected"</c:if>value="weekly">Veckovis</option>
                    <option <c:if test="${periodicity == 'yearly'}">selected="selected"</c:if>value="yearly">Årsvis</option>
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
                        <th><c:out value="${period}"/></th>
                    </c:forEach>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="casecount" items="${result.casecountSummary}" >

                    <tr>
                        <td class="stats-label"><c:out value="${casecount.name}" /></td>
                        <td><c:out value="${casecount.total}" /></td>
                        <c:forEach var="period" items="${casecount.values}">
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
        <h2>Huvudenhet <small>Ärenden fördelade på översta organisatorisk enhet som ärendetypen tillhör</small></h2>
    </div>
    <div class="row">
        <div class="span12">
            <table class="table table-condensed table-striped table-stats">
                <thead>
                <tr>
                    <th></th>
                    <th>Totalt</th>
                    <c:forEach var="period" items="${periods}">
                        <th><c:out value="${period}"/></th>
                    </c:forEach>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="casecount" items="${result.caseCountByTopOuOwner}" varStatus="status">

                    <tr <c:if test="${status.last}">class="stats-summary"</c:if> >
                        <td class="stats-label"><c:out value="${casecount.name}" /></td>
                        <td><c:out value="${casecount.total}" /></td>
                        <c:forEach var="period" items="${casecount.values}">
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
    <h2>Ägare <small>Ärenden fördelade på enhet som ärendetypen tillhör</small></h2>
</div>

<div class="row">
    <div class="span12">
        <table class="table table-condensed table-striped table-stats">
            <thead>
            <tr>
                <th></th>
                <th>Totalt</th>
                <c:forEach var="period" items="${periods}">
                    <th><c:out value="${period}"/></th>
                </c:forEach>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="casecount" items="${result.caseCountByOuOwner}" varStatus="status">

                <tr <c:if test="${status.last}">class="stats-summary"</c:if> >
                    <td class="stats-label"><c:out value="${casecount.name}" /></td>
                    <td><c:out value="${casecount.total}" /></td>
                    <c:forEach var="period" items="${casecount.values}">
                        <td><c:out value="${period.count}" /></td>
                    </c:forEach>
                </tr>

            </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<section id="casetyp">
    <div class="page-header">
        <h2>Ärendetypen <small>Ärenden fördelade på ärendetyp</small></h2>
    </div>

    <div class="row">
        <div class="span12">
            <table class="table table-condensed table-striped table-stats">
                <thead>
                <tr>
                    <th></th>
                    <th>Totalt</th>
                    <c:forEach var="period" items="${periods}">
                    <th><c:out value="${period}"/></th>
                    </c:forEach>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="casecount" items="${result.caseCountByCasetype}" varStatus="status">

                <tr <c:if test="${status.last}">class="stats-summary"</c:if> >
                    <td class="stats-label"><c:out value="${casecount.name}" /></td>
                    <td><c:out value="${casecount.total}" /></td>
                    <c:forEach var="period" items="${casecount.values}">
                    <td><c:out value="${period.count}" /></td>
                    </c:forEach>
                </tr>

                </c:forEach>

                </tbody>
            </table>
        </div>
    </div>
</section>

<footer class="footer">
    <p>Powered by Streamflow</p>
</footer>

</div><!-- /container -->

    <script src="resources/js/external/bootstrap-scrollspy.js"></script>
</body>
</html>