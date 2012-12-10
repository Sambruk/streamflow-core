/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import java.io.IOException;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.web.domain.structure.organization.Priorities;
import se.streamsource.streamflow.web.domain.structure.organization.Priority;
import se.streamsource.streamflow.web.domain.structure.organization.PrioritySettings;

/**
 * Case priority definition context.
 */
public class PriorityContext
   implements IndexContext<PriorityValue>, DeleteContext
{

      @Structure
      Module module;

      public void delete() throws IOException
      {
         Priority priority = RoleMap.role( Priority.class );
         RoleMap.role( Priorities.class ).removePriority( priority );
         priority.removeEntity();

      }

      public PriorityValue index()
      {
         Priority priority = RoleMap.role(  Priority.class );
         ValueBuilder<PriorityValue> builder = module.valueBuilderFactory().newValueBuilder( PriorityValue.class );
         builder.prototype().text().set( priority.getDescription() );
         builder.prototype().id().set( EntityReference.getEntityReference( priority ).identity() );
         builder.prototype().href().set( EntityReference.getEntityReference( priority ).identity() );
         builder.prototype().color().set( ((PrioritySettings.Data)priority).color().get() );
         builder.prototype().priority().set( ((PrioritySettings.Data)priority).priority().get() );
         return builder.newInstance();
      }

      public void changecolor( @Name("color") String color )
      {
         RoleMap.role( Priority.class ).changeColor( color );
      }

}