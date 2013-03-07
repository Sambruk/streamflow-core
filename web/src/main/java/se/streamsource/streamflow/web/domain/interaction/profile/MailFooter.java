package se.streamsource.streamflow.web.domain.interaction.profile;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 *
 */
@Mixins(MailFooter.Mixin.class)
public interface MailFooter
{
   void changeMailFooter( String footer );

   interface Data
   {
      @Optional
      @UseDefaults
      Property<String> footer();

      void changedMailFooter( @Optional DomainEvent event, String footer );
   }

   abstract class Mixin
      implements MailFooter, Data
   {
      @This
      Data data;

      public void changeMailFooter( String footer )
      {
         if( !footer.equals( data.footer().get() ))
         {
            data.changedMailFooter( null, footer );
         }
      }

      public void changedMailFooter( @Optional DomainEvent event, String footer )
      {
         data.footer().set( footer );
      }
   }
}
