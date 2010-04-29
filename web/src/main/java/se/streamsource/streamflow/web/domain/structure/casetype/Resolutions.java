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

package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * List of defined Resolutions
 */
@Mixins(Resolutions.Mixin.class)
public interface Resolutions
{
   Resolution createResolution( String name );

   void removeResolution( Resolution resolution );

   boolean hasResolutions();

   Iterable<Resolution> getResolutions();

   Query<SelectedResolutions> usages( Resolution resolution );

   interface Data
   {
      @Aggregated
      ManyAssociation<Resolution> resolutions();

      Resolution createdResolution( DomainEvent event );

      void removedResolution( DomainEvent event, Resolution resolution );
   }

   abstract class Mixin
         implements Resolutions, Data
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      QueryBuilderFactory qbf;

      @This
      Data state;

      public Resolution createResolution( String name )
      {
         Resolution resolution = createdResolution( DomainEvent.CREATE );
         resolution.changeDescription( name );
         return resolution;
      }

      public void removeResolution( Resolution resolution )
      {
         if (state.resolutions().contains( resolution ))
         {
            removedResolution( DomainEvent.CREATE, resolution );
            resolution.removeEntity();
         }
      }

      public boolean hasResolutions()
      {
         return state.resolutions().count() > 0;
      }

      public Iterable<Resolution> getResolutions()
      {
         return state.resolutions();
      }

      public Query<SelectedResolutions> usages( Resolution resolution )
      {
         SelectedResolutions.Data selectedResolutions = QueryExpressions.templateFor( SelectedResolutions.Data.class );
         Query<SelectedResolutions> resolutionUsages = qbf.newQueryBuilder( SelectedResolutions.class ).
               where( QueryExpressions.contains( selectedResolutions.selectedResolutions(), resolution ) ).
               newQuery( uowf.currentUnitOfWork() );

         return resolutionUsages;
      }

      public Resolution createdResolution( DomainEvent event )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();
         Resolution resolution = uow.newEntity( Resolution.class );
         state.resolutions().add( state.resolutions().count(), resolution );
         return resolution;
      }

      public void removedResolution( DomainEvent event, Resolution resolution )
      {
         state.resolutions().remove( resolution );
      }
   }
}
