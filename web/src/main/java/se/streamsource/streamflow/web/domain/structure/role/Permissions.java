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
package se.streamsource.streamflow.web.domain.structure.role;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(Permissions.Mixin.class)
public interface Permissions
{
   void addPermission(String permission);
   void removePermission(String permission);

   boolean hasPermission(String permission);

   interface Data
   {
      @UseDefaults
      Property<List<String>> permissions();

      void addedPermission( @Optional DomainEvent event, String permission);
      void removedPermission( @Optional DomainEvent event, String permission);
   }

   abstract class Mixin
      implements Permissions, Data
   {
      public void addPermission( String permission )
      {
         if (!permissions().get().contains( permission ))
         {
            addedPermission( null, permission );
         }
      }

      public void removePermission( String permission )
      {
         if (permissions().get().contains( permission ))
         {
            removedPermission( null, permission );
         }
      }

      public boolean hasPermission( String permission )
      {
         return permissions().get().contains( permission );
      }

      public void addedPermission( DomainEvent event, String permission )
      {
         List<String> permissionList = permissions().get();
         permissionList.add( permission );
         permissions().set( permissionList );
      }

      public void removedPermission( DomainEvent event, String permission )
      {
         List<String> permissionList = permissions().get();
         permissionList.remove( permission );
         permissions().set( permissionList );
      }
   }
}
