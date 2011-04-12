/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.interaction.security.*;

import java.util.*;

/**
 * JAVADOC
 */
public class CaseAccessDefaultsContext
   implements IndexContext<LinksValue>
{
   @Structure
   ValueBuilderFactory vbf;

   public LinksValue index()
   {
      CaseAccessDefaults defaults = RoleMap.role( CaseAccessDefaults.class );
      LinksBuilder builder = new LinksBuilder(vbf);
      ResourceBundle bundle = ResourceBundle.getBundle( CaseAccessType.class.getName(), RoleMap.role( Locale.class ) );
      ResourceBundle permissionTypeBundle = ResourceBundle.getBundle( PermissionType.class.getName(), RoleMap.role( Locale.class ) );
      for (PermissionType permissionType : PermissionType.values())
      {
         if (permissionType == PermissionType.read || permissionType == PermissionType.write)
         {
            builder.addLink(permissionTypeBundle.getString( permissionType.name() ) +" : "+bundle.getString( defaults.getAccessType( permissionType ).name() ), permissionType.name(), "possibledefaultaccess", "possibledefaultaccess?permission="+permissionType.name(), "" );
         }
      }

      return builder.newLinks();
   }

   public LinksValue possibledefaultaccess(@Name("permission") PermissionType permissionType)
   {
      CaseAccessDefaults defaults = RoleMap.role( CaseAccessDefaults.class );
      LinksBuilder builder = new LinksBuilder(vbf);
      ResourceBundle bundle = ResourceBundle.getBundle( CaseAccessType.class.getName(), RoleMap.role( Locale.class ) );
      for (CaseAccessType possibleType : CaseAccessType.values())
      {
         if (!possibleType.equals( defaults.getAccessType( permissionType ) ))
            builder.addLink( bundle.getString( possibleType.name() ), possibleType.name(), "changedefaultaccess", "changedefaultaccess?permission="+permissionType.name()+"&accesstype="+possibleType.name(), "" );
      }

      return builder.newLinks();
   }

   public void changedefaultaccess(@Name("permission") PermissionType permissionType, @Name("accesstype") CaseAccessType accessType)
   {
      RoleMap.role( CaseAccessDefaults.class ).changeAccessDefault( permissionType, accessType );
   }
}
