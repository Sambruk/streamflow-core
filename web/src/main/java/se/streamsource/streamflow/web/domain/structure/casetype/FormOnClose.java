package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.form.Form;

/**
 * Holds the reference to a form that has to be filled in and submitted prior
 * to be able to close a case that has a certain case type.
 */
@Mixins(FormOnClose.Mixin.class)
public interface FormOnClose
{
   void changeFormOnClose( @Optional Form form);

   interface Data
   {
      @Optional
      Association<Form> formOnClose();
   }

   interface Events
   {
      void changedFormOnClose( @Optional DomainEvent event, @Optional Form form );
   }

   abstract class Mixin
      implements FormOnClose, Events
   {
      @This
      Data data;

      public void changeFormOnClose( Form form )
      {

         changedFormOnClose( null, form );
      }

      public void changedFormOnClose( DomainEvent event, Form form )
      {

         data.formOnClose().set( form );
      }
   }
}
