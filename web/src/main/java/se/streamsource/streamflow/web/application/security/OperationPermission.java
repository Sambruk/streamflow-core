/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.application.security;

import java.security.BasicPermission;

/**
 * Permission implementation for StreamFlow. The name has a two-level structure:
 * the first part is what kind of object is being invoked, and the second is the operation to be performed.
 * <p/>
 * Example:
 * organizationalUnits.createOrganizationalUnit
 */
public class OperationPermission
      extends BasicPermission
{
   public OperationPermission( String object, String operation )
   {
      super( object + "." + operation );
   }

   public OperationPermission( String name )
   {
      super( name );
   }
}
