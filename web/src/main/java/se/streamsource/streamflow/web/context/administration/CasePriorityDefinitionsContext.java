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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.priority.CasePriorityValue;
import se.streamsource.streamflow.web.domain.structure.organization.CasePriorityDefinitions;

/**
 * Case priorities context
 */
@Mixins( CasePriorityDefinitionsContext.Mixin.class )
public interface CasePriorityDefinitionsContext
   extends IndexContext<LinksValue>, Context
{
   public void create( @Name( "name" ) String name );
   
   public void up( @Name( "index" ) int index );
   
   public void down( @Name( "index" ) int index );

   abstract class Mixin
      implements CasePriorityDefinitionsContext
   {
      @Structure
      Module module;
      
      public LinksValue index()
      {
         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
         int count = 0;
         for( CasePriorityValue priority : RoleMap.role( CasePriorityDefinitions.Data.class ).prioritys().get() )
         {
            builder.addLink( priority.name().get(), "" + count, "priority", "" + count + "/", null );
            count++;
         }
         return builder.newLinks();
      }
      
      public void create( String name )
      {
         RoleMap.role( CasePriorityDefinitions.class ).createPriority( name );
      }
      
      public void up( int index )
      {
         RoleMap.role(  CasePriorityDefinitions.class ).changePriorityOrder( index, index - 1 );
      }
      
      public void down( int index )
      {
         RoleMap.role(  CasePriorityDefinitions.class ).changePriorityOrder( index, index + 1 );
      }
   }
}
