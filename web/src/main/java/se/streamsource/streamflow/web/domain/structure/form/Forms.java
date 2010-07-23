/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.interaction.gtd.ChangesOwner;

/**
 * JAVADOC
 */
@Mixins(Forms.Mixin.class)
public interface Forms
{
   Form createForm();

   @ChangesOwner
   void addForm( Form form );

   void removeForm( Form form );

   void moveForm( Form form, Forms toForms );

   interface Data
   {
      @Aggregated
      ManyAssociation<Form> forms();

      Form createdForm( DomainEvent event, String id);

      void addedForm( DomainEvent create, Form form );

      void removedForm( DomainEvent event, Form removedForm );

      Form getFormByName( String name );
   }

   abstract class Mixin
         implements Forms, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Data data;

      public Form createForm()
      {
         Form form = createdForm( DomainEvent.CREATE, idGen.generate( Identity.class ) );
         addForm(form);

         return form;
      }

      public void addForm( Form form )
      {
         if (data.forms().contains( form ))
            return;

         data.addedForm(DomainEvent.CREATE, form);
      }

      public void removeForm( Form form )
      {
         if (!data.forms().contains( form ))
            return;

         removedForm( DomainEvent.CREATE, form );
      }

      public void moveForm( Form form, Forms toForms )
      {
         toForms.addForm(form);

         removedForm(DomainEvent.CREATE, form);
      }

      public Form createdForm( DomainEvent event, String id )
      {
         EntityBuilder<Form> builder = uowf.currentUnitOfWork().newEntityBuilder( Form.class, id );
         Form form = builder.newInstance();
         data.forms().add( form );
         return form;
      }

      public void removedForm( DomainEvent event, Form removedForm )
      {
         data.forms().remove( removedForm );
      }

      public Form getFormByName( String name )
      {
         return Describable.Mixin.getDescribable( data.forms(), name );
      }
   }
}
