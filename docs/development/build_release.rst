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
        For sign you need define properties in the `./webstart/certificate.properties` and **VALID certificate**

For production and proper releases
----------------------------------

#. Perform required actions at CVS(Github)

    If there are uncommitted changes:

    .. code-block:: terminal

        git status

    And if there are uncommitted changes:

    .. code-block:: terminal

        git push origin develop

    Create a new git branch for the release preparations:

    .. code-block:: terminal

        git checkout -b release-<version> develop

    .. note::
        For patch releases draw the the release branch from master!

        .. code-block:: terminal

            git checkout -b release-<version> master

        and you might need to change the pom version from a feature version to a patch version ( Optional )
        After check validity of changed poms and if right.


    If you made any changes to pom's you need

    .. code-block:: terminal

        git commit -a -m "Bumped version number to x.x.x-SNAPSHOT"

    .. note::
        If you are working on a patch release don't forget to set the new development version to the version the develop branch is in! (Prevents merge conflicts after the release!)

        If the release build breaks before checking in the changed poms, just re-run it. The release.properties file keeps track of where it was.

#. Prepare codesign storage

    * If you haven't one create new. For example and details you can follow https://docs.oracle.com/javase/tutorial/security/sigcert/index.html or  https://docs.oracle.com/javase/tutorial/security/sigcert/index.html.

    * Add your certificate to your store. You can do that with command

        .. code-block:: terminal

            keytool -import -alias codesigncert -keystore <cacerts> -file <cert.pem>


    * Update properties at **./webstart/certificate.properties**

    .. note::
        Signing code is only used for swing web start client. So you can freely build and use server part only.

#. Deploy build to repository

    * To **/snapshots** repository. You should have **SNAPSHOT** version:

        .. code-block:: terminal

            mvn clean deploy -P sign

        or if you want to define custom *settings.xml*:

        .. code-block:: terminal

            mvn -s ../setings.xml clean deploy -P sign

        .. note::
            ../settings.xml there is a path to settings.xml file.

            For example like this:

            .. code-block:: xml

                <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
                          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                                          http://maven.apache.org/xsd/settings-1.0.0.xsd">
                    <servers>
                        <server>
                            <id>repositoryId</id>
                            <username>username</username>
                            <password>password</password>
                        </server>
                    </servers>
                </settings>


    * To **/pre-releases** repository the same way as for **/snapshots**.

        You should pre release `stable` version (`alpha`, `beta`, `M`, `RC`).

    * To **/releases** repository. You should have tested release **stable** version:

        .. code-block:: terminal

            mvn clean deploy -P sign,release

#. Finish release at CVS(Github)

    After the successful release build we have to push the last changes to the release branch and merge the results into both master and develop branch.

    .. code-block:: terminal

        git push origin release-<version>

        git checkout master

        git pull origin master

        git merge release-<version>

        git tag -a streamflow-<version>

        git push origin master

        git checkout develop

        git pull origin develop

        git merge release-<version>

        git push origin develop


    .. important::
        Be sure that there are no **.StreamflowServer*** folders before deploying, in another case you will get lock error, in case of them remove and restart server.


Building WebForms
-----------------

If you are releasing Streamflow WebForms you have to do the assembly your self

    .. code-block:: terminal

        cd /target/checkout
        mvn assembly:assembly

    The zip-fil is located in the target folder.


