

wget http://repo2.maven.org/maven2/org/mortbay/jetty/jetty-runner/8.1.8.v20121106/jetty-runner-8.1.8.v20121106.jar

ln -s ../../web/target/streamflow-web-1.8.0-SNAPSHOT.war streamflow-web.war
ln -s jetty-runner-8.1.8.v20121106.jar jetty-runner.jar



### If you get a ClassCastException when running the server
E.g. :
java.lang.ClassCastException: $Proxy28 cannot be cast to se.streamsource.streamflow.web.infrastructure.plugin.ContactLookupServiceConfiguration
If you get an exception running the streamflow server? S:

Do this:
open the HOMEFOLDER/Library/Preferences/streamsource.streamflow.streamflowserver.plist

Edit the second value root->streamsource/streamflow->contactloockup->type to

se.streamsource.streamflow.web.infrastructure.plugin.ContactLookupServiceConfiguration