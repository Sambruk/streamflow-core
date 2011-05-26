package se.streamsource.streamflow.client.ui.administration.groups;

import se.streamsource.streamflow.client.ResourceModel;

/**
 * Represents a group in the administration model
 */
public class GroupModel
   extends ResourceModel
{
   public GroupModel()
   {
      relationModelMapping("resource", ParticipantsModel.class);
   }
}
