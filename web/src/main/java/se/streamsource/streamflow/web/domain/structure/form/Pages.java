/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.library.constraints.annotation.GreaterThan;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.Describable;

/**
 * JAVADOC
 */
@Mixins(Pages.Mixin.class)
@Concerns( Pages.MoveConcern.class )
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
      Module module;

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

         EntityBuilder<Page> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Page.class, id );

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

   abstract class MoveConcern
      extends ConcernOf<Pages>
      implements Pages
   {
      @This Pages.Data pages;

      public void movePage( Page page, @GreaterThan(-1) Integer toIdx )
      {
         if( ruleViolation( page, toIdx ) )
         {
            throw new IllegalArgumentException( ErrorResources.form_move_page_rule_violation.name() );
         } else
         {
            next.movePage( page, toIdx );
         }
      }

      /**
       * Check if a move would result in a rule violation.
       * A page with a rule may not be moved to a location before the target field of the rule!
       * A page may not switch place with a page that has the target field as rule!
       * @param page The page to be moved
       * @param toIdx The index to move to
       * @return Whether the move will result in a rule violation or not.
       */
      private boolean ruleViolation(final Page page, Integer toIdx )
      {
         final Page moveTo = pages.pages().get( toIdx.intValue() );
         boolean returnValue = false;

         if( page.getRule() != null && !Strings.empty( page.getRule().field().get() ) )
         {
            returnValue = Iterables.count( Iterables.filter( new Specification<Field>()
            {
               public boolean satisfiedBy( Field field )
               {
                  return ((Identity)field).identity().get().equals( page.getRule().field().get() );
               }
            }, ((Fields.Data)moveTo).fields() ) ) > 0;

         } else if( moveTo.getRule() != null && !Strings.empty( moveTo.getRule().field().get() ) )
         {
            returnValue = Iterables.count( Iterables.filter( new Specification<Field>()
            {
               public boolean satisfiedBy( Field field )
               {
                  return ((Identity)field).identity().get().equals( moveTo.getRule().field().get() );
               }
            }, ((Fields.Data)page).fields() ) ) > 0;
         }
         return returnValue;
      }
   }
}