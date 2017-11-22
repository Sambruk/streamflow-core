Environment setup
=================

Windows Server 2012 R2 + Java 7 + Glassfish 3.1.2.2
---------------------------------------------------

Java setup
^^^^^^^^^^
#. Download latest jdk 7 from official Oracle site. For now can be accessed from Oracle archive http://www.oracle.com/technetwork/java/javase/archive-139210.html
#. Install downloaded file

#. Add **JAVA_HOME** and **JAVA_JRE** path variables to your system. open command line from Administrator and run

    .. code-block:: console

       setx -m JAVA_HOME "C:\Progra~1\Java\jdk1.7.0_XX"

    and

    .. code-block:: console

       setx -m JAVA_JRE "C:\Progra~1\Java\jre7"

    with setting up correct path to installation.

    .. note::
        You can do that by reading following tutorial https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html

Apache2 setup
^^^^^^^^^^^^^

#. Download Apache2 from http://www.apachelounge.com/download/
    .. hint::
        Don't forget to install the "Visual C++ Redistributable" that is linked from the download page.

#. Unzip the Apache24 folder to **C:/Apache24** (that is the ServerRoot in the config).
    Default folder for your your webpages is DocumentRoot **C:/Apache24/htdocs**

    When you unzip to an other location, change ServerRoot in the *httpd.conf* and change in *httpd.conf* the Documenroot, Directories, ScriptAlias also when you use the extra folder config file(s) change to your location there


#. Edit the httpd.conf file located in *<apache install folder>\conf* and uncomment following lines

    .. code-block:: xml

        LoadModule proxy_module modules/mod_proxy.so
        LoadModule proxy_http_module modules/mod_proxy_http.so
        LoadModule headers_module modules/mod_headers.so

    And add proxy configuration at the end of file

    .. code-block:: xml

        <IfModule proxy_module>
            # Add header to prevent CORS problem with running streamflow webclient
            Header set Access-Control-Allow-Origin "*"
            ProxyRequests Off
            ProxyPreserveHost On
            ProxyVia On

            # Let apache correctly rewrite redirect

            ProxyPass / http://localhost:8080/
            ProxyPass /streamflow/ http://localhost:8080/streamflow/
            ProxyPass /surface/ http://localhost:8080/surface/
            ProxyPassReverse / http://localhost:8080/
            ProxyPassReverse /streamflow/ http://localhost:8080/streamflow/
            ProxyPassReverse /surface/ http://localhost:8080/surface/

            # don't lose time with IP address lookups
            HostnameLookups Off
        </IfModule>


TLS setup
"""""""""

#. Edit the httpd.conf file located in <apache install folder>\conf
    Enable the following modules

    .. code-block:: xml

        LoadModule ssl_module modules/mod_ssl.so

    and enable the following configuration at the end of the file

    .. code-block:: xml

        # Secure (SSL/TLS) connections
        Include conf/extra/httpd-ssl.conf
        #
        # Note: The following must must be present to support
        #       starting without SSL on platforms with no /dev/random equivalent
        #       but a statically compiled-in mod_ssl.
        #
        <IfModule ssl_module>
            SSLRandomSeed startup builtin
            SSLRandomSeed connect builtin
        </IfModule>

#. Edit the httpd-ssl.conf file located in *<apache install folder>\conf\extra*

    .. code-block:: config

        ...
        ServerName streamflow.test.imcode.com:443
        ServerAdmin tech@imcode.com
        ...
        SSLCertificateFile "c:/Apache24/conf/<your cert>.crt" (or bundle)
        ...
        SSLCertificateKeyFile "c:/Apache24/conf/<your key>.key"
        ...

#. If you want to automatically redirect the user from http to https you need to enable the module mod_rewrite and add the following lines to your httpd.conf files. See http://www.sslshopper.com/apache-redirect-http-to-https.html

    .. code-block:: config

        RewriteEngine On
        RewriteCond %{HTTPS} off
        # No redirect for client download
        RewriteCond %{REQUEST_URI} !.*/client
        RewriteRule (.*) https://%{HTTP_HOST}%{REQUEST_URI}


#. Run as a service
    .. code-block:: console

        httpd.exe -k install -n "Apache HTTP Service"


Glassfish setup
^^^^^^^^^^^^^^^

#. Download latest Glassfish 3 (*3.1.2.2* for now) from http://www.oracle.com/technetwork/middleware/glassfish/downloads/ogs-3-1-1-downloads-439803.html

#. Launch downloaded file.

    .. important::
        If it complains about not installed java. Open console at administration mode and execute like

        .. code-block:: console

            osgi-3.1.2.2.exe -j "C:\Progra~1\Java\jre7"

#. During install choose custom installation -> Install and Configure.

    .. note::
       Don't install update tool. It's not gonna be update

#. Choose *Create server domain* and after input needed values
#. Also choose *Create Operating System service for domain*
#. Go inside to following location under installation folder. **glassfish/domains/%Domain name%/config/** and change **domain.xml** file and add following lines
    .. code-block:: xml

        <jvm-options>-Djavax.net.ssl.keyStorePassword=changeit</jvm-options>
        <jvm-options>-Djavax.net.ssl.trustStorePassword=changeit</jvm-options>

   to java-config section  (There are two of them) you can find iy quickly by huge amount of other jvm-options




