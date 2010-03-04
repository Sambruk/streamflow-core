/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.domain.structure.role;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

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

      void addedPermission( DomainEvent event, String permission);
      void removedPermission( DomainEvent event, String permission);
   }

   abstract class Mixin
      implements Permissions, Data
   {
      public void addPermission( String permission )
      {
         if (!permissions().get().contains( permission ))
         {
            addedPermission( DomainEvent.CREATE, permission );
         }
      }

      public void removePermission( String permission )
      {
         if (permissions().get().contains( permission ))
         {
            removedPermission( DomainEvent.CREATE, permission );
         }
      }

      public boolean hasPermission( String permission )
      {
         return permissions().get().contains( permission );
      }

      public void addedPermission( DomainEvent event, String permission )
      {
         permissions().get().add( permission );
      }

      public void removedPermission( DomainEvent event, String permission )
      {
         permissions().get().remove( permission );
      }
   }
}
