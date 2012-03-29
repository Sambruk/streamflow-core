package se.streamsource.streamflow.client.ui.administration.casepriorities;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.administration.priority.CasePriorityValue;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * Model containing priority info
 */
public class CasePriorityModel 
   extends ResourceModel<CasePriorityValue>
{
   @Uses
   private CommandQueryClient client;

   public void change( CasePriorityValue priority)
   {
      if(!getIndex().equals( priority ))
         client.postCommand( "change", priority );
   }
}
