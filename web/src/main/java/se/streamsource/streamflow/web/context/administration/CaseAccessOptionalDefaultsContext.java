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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.CaseAccessOptionalDefaultsDTO;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessOptionalDefaults;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessType;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * JAVADOC
 */
public class CaseAccessOptionalDefaultsContext
   implements IndexContext<CaseAccessOptionalDefaultsDTO>
{
   @Structure
   Module module;

   @Uses
   Locale locale;

   @Uses
   CaseAccessOptionalDefaults caseAccessOptionalDefaults;

   @Uses
   CaseAccessOptionalDefaults.Data caseAccessOptionalDefaultsData;

   public CaseAccessOptionalDefaultsDTO index()
   {
      ValueBuilder<CaseAccessOptionalDefaultsDTO> dto = module.valueBuilderFactory().newValueBuilder( CaseAccessOptionalDefaultsDTO.class );
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      ResourceBundle bundle = ResourceBundle.getBundle( CaseAccessType.class.getName(), locale );
      ResourceBundle permissionTypeBundle = ResourceBundle.getBundle( PermissionType.class.getName(), locale );
      for (PermissionType permissionType : PermissionType.values())
      {
         if (permissionType == PermissionType.read  || permissionType == PermissionType.write)
         {
            builder.addLink(permissionTypeBundle.getString( permissionType.name() ) +" : "+bundle.getString( caseAccessOptionalDefaults.getOptionalAccessType( permissionType ).name() ), permissionType.name(), "possibleoptionaldefaultaccess", "possibleoptionaldefaultaccess?permission="+permissionType.name(), "" );
         }
      }
      dto.prototype().accessPermissions().set( builder.newLinks() );

      dto.prototype().enable().set( caseAccessOptionalDefaultsData.enableOptionalDefaults().get() );
      return dto.newInstance();
   }

   public LinksValue possibleoptionaldefaultaccess(@Name("permission") PermissionType permissionType)
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      ResourceBundle bundle = ResourceBundle.getBundle( CaseAccessType.class.getName(), locale );
      for (CaseAccessType possibleType : CaseAccessType.values())
      {
         if (!possibleType.equals( caseAccessOptionalDefaults.getOptionalAccessType( permissionType ) ))
            builder.addLink( bundle.getString( possibleType.name() ), possibleType.name(), "changeoptionaldefaultaccess", "changeoptionaldefaultaccess?permission="+permissionType.name()+"&accesstype="+possibleType.name(), "" );
      }

      return builder.newLinks();
   }

   public void changeoptionaldefaultaccess(@Name("permission") PermissionType permissionType, @Name("accesstype") CaseAccessType accessType)
   {
      caseAccessOptionalDefaults.changeOptionalAccessDefault( permissionType, accessType );
   }

   public void changeoptional(@Name("optional") Boolean optional )
   {
      caseAccessOptionalDefaults.changeEnableOptionalDefault(  optional );
   }
}
