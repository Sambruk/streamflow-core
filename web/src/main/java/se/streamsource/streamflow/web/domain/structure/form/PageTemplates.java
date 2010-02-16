/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.structure.form;

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
@Mixins(PageTemplates.Mixin.class)
public interface PageTemplates
{
   Page createPageTemplate( String name );

   void removePageDefinition( Page page );

   interface Data
   {
      ManyAssociation<Page> pageDefinitions();

      Page createdPageDefinition( DomainEvent event, String id );

      void addedPageDefinition( DomainEvent event, Page page );

      void removedPageDefinition( DomainEvent event, Page page );

      Page getPageDefinitionByName( String name );
   }

   abstract class Mixin
         implements PageTemplates, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      public Page createPageTemplate( String name )
      {
         String id = idGen.generate( Identity.class );

         Page page = createdPageDefinition( DomainEvent.CREATE, id );
         addedPageDefinition( DomainEvent.CREATE, page );
         page.changeDescription( name );

         return page;
      }

      public Page createdPageDefinition( DomainEvent event, String id )
      {
         EntityBuilder<Page> builder = uowf.currentUnitOfWork().newEntityBuilder( Page.class, id );

         return builder.newInstance();
      }

      public Page getPageDefinitionByName( String name )
      {
         return Describable.Mixin.getDescribable( pageDefinitions(), name );
      }
   }
}