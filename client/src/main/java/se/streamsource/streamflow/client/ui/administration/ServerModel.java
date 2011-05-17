package se.streamsource.streamflow.client.ui.administration;

import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.users.UsersAdministrationModel;

/**
 * This represents the server in the administration model
 */
public class ServerModel
   extends ResourceModel
{
   public ServerModel()
   {
      relationModelMapping("users", UsersAdministrationModel.class);
   }
}
