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

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.priority.CasePriorityValue;
import se.streamsource.streamflow.web.domain.structure.caze.CasePriorityDefinitions;

import java.io.IOException;

/**
 * Case priority definition context.
 */

public class CasePriorityDefinitionContext
   implements IndexContext<CasePriorityValue>, DeleteContext
{

   public void delete() throws IOException
   {
      CasePriorityDefinitions casePriorities = RoleMap.role( CasePriorityDefinitions.class );
      int index = RoleMap.role( Integer.class );

      casePriorities.removePriority( index );
   }

   public CasePriorityValue index()
   {
      CasePriorityDefinitions.Data casePriorities = RoleMap.role(  CasePriorityDefinitions.Data.class );
      int index = RoleMap.role( Integer.class );

      return casePriorities.prioritys().get().get( index );
   }

   public void change( CasePriorityValue value )
   {
      int index = RoleMap.role( Integer.class );
      RoleMap.role(  CasePriorityDefinitions.class ).changePriority( index, value );
   }
}