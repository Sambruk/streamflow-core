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

    .. note::
        You can add system service later manually. To do that go to **%Glassfish home directory%\bin** and then run asadmin.exe and execute following command

        .. code-block:: console

            create-service --name %Your service name%

        Also you can check additional options at https://docs.oracle.com/cd/E19798-01/821-1758/create-service-1/index.html

#. Go inside to following location under installation folder. **glassfish/domains/%Domain name%/config/** and change **domain.xml** file and add following lines
    .. code-block:: xml

        <jvm-options>-Djavax.net.ssl.keyStorePassword=changeit</jvm-options>
        <jvm-options>-Djavax.net.ssl.trustStorePassword=changeit</jvm-options>

   to java-config section  (There are two of them) you can find iy quickly by huge amount of other jvm-options


#. If SSL was configured tell Glassfish that Apache acts as a SSL-terminating proxy server.

    In the Admin Console go to
    Network Config - Network Listeners - http-listener-1 - Tab HTTP
    Enable Auth Pass Through

.. important::
    In order to fix possible *connections reset* and *500* especially for webclient it's recommended to increase **max-thread-pool** size for http
    In case of Glassfish it can be made by next param
        .. code-block:: xml

            <thread-pool name="http-thread-pool" max-thread-pool-size="200"/>

    Put needed value (Maybe it should be lower than 200)

.. _local-files-label-reference:

Local files
^^^^^^^^^^^
Streamflow database folder and configuration entries in Java Preferences(windows registry) are tight coupled to the user who is running Glassfish. Can be edited with :doc:`../administration/manager`

If Glassfish is run as a windows service with the system default user the database folder ends up in

    .. note::
        varies if run with 32bit or 64bit - SysWOW64 or System32

    ``C:\Windows\System32\config\systemprofile\Application Data``

It is possible to run Glassfish with its own user that has the *userprofile* location moved to another location - i.e. D:

    #. Create a new Standard User with no password expiration and user may not change password.

    #. If Glassfish was installed by an administrator the new user will need to be part of the administrators group to be able to run Glassfish.
    #. Move the profile to the new location ( D: ) and create a link

        .. code-block:: console

            mklink /D C:\Users\<username>  D:\<username>

        .. hint::
            (where ever you moved the profile to)

    #. Go to the Glassfish windows service and change the user in LogOn tab to the newly created user.

    #. Start the Glassfish service again to create the Streamflow database and the configuration location in the registry for the new user.

If you need to move an old database to the new location just replace the StreamflowServer folder inside the new Application Data with the one from the old location.

Configuration preferences are a little more tricky. The best way might be to export the old entries change the first part of the path to the location for the new user, change the location entry for the database files in the **data** key to represent the new database file location and then import the entries into the registry.


Ubuntu + Java 7 + Tomcat
------------------------

Java setup
^^^^^^^^^^
Install Java
    .. code-block:: terminal

        sudo add-apt-repository ppa:webupd8team/java
        sudo apt-get update
        sudo apt-get install oracle-java7-installer
        sudo apt-get install oracle-java7-set-default

Apache setup
^^^^^^^^^^^^
#. Install apache
    .. code-block:: terminal

        sudo apt-get install apache2

        a2enmod proxy
        a2enmod proxy_http

#. Edit default site configuration to enable proxy located at file **/etc/apache2/sites-available/default**

    There should be following content

    .. code-block:: xml

        NameVirtualHost *:80
        <VirtualHost *:80>
                ServerAdmin support@streamsource.se

                DocumentRoot /var/www
                <Directory />
                        Options FollowSymLinks
                        AllowOverride None
                </Directory>
                <Directory /var/www/>
                        Options Indexes FollowSymLinks MultiViews
                        AllowOverride None
                        Order allow,deny
                        allow from all
                </Directory>

                ScriptAlias /cgi-bin/ /usr/lib/cgi-bin/
                <Directory "/usr/lib/cgi-bin">
                        AllowOverride None
                        Options +ExecCGI -MultiViews +SymLinksIfOwnerMatch
                        Order allow,deny
                        Allow from all
                </Directory>

                ErrorLog ${APACHE_LOG_DIR}/error.log

                # Possible values include: debug, info, notice, warn, error, crit,
                # alert, emerg.
                LogLevel warn

                CustomLog ${APACHE_LOG_DIR}/access.log combined

            Alias /doc/ "/usr/share/doc/"
            <Directory "/usr/share/doc/">
                Options Indexes MultiViews FollowSymLinks
                AllowOverride None
                Order deny,allow
                Deny from all
                Allow from 127.0.0.0/255.0.0.0 ::1/128
            </Directory>

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

        </VirtualHost>

#. And change **/etc/apache2/ports.conf**. Comment out following lines

    .. code-block:: configuration

        #NameVirtualHost *:80



