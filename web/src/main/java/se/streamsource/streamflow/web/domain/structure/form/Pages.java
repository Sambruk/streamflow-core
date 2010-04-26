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

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.constraints.annotation.GreaterThan;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

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
      ManyAssociation<Page> pages();

      Page createdPage( DomainEvent event, String id );

      void removedPage( DomainEvent event, Page Page );

      void movedPage( DomainEvent event, Page page, int toIdx );
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

      public Page createPage( String name )
      {
         Page Page = createdPage( DomainEvent.CREATE, idGen.generate( Identity.class ) );
         Page.changeDescription( name );
         return Page;
      }

      public void removePage( Page page )
      {
         if (!pages().contains( page ))
            return;

         removedPage( DomainEvent.CREATE, page );
      }

      public void movePage( Page page, Integer toIdx )
      {
         if (!pages().contains( page ) || pages().count() <= toIdx)
            return;

         movedPage( DomainEvent.CREATE, page, toIdx );
      }

      public Page getPageByName( String name )
      {
         for (Page page : pages())
         {
            if (((Describable.Data) page).description().get().equals( name ))
               return page;
         }
         return null;
      }

      public Page createdPage( DomainEvent event, String id )
      {

         EntityBuilder<Page> builder = uowf.currentUnitOfWork().newEntityBuilder( Page.class, id );

         Page page = builder.newInstance();

         pages().add( page );

         return page;
      }

      public void movedPage( DomainEvent event, Page page, int toIdx )
      {
         pages().remove( page );

         pages().add( toIdx, page );
      }

      public void removedPage( DomainEvent event, Page page )
      {
         pages().remove( page );
      }
   }
}