
To setup Jetty run the script `setup_server`, it will download jetty and setup
symbolic links to the proper files.

./setup_server.sh

After that you run the script `run.sh` to start the server.

./run.sh


### If you get a ClassCastException when running the server
E.g. :
java.lang.ClassCastException: $Proxy28 cannot be cast to se.streamsource.streamflow.web.infrastructure.plugin.ContactLookupServiceConfiguration
If you get an exception running the streamflow server? S:

Do this:
open the HOMEFOLDER/Library/Preferences/streamsource.streamflow.streamflowserver.plist

Edit the second value root->streamsource/streamflow->contactloockup->type to

se.streamsource.streamflow.web.infrastructure.plugin.ContactLookupServiceConfiguration
