Installation
############


Server part + swing client + statistics
---------------------------------------

Glassfish
=========
#. Go to administration console of Glassfish. By default *http://localhost:4848*

#. After log in at the left-hand menu, select "Applications".
#. Make sure that the application you are using do not install(Deploy) already exists in the list of installed.
#. Then click on the "Deploy" button.
#. Click on "Choose File" and select the ear file you thought down and unpacked from the zip file. It is called streamflow-x.x.ear.
#. Click "Ok" in the top right corner or down the right corner. The button will be deactivated and displays the text "Processing". It may take a few minutes before the application has been installed.
#. When installation is complete, the list of installed applications will be displayed again.
#. Now sign out again

Tomcat
======
#. Go to manager application of Tomcat. By default *http://localhost:8080/manager/html*
#. Unzip, unpack the ear file and grab the war files.
#. Rename files with saving **.war** extension to:

    * streamflow-web to streamflow
    * streamflow-war to client
    * streamflow-statistic to statistic
#. Make sure that the application you are using do not install(Deploy) already exists in the list of installed. If already some press **Undeploy** button
#. Scroll down to *Deploy* section. Click on "Choose File" and select required war file and then click on the "Deploy" button.
#. Click "Ok" in the top right corner or down the right corner. The button will be deactivated and displays the text "Processing". It may take a few minutes before the application has been installed.
#. When installation is complete, the list of installed applications will be displayed again.
#. Now sign out again


DB Entity export
================
Starting version **1.29-beta3** there is new feature which allows to export all entities to relational DB system like Microsoft SQL only (for now)

#. First launch should trigger reindex automatically. But if now call it manually with VisualVM
#. After successful reindexing enable entity export. Example configuration can be found at :doc:`../administration/sql_export`
#. After needed to preform server restart.

.. important::
    If some problems occur refer to :doc:`../administration/sql_export` Pitfalls section