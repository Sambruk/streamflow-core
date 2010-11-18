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

package se.streamsource.streamflow.web.domain.interaction.profile;

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
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.resource.user.profile.SearchValue;
import se.streamsource.streamflow.web.domain.entity.user.profile.SavedSearchEntity;
import se.streamsource.streamflow.web.domain.structure.user.profile.SavedSearch;

@Mixins(SavedSearches.Mixin.class)
public interface SavedSearches
{
   public void createSavedSearch( SearchValue search );

   public void removeSavedSearch( SavedSearch search );

   interface Data
   {
      @Aggregated
      ManyAssociation<SavedSearch> searches();

      SavedSearch createdSavedSearch( @Optional DomainEvent event, String id );

      void removedSavedSearch( @Optional DomainEvent event, SavedSearch search );
   }

   abstract class Mixin
         implements SavedSearches, Data
   {
      @This
      Data state;

      @Service
      IdentityGenerator idgen;

      @Structure
      UnitOfWorkFactory uowf;

      public void createSavedSearch( SearchValue search )
      {
         String id = idgen.generate( Identity.class );
         SavedSearch newSearch = createdSavedSearch( null, id );
         newSearch.changeDescription( search.name().get() );
      }

      public SavedSearch createdSavedSearch( DomainEvent event, String id, String query )
      {
         EntityBuilder<SavedSearchEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( SavedSearchEntity.class, id );
         builder.instance().query().set( query );
         SavedSearch savedSeach = builder.newInstance();
         state.searches().add( savedSeach );
         return savedSeach;
      }

      public void removeSavedSearch( SavedSearch search )
      {
         if (state.searches().contains( search ))
         {
            removedSavedSearch( null, search );
         }
      }

      public void removedSavedSearch( DomainEvent event, SavedSearch search )
      {
         state.searches().remove( search );
         uowf.currentUnitOfWork().remove( search );
      }
   }
}
