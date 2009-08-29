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
</head>
<body>
<a href="webstart/streamflow.jnlp">StreamFlow client</a><br />
Version: ${application_version}<br >
Build: ${application_buildKey} ${application_buildNumber}<br />
Revision: ${application_revision}<br />
</body>
</html>
        