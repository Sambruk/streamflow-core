package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.BasicEventList;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;

/**
 * A general super class for models that use LinkValue lists shown in a JList.
 * This class simplifies the list update for changedDescription events.
 */
public class LinkValueListModel
   implements EventVisitor
{

   protected EventVisitorFilter eventFilter = new EventVisitorFilter(this, "changedDescription" );
   protected BasicEventList<LinkValue> linkValues = new BasicEventList<LinkValue>();

   public boolean visit( DomainEvent event )
   {
      boolean success = false;
      LinkValue updated = getLinkValue( event );
      if( updated != null )
      {
         int idx = linkValues.indexOf( updated );
         ValueBuilder<LinkValue> valueBuilder = updated.buildWith();
         updated = valueBuilder.prototype();

         String eventName = event.name().get();
         if (eventName.equals( "changedDescription" ))
         {
            try
            {
               String newDesc = EventParameters.getParameter( event, "param1" );
               updated.text().set( newDesc );
               linkValues.set( idx, valueBuilder.newInstance() );
               success = true;
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
      return success;
   }

   private LinkValue getLinkValue( DomainEvent event)
   {
      if( linkValues == null )
         return null;

      for (LinkValue link : linkValues)
      {
         if( link.id().get().equals( event.entity().get() ))
         {
            return link;
         }

      }
      return null;
   }
}
