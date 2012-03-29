package se.streamsource.streamflow.client.ui.administration.casepriorities;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.util.DefinitionListModel;
import org.restlet.data.Form;

/**
 * Model for fetching case priorities.
 */
public class CasePrioritiesModel
      extends DefinitionListModel
{
   public CasePrioritiesModel( )
   {
      super( "create" );

      relationModelMapping("priority", CasePriorityModel.class);
   }

   public void remove( LinkValue link)
   {
      client.getClient( link ).delete();
   }

   public void up( LinkValue selected )
   {
      Form form = new Form();
      form.set( "index", selected.id().get() );
      
      client.postCommand( "up", form );
   }

   public void down( LinkValue selected )
   {
      Form form = new Form();
      form.set( "index", selected.id().get() );

      client.postCommand( "down", form );
   }
}