#. Configure SSL if needed
    .. note::
        Ubuntu - check that ssl-cert - OpenSSL wrapper is already installed
        For Development or Test servers create a self signed certificate

    .. code-block:: terminal

        sudo mkdir /etc/apache2/ssl
        sudo make-ssl-cert /usr/share/ssl-cert/ssleay.cnf /etc/apache2/ssl/apache.pem

    Enable SSL on apache2

    .. code-block:: terminal

        sudo a2enmod ssl
        sudo a2enmod rewrite
        sudo a2enmod headers

        sudo /etc/init.d/apache2 force-reload

    Copy default virtual host config

    .. code-block:: terminal

        sudo cp /etc/apache2/sites-available/default /etc/apache2/sites-available/ssl

    Edit the new file by replacing the content with:

    .. code-block:: configuration

        NameVirtualHost *:443
        <VirtualHost *:443>
           ServerAdmin support@streamsource.se
           ServerName test.sf.streamsource.se

           # if not specified, the global error log is used
           ErrorLog ${APACHE_LOG_DIR}/error.log
           CustomLog ${APACHE_LOG_DIR}/access.log combined

           # Avoid open your server to proxying
           ProxyRequests Off
           #ProxyPreserveHost On
           ProxyVia On

           # SSL
           SSLEngine on
           SSLProxyEngine On
           SSLCertificateFile /etc/apache2/ssl/apache.pem

           # Let apache correctly rewrite redirect

           ProxyPass / http://localhost:8080/
           #ProxyPass /streamflow/ http://localhost:8080/streamflow/
           #ProxyPass /surface/ http://localhost:8080/surface/
           #ProxyPass /client/ http://localhost:8080/client/
           #ProxyPassReverse / http://localhost:8080/
           #ProxyPassReverse /streamflow/ http://localhost:8080/streamflow/
           #ProxyPassReverse /surface/ http://localhost:8080/surface/
           #ProxyPassReverse /client/ http://localhost:8080/client/

           # don't lose time with IP address lookups
           HostnameLookups Off

          ProxyPreserveHost     on
          RewriteEngine         on

          RequestHeader Set Proxy-keysize 512
          RequestHeader Set Proxy-ip %{REMOTE_ADDR}e
          RequestHeader Set Host test.sf.streamsource.se

          RewriteRule ^/streamflow$ /streamflow/ [R,L]
          RewriteRule ^/streamflow/(.*) http://localhost:8080/streamflow/$1 [P,L]

          RewriteRule ^/client$ /client/ [R,L]
          RewriteRule ^/client/(.*) http://localhost:8080/client/$1 [P,L]

          RewriteRule ^/surface$ /surface/ [R,L]
          RewriteRule ^/surface/(.*) http://localhost:8080/surface/$1 [P,L]

           # configures the footer on server-generated documents
           #ServerSignature On
        </VirtualHost>

    Enable the new site with

    .. code-block:: terminal

        sudo a2ensite ssl
        sudo /etc/init.d/apache2 reload
        sudo service apache2 restart


Tomcat setup
^^^^^^^^^^^^

#. Install tomcat

    .. code-block:: terminal

        sudo apt-get install tomcat8 tomcat8-admin

#. Edit default tomcat startup script located at **/etc/default/tomcat8** and disable java security

    .. code-block:: config

        TOMCAT8_SECURITY=no


#. Edit Tomcat **/etc/tomcat8/server.xml** in order to enable the AJP connector. Define AJP connector for communication between Tomcat and Apache:

    .. code-block:: xml

        <!-- Define an AJP 1.3 Connector on port 8009 -->
        <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />

#. Edit the **/etc/tomcat6/tomcat-users.xml** file in order to enable the manager user and add the manager user:

    .. code-block:: xml

        <tomcat-users>
            <role rolename="manager"/>
            <user username="streamflow" password="j0hnd0e" roles="manager"/>
        </tomcat-users>

#. Make tomcat6 owner of the files:
    .. code-block:: terminal

        chown -R tomcat8:tomcat8 ~tomcat8

#. Restart tomcat:
    .. code-block:: terminal

        service tomcat8 restart

#. Configure ajp-proxy for Apache and Tomcat
    .. code-block:: terminal

        a2enmod proxy_ajp

#. Edit proxy configuration at **/etc/apache2/mods-enabled/proxy.conf**. The file should look like this:

    .. code-block:: configuration

        <IfModule mod_proxy.c>
                #turning ProxyRequests on and allowing proxying from all may allow
                #spammers to use your proxy to send email.

                ProxyRequests Off
            ProxyPreserveHost On

                <Proxy *>
                        AddDefaultCharset off
                        Order deny,allow
                        #Deny from all
                        #Allow from .example.com
                </Proxy>

                # Enable/disable the handling of HTTP/1.1 "Via:" headers.
                # ("Full" adds the server version; "Block" removes all outgoing Via: headers)
                # Set to one of: Off | On | Full | Block

                ProxyVia On
            ProxyPass /streamflow/ ajp://localhost:8009/streamflow/
            ProxyPass /manager/ ajp://localhost:8009/manager/
            ProxyPassReverse /streamflow/ ajp://localhost:8009/streamflow/
            ProxyPassReverse /manager/ ajp://localhost:8009/manager/

            RedirectMatch ^/streamflow$ /streamflow/
            RedirectMatch ^/manager$ /manager/
        </IfModule>

#. Restart Apache:

    .. code-block:: terminal

        service apache2 restart1


.. important::
    In order to fix possible *connections reset* and *500* especially for webclient it's recommended to increase **max-thread-pool** size for http
    In case of Tomcat it can be made by next param
        .. code-block:: xml

            <connector connectiontimeout="20000"
                       maxthreads="200"
                       port="8080"
                       protocol="HTTP/1.1"
                       redirectport="8443" />

    Put needed value (Maybe it should be lower than 200)

SQL Server
----------

To setup a connection to a SQLServer you need to:

    #. Download the **sql-driver** from `Microsoft  http://www.microsoft.com/en-us/download/details.aspx?displaylang=en&id=11774`_.
    #. Unzip and copy **sqljdbc4.jar** to application folder library location

        * Tomcat
            `$CATALINA_HOME/lib`

        * Glassfish
            `../glassfish/domains/<domain>/lib/ext`

    #. Setup the datasource in Streamflow using :doc:`../administration/manager` the following information.

        **driver**  com.microsoft.sqlserver.jdbc.SQLServerDataSource
        **url** jdbc:sqlserver://<hostname>:1433;databaseName=<databasename>

