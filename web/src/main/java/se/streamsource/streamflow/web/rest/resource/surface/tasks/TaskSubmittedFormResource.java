package se.streamsource.streamflow.web.rest.resource.surface.tasks;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.surface.tasks.TaskSubmittedFormSummaryContext;

/**
 *
 * */
public class TaskSubmittedFormResource
      extends CommandQueryResource
{

   @SubResource
   public void summary()
   {
      subResourceContexts( TaskSubmittedFormSummaryContext.class );
   }

}
