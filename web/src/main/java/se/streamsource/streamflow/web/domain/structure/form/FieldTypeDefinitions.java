/**
 *
 * Copyright 2009-2011 Streamsource AB
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
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.form.FieldTypeDefinitionEntity;

/**
 * JAVADOC
 */
@Mixins(FieldTypeDefinitions.Mixin.class)
public interface FieldTypeDefinitions
{
   FieldTypeDefinition createFieldTypeDefinition( String name );

   void addFieldTypeDefinition(FieldTypeDefinition label);

   void removeFieldTypeDefinition( FieldTypeDefinition label );

   interface Data
   {
      @Aggregated
      ManyAssociation<FieldTypeDefinition> fieldTypeDefinitions();

      FieldTypeDefinition createdFieldTypeDefinition( @Optional DomainEvent event, String identity );

      void addedFieldTypeDefinition( @Optional DomainEvent event, FieldTypeDefinition fieldTypeDefinition );

      void removedFieldTypeDefinition( @Optional DomainEvent event, FieldTypeDefinition fieldTypeDefinition );
   }

   abstract class Mixin
         implements FieldTypeDefinitions, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      @This
      Data data;

      public FieldTypeDefinition createFieldTypeDefinition( String url )
      {
         FieldTypeDefinition fieldTypeDefinition = data.createdFieldTypeDefinition( null, idGen.generate( FieldTypeDefinitionEntity.class ));
         fieldTypeDefinition.changeDescription( url );
         return fieldTypeDefinition;
      }

      public void addFieldTypeDefinition( FieldTypeDefinition fieldTypeDefinition )
      {
         if (!data.fieldTypeDefinitions().contains( fieldTypeDefinition ))
            addedFieldTypeDefinition( null, fieldTypeDefinition);
      }

      public void removeFieldTypeDefinition( FieldTypeDefinition fieldTypeDefinition )
      {
         if (data.fieldTypeDefinitions().contains( fieldTypeDefinition ))
         {
            removedFieldTypeDefinition( null, fieldTypeDefinition );
            fieldTypeDefinition.removeEntity();
         }
      }

      public FieldTypeDefinition createdLabel( DomainEvent event, String identity )
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         FieldTypeDefinition fieldTypeDefinition = uow.newEntity( FieldTypeDefinition.class, identity );
         data.fieldTypeDefinitions().add( data.fieldTypeDefinitions().count(), fieldTypeDefinition );
         return fieldTypeDefinition;
      }

      public void addedFieldTypeDefinition( DomainEvent event, FieldTypeDefinition fieldTypeDefinition )
      {
         data.fieldTypeDefinitions().add( fieldTypeDefinition );
      }

      public void removedFieldTypeDefinition( @Optional DomainEvent event, FieldTypeDefinition fieldTypeDefinition )
      {
         data.fieldTypeDefinitions().remove( fieldTypeDefinition );
      }
   }
}
