/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.form.FieldGroupEntity;

/**
 * JAVADOC
 */
@Mixins(FieldGroups.Mixin.class)
public interface FieldGroups
{
   FieldGroup createFieldGroup( String name );

   void addFieldGroup(FieldGroup fieldGroup);

   void removeFieldGroup( FieldGroup fieldGroup );

   interface Data
   {
      @Aggregated
      ManyAssociation<FieldGroup> fieldGroups();

      FieldGroup createdFieldGroup( @Optional DomainEvent event, String identity, String url );

      void addedFieldGroup( @Optional DomainEvent event, FieldGroup fieldGroup );

      void removedFieldGroup( @Optional DomainEvent event, FieldGroup fieldGroup );
   }

   abstract class Mixin
         implements FieldGroups, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      @This
      Data data;

      public FieldGroup createFieldGroup( String url )
      {
         FieldGroup fieldGroup = data.createdFieldGroup( null, idGen.generate( FieldGroupEntity.class ), url);
         addFieldGroup( fieldGroup );
         return fieldGroup;
      }

      public void addFieldGroup( FieldGroup fieldGroup )
      {
         if (!data.fieldGroups().contains( fieldGroup ))
            addedFieldGroup( null, fieldGroup);
      }

      public void removeFieldGroup( FieldGroup fieldGroup )
      {
         if (data.fieldGroups().contains( fieldGroup ))
         {
            removedFieldGroup( null, fieldGroup );
            fieldGroup.removeEntity();
         }
      }

      public FieldGroup createdFieldGroup( DomainEvent event, String id, String name)
      {
         EntityBuilder<FieldGroupEntity> entityBuilder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( FieldGroupEntity.class, id );
         entityBuilder.instance().description().set( name );
         return entityBuilder.newInstance();
      }

      public void addedFieldGroup( DomainEvent event, FieldGroup fieldGroup )
      {
         data.fieldGroups().add( fieldGroup );
      }

      public void removedFieldGroup( DomainEvent event, FieldGroup fieldGroup )
      {
         data.fieldGroups().remove( fieldGroup );
      }
   }
}
