<%--


    Copyright 2009-2010 Streamsource AB

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

<%@page import="java.io.InputStream,java.util.Properties" %>

<%
    InputStream is = getClass().getResourceAsStream("/version.properties");
    Properties p = new Properties();
    p.load(is);
    is.close();
    pageContext.setAttribute("application_version", p.getProperty("application.version"));
    pageContext.setAttribute("application_buildKey", p.getProperty("application.buildKey"));
    pageContext.setAttribute("application_buildNumber", p.getProperty("application.buildNumber"));
    pageContext.setAttribute("application_revision", p.getProperty("application.revision"));
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Streamflow&#0153; Client</title>
    <link rel="stylesheet" type="text/css" href="css/stylesheet.css"/>
</head>

<body>

<ul class="home-download all_os ">
    <li class="all_os">
        <a class="download-link download-streamflow" href="webstart/streamflow.jnlp">
 		<span><strong>Ladda ner Streamflow&#0153;</strong> 
 		<em>${application_version}</em> 
 		<em class="download-lang">F&ouml;r alla operativsystem</em></span>
        </a>
    </li>
</ul>

<!--<a href="webstart/streamflow.jnlp">StreamFlow client</a><br />-->

<div id="foot">
    <ul id="versioninfo">
        <li>Version: ${application_version}</li>
        <li>Build: ${application_buildKey} ${application_buildNumber}</li>
        <li>Revision: ${application_revision}</li>
    </ul>
</div>

</body>
</html>