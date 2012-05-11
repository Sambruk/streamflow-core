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
package se.streamsource.streamflow.web.domain.interaction.security;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.Map;

/**
 * Management of default case access rules.
 */
@Mixins(CaseAccessOptionalDefaults.Mixin.class)
public interface CaseAccessOptionalDefaults
{
   void changeEnableOptionalDefault( Boolean enable );

   void changeOptionalAccessDefault( PermissionType permissionType, CaseAccessType accessType );

   CaseAccessType getOptionalAccessType( PermissionType permissionType );

   interface Data
   {
      @UseDefaults
      Property<Boolean> enableOptionalDefaults();

      @UseDefaults
      Property<Map<PermissionType, CaseAccessType>> accessOptionalPermissionDefaults();

      void changedOptionalAccess( @Optional DomainEvent event, PermissionType permissionType, CaseAccessType accessType );

      void changedOptional( @Optional DomainEvent event, Boolean optionalAccess );
   }

   abstract class Mixin
      implements CaseAccessOptionalDefaults, Data
   {
      public void changeEnableOptionalDefault( Boolean enable )
      {
         if ( enableOptionalDefaults().get() != enable ) {
            changedOptional( null, enable );
         }
      }

      public void changeOptionalAccessDefault( PermissionType permissionType, CaseAccessType accessType )
      {
         changedOptionalAccess( null, permissionType, accessType );
      }

      public void changedOptionalAccess( @Optional DomainEvent event, PermissionType permissionType, CaseAccessType accessType )
      {
         Map<PermissionType, CaseAccessType> permissionAccess = accessOptionalPermissionDefaults().get();

         permissionAccess.put( permissionType, accessType );

         accessOptionalPermissionDefaults().set( permissionAccess );
      }

      public void changedOptional( @Optional DomainEvent event, Boolean optionalAccess )
      {
         enableOptionalDefaults().set(  optionalAccess  );
      }

      public CaseAccessType getOptionalAccessType( PermissionType permissionType )
      {
         CaseAccessType current = accessOptionalPermissionDefaults().get().get( permissionType );

         if (current == null)
            current = CaseAccessType.all;

         return current;
      }
   }
}
