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
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.constraints.annotation.GreaterThan;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Pages.Mixin.class)
public interface Pages
{
   Page createPage( String name );

   void removePage( Page page );

   void movePage( Page page, @GreaterThan(-1) Integer toIdx );

   Page getPageByName( String name );

   interface Data
   {
      @Aggregated
      ManyAssociation<Page> pages();

      Page createdPage( @Optional DomainEvent event, String id );

      void removedPage( @Optional DomainEvent event, Page Page );

      void movedPage( @Optional DomainEvent event, Page page, int toIdx );
   }

   abstract class Mixin
         implements Pages, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @This
      Data data;

      public Page createPage( String name )
      {
         Page Page = createdPage( null, idGen.generate( Identity.class ) );
         Page.changeDescription( name );
         return Page;
      }

      public void removePage( Page page )
      {
         if (!data.pages().contains( page ))
            return;

         removedPage( null, page );
      }

      public void movePage( Page page, Integer toIdx )
      {
         if (!data.pages().contains( page ) || data.pages().count() <= toIdx)
            return;

         movedPage( null, page, toIdx );
      }

      public Page getPageByName( String name )
      {
         for (Page page : data.pages())
         {
            if (((Describable.Data) page).description().get().equals( name ))
               return page;
         }
         return null;
      }

      public Page createdPage( @Optional DomainEvent event, String id )
      {

         EntityBuilder<Page> builder = uowf.currentUnitOfWork().newEntityBuilder( Page.class, id );

         Page page = builder.newInstance();

         data.pages().add( page );

         return page;
      }

      public void movedPage( @Optional DomainEvent event, Page page, int toIdx )
      {
         data.pages().remove( page );

         data.pages().add( toIdx, page );
      }

      public void removedPage( @Optional DomainEvent event, Page page )
      {
         data.pages().remove( page );
      }
   }
}