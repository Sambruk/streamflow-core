/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Forms.Mixin.class)
public interface Forms
{
   Form createForm();

   void removeForm( Form form );

   interface Data
   {
      ManyAssociation<Form> forms();

      Form createdForm( DomainEvent event, String id);

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

      public Form createForm()
      {
         Form form = createdForm( DomainEvent.CREATE, idGen.generate( Identity.class ) );

         return form;
      }

      public void removeForm( Form form )
      {
         if (!forms().contains( form ))
            return;

         removedForm( DomainEvent.CREATE, form );
      }

      public Form createdForm( DomainEvent event, String id )
      {
         EntityBuilder<Form> builder = uowf.currentUnitOfWork().newEntityBuilder( Form.class, id );
         Form form = builder.newInstance();
         forms().add( form );
         return form;
      }

      public void removedForm( DomainEvent event, Form removedForm )
      {
         forms().remove( removedForm );
      }

      public Form getFormByName( String name )
      {
         return Describable.Mixin.getDescribable( forms(), name );
      }
   }
}
