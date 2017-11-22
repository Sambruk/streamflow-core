Build
=====

For development purposes
------------------------

#. To install to local maven repository and build war/ear files

	.. code-block:: terminal

		mvn clean install

#. To install to local maven repository and prepare war/ear files with proper signing by owned certificate (You should have certificate file).

	.. code-block:: terminal

		mvn clean install -P sign

	.. note::
		For sign you need define properties in the `webstart/certificate.properties`.

For production and proper releases
------

1. To **/snapshots** repository. You should have **SNAPSHOT** version:

	.. code-block:: terminal

		mvn clean deploy -P sign

	or if you want to define custom *settings.xml*:

	.. code-block:: terminal

		mvn -s ../setings.xml clean deploy -P sign

	.. note::
		...settings.xml there is a path to settings.xml file.

2. To **/pre-releases** repository the same way as for **/snapshots**.
You should pre release `stable` version (`alpha`, `beta`, `M`, `RC`).

3. To **/releases** repository. You should have tested release **stable** version:

	.. code-block:: terminal

		mvn clean deploy -P sign,release


.. important::
	Be sure that there are no **.StreamflowServer*** folders before deploying, in another case you will get lock error, in case of them remove and restart server.


.. note::
	After deploying all urls are works but they dont include streamflow root path. They must look like * /streamflow/workspace/ instead of * /workspace/
