Environment setup
============

Windows Server 2012 R2 + Java 7 + Glassfish 3.1.2.2
--------------

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




