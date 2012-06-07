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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.organization.Priorities;
import se.streamsource.streamflow.web.domain.structure.organization.Priority;
import se.streamsource.streamflow.web.domain.structure.organization.PrioritySettings;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Case priorities context
 */
@Mixins( PrioritiesContext.Mixin.class )
public interface PrioritiesContext
   extends IndexContext<LinksValue>, Context
{
   public void create( @Name( "name" ) String name );
   
   public void up( @Name( "id" ) String id );
   
   public void down( @Name( "id" ) String id );

   abstract class Mixin
      implements PrioritiesContext
   {
      @Structure
      Module module;
      
      public LinksValue index()
      {
         Priorities.Data priorities = RoleMap.role( Priorities.Data.class );
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         builder.command( "delete" );

         List<Priority> sortedList =  priorities.prioritys().toList();
         Collections.sort( sortedList, new Comparator<Priority>()
         {
            public int compare( Priority o1, Priority o2 )
            {
               return ((PrioritySettings.Data)o1).priority().get().compareTo( ((PrioritySettings.Data)o2).priority().get() );
            }
         } );

         for(Priority priority : sortedList )
         {
            builder.addLink( priority.getDescription(), EntityReference.getEntityReference( priority ).identity() );
         }
         return builder.newLinks();
      }
      
      public void create( final String name )
      {
         Priorities priorities = RoleMap.role( Priorities.class );
         if( !Iterables.matchesAny( new Specification<Describable>()
         {
            public boolean satisfiedBy( Describable describable )
            {
               return name.equals( describable.getDescription() );
            }
         }, ((Priorities.Data)priorities).prioritys().toList() ) )
         {
            Priority priority = priorities.createPriority( );
            priority.changeDescription( name );
         }
      }
      
      public void up( String id )
      {
         RoleMap.role(  Priorities.class ).changePriorityOrder( module.unitOfWorkFactory().currentUnitOfWork().get( Priority.class, id ), - 1 );
      }
      
      public void down( String id )
      {
         RoleMap.role(  Priorities.class ).changePriorityOrder( module.unitOfWorkFactory().currentUnitOfWork().get( Priority.class, id ), 1 );
      }
   }
}
