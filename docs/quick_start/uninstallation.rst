Uninstall of previous version
=============================

#. Go to administration console of Glassfish by default *http://localhost:4848*
#. After log in at the left-hand menu, select "Applications"
#. Then click the box in front of "streamflow-x.x" and select "Undeploy" from the menu. A dialog box opens where you must confirm that you want to uninstall ("Undeploy") application.
#. Once Streamflow is uninstalled, the list of installed applications will be displayed again.
#. Before moving on and installing the new version recommended an extra backup of the whole The StreamflowServer directory is made. See :doc:`backup_data`
#. After the old Streamflow version has been uninstalled, the GlassFish service must be started
#. If it is a Windows server used then open the "Services" at Task manager. There you can look up the service and restart it.