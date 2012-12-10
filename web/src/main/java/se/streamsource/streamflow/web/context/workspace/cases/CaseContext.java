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
package se.streamsource.streamflow.web.context.workspace.cases;

import java.util.Locale;
import java.util.ResourceBundle;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.general.PermissionsDTO;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessType;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Case;

/**
 * JAVADOC
 */
public class CaseContext
   implements IndexContext<Case>
{
   @Structure
   Module module;

   @Uses
   Locale locale;

   public Case index()
   {
      return RoleMap.role( Case.class );
   }

   public PermissionsDTO permissions()
   {
      ValueBuilder<PermissionsDTO> builder = module.valueBuilderFactory().newValueBuilder( PermissionsDTO.class );
      ResourceBundle bundle = ResourceBundle.getBundle( CaseAccessType.class.getName(), locale );
      ResourceBundle permissionTypeBundle = ResourceBundle.getBundle( PermissionType.class.getName(), locale );
      Case aCase = RoleMap.role( Case.class );

      CaseAccessType read  = aCase.getAccessType( PermissionType.read );
      builder.prototype().readAccess().set( permissionTypeBundle.getString( PermissionType.read.name() ) + ": " + bundle.getString( read.name() ) );
      CaseAccessType write = aCase.getAccessType( PermissionType.write );
      builder.prototype().writeAccess().set( permissionTypeBundle.getString( PermissionType.write.name())+ ": " + bundle.getString( write.name() ) );

      return builder.newInstance();
   }
}
