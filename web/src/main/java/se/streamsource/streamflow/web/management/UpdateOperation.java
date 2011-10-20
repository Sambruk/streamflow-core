package se.streamsource.streamflow.web.management;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;

/**
 * Implementation of an update operation that is to be executed
 * when the app is update to a new version.
 */
public interface UpdateOperation
{
   void update(Application app, Module module)
      throws Exception;
}
