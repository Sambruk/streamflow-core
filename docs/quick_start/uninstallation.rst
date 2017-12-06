Uninstall of previous version
=============================

Glassfish
*********
#. Go to administration console of Glassfish by default *http://localhost:4848*
#. After log in at the left-hand menu, select "Applications"
#. Then click the box in front of **streamflow-x.x** and select **Undeploy** from the menu. A dialog box opens where you must confirm that you want to uninstall ("Undeploy") application.
#. Once Streamflow is uninstalled, the list of installed applications will be displayed again.
#. Before moving on and installing the new version recommended an extra backup of the whole The StreamflowServer directory is made. See :doc:`backup_data`
#. After the old Streamflow version has been uninstalled, the GlassFish service must be started
#. If it is a Windows server used then open the "Services" at Task manager. There you can look up the service and restart it.

Tomcat
******
#. Go to management tool of Tomcat by default *http://localhost:8080/manager/html*
#. Then click the box in front of **streamflow-{module}-x.x** and press **Undeploy** from the right of needed application. A dialog box opens where you must confirm that you want to uninstall ("Undeploy") application.
    .. hint::
        Actual streamflow module names are:
            * streamflow-statistic-x.x
            * streamflow-war-x.x
            * streamflow-web-x.x

    .. warning::
        In theory you don't need to uninstall each package. But it's recommended to uninstall all of them.

#. Once Streamflow is uninstalled, the list of installed applications will be displayed again.
#. Before moving on and installing the new version recommended an extra backup of the whole The StreamflowServer directory is made. See :doc:`backup_data`
#. After the old Streamflow version has been uninstalled, the Tomcat service must be started
#. If it is a Windows server used then open the "Services" at Task manager. There you can look up the service and restart it.