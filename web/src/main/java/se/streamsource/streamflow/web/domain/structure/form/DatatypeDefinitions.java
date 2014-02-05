/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
import se.streamsource.streamflow.web.domain.entity.form.DatatypeDefinitionEntity;

/**
 * JAVADOC
 */
@Mixins(DatatypeDefinitions.Mixin.class)
public interface DatatypeDefinitions
{
   DatatypeDefinition createDatatypeDefinition( String name );

   void addDatatypeDefinition(DatatypeDefinition datatypeDefinition);

   void removeDatatypeDefinition( DatatypeDefinition datatypeDefinition );

   interface Data
   {
      @Aggregated
      ManyAssociation<DatatypeDefinition> datatypeDefinitions();

      DatatypeDefinition createdDatatypeDefinition( @Optional DomainEvent event, String identity, String url );

      void addedDatatypeDefinition( @Optional DomainEvent event, DatatypeDefinition datatypeDefinition );

      void removedDatatypeDefinition( @Optional DomainEvent event, DatatypeDefinition datatypeDefinition );
   }

   abstract class Mixin
         implements DatatypeDefinitions, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      @This
      Data data;

      public DatatypeDefinition createDatatypeDefinition( String url )
      {
         DatatypeDefinition datatypeDefinition = data.createdDatatypeDefinition( null, idGen.generate( DatatypeDefinitionEntity.class ), url);
         addDatatypeDefinition( datatypeDefinition );
         return datatypeDefinition;
      }

      public void addDatatypeDefinition( DatatypeDefinition datatypeDefinition )
      {
         if (!data.datatypeDefinitions().contains( datatypeDefinition ))
            addedDatatypeDefinition( null, datatypeDefinition);
      }

      public void removeDatatypeDefinition( DatatypeDefinition datatypeDefinition )
      {
         if (data.datatypeDefinitions().contains( datatypeDefinition ))
         {
            removedDatatypeDefinition( null, datatypeDefinition );
            datatypeDefinition.removeEntity();
         }
      }

      public DatatypeDefinition createdDatatypeDefinition( DomainEvent event, String id, String url)
      {
         EntityBuilder<DatatypeDefinitionEntity> entityBuilder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( DatatypeDefinitionEntity.class, id );
         entityBuilder.instance().url().set( url );
         return entityBuilder.newInstance();
      }

      public void addedDatatypeDefinition( DomainEvent event, DatatypeDefinition datatypeDefinition )
      {
         data.datatypeDefinitions().add( datatypeDefinition );
      }

      public void removedDatatypeDefinition( DomainEvent event, DatatypeDefinition datatypeDefinition )
      {
         data.datatypeDefinitions().remove( datatypeDefinition );
      }
   }
}
