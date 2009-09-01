<%@page import="java.io.InputStream,java.util.Properties,info.aduna.io.IOUtil" %>

<%
InputStream is = getClass().getResourceAsStream("/version.properties");
Properties p = IOUtil.readProperties(is);
pageContext.setAttribute("application_version", p.getProperty("application.version"));
pageContext.setAttribute("application_buildKey", p.getProperty("application.buildKey"));
pageContext.setAttribute("application_buildNumber", p.getProperty("application.buildNumber"));
pageContext.setAttribute("application_revision", p.getProperty("application.revision"));
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <title>StreamFlow client</title>
  <link rel="stylesheet" type="text/css" href="css/stylesheet.css" />
</head>

<body>

<ul class="home-download all_os ">
 <li class="all_os"> <a class="download-link download-streamflow" href="webstart/streamflow.jnlp"> <span><strong>Ladda ner StreamFlow</strong> <em>${application_version} f&ouml;r alla operativsystem</em> <em class="download-lang">Build: ${application_buildKey} ${application_buildNumber} Revision: ${application_revision}</em></span></a> </li>
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