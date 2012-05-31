/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.api.administration.priority.CasePriorityValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.casetype.CasePrioritySetting;

import java.util.List;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * Contains priority definitions.
 */
@Concerns( CasePriorityDefinitions.RemovePriorityConcern.class )
@Mixins( CasePriorityDefinitions.Mixin.class )
public interface CasePriorityDefinitions
{
   public void createPriority( String name );
   
   public void removePriority( int index );
   
   public void changePriority( int index, CasePriorityValue value );
   
   public void changePriorityOrder( int index, int newIndex );
   
   interface Data
   {
      @Optional
      @UseDefaults
      Property<List<CasePriorityValue>> prioritys();
      
      void createdPriority( @Optional DomainEvent event, String name );
      void removedPriority( @Optional DomainEvent event, CasePriorityValue priority );
      void changedPriority( @Optional DomainEvent event, int index, CasePriorityValue priority );
      void changedPriorityOrder( @Optional DomainEvent event, int index, int newIndex );
   }

   abstract class Mixin
      implements CasePriorityDefinitions, Data
   {
      @This
      Data data;
      
      @Structure
      Module module;
      
      public void createPriority( String name )
      {
         if( !contains( name ) )
            data.createdPriority( null, name );
      }

      public void removePriority( int index )
      {
         removedPriority( null, data.prioritys().get().get( index ) );
      }
      
      public void removedPriority( DomainEvent event, CasePriorityValue priority )
      {
         List<CasePriorityValue> list = data.prioritys().get();
         list.remove( priority );
         data.prioritys().set( list );
      }
      
      public void createdPriority( DomainEvent event, String name )
      {
         ValueBuilder<CasePriorityValue> builder = module.valueBuilderFactory().newValueBuilder( CasePriorityValue.class );
         builder.prototype().name().set( name );
         
         List<CasePriorityValue> list = data.prioritys().get();
         list.add( builder.newInstance() );
         data.prioritys().set( list );
      }

      public void changePriority( int index, CasePriorityValue priority )
      {
         if( index >=0 && index < data.prioritys().get().size() ) 
            changedPriority( null, index, priority );
      }
      
      public void changedPriority( DomainEvent event, int index, CasePriorityValue priority )
      {
         List<CasePriorityValue> list = data.prioritys().get();
         list.remove( index );
         list.add( index, priority );
         
         data.prioritys().set( list );
               
      }
      
      public void changePriorityOrder( int index, int newIndex )
      {
         if( newIndex >= 0 && newIndex < data.prioritys().get().size() )
            changedPriorityOrder( null, index, newIndex );
      }
      
      public void changedPriorityOrder( DomainEvent event, int index, int newIndex )
      {
         List<CasePriorityValue> list = data.prioritys().get();
         list.add( newIndex, list.remove(index) );
         
         data.prioritys().set( list );
      }
      private boolean contains( String name )
      {
         for( CasePriorityValue priority : data.prioritys().get() )
         {
            if( name.equals( priority.name().get() ) )
               return true;
         }
         return false;
      }
   }

   abstract class RemovePriorityConcern
      extends ConcernOf<CasePriorityDefinitions>
      implements CasePriorityDefinitions
   {
      @Structure
      Module module;

      @This
      Data data;

      public void removePriority( int index)
      {
         Query<CasePrioritySetting> query = module.queryBuilderFactory().newQueryBuilder( CasePrioritySetting.class )
               .where( eq(
                     templateFor( CasePrioritySetting.Data.class ).defaultPriority().get().name(),
                     data.prioritys().get().get( index ).name().get() ) )
               .newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

         if( query.count() > 0 )
            throw new IllegalStateException( ErrorResources.priority_remove_failed_default_exist.toString() );

         next.removePriority( index );
      }

   }
}
