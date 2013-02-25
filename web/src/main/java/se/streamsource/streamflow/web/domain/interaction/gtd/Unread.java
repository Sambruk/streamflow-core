package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * This interface is used to determine if an implementor has been read( acknowledged ) or not.
 */
@Mixins(Unread.Mixin.class)
public interface Unread
{
   public void setUnread( boolean unread );
   public boolean isUnread();

   interface Data
   {
      @UseDefaults
      Property<Boolean> unread();

      void setUnread( @Optional DomainEvent event, boolean unread );
   }

   abstract class Mixin
      implements Unread, Data

   {
      @This
      Data data;

      public void setUnread( boolean unread )
      {
         setUnread( null, unread );
      }

      public boolean isUnread()
      {
         return data.unread().get();
      }

      public void setUnread( DomainEvent event, boolean unread )
      {
         data.unread().set( unread );
      }
   }
}
