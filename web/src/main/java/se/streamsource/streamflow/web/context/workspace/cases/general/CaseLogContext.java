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

package se.streamsource.streamflow.web.context.workspace.cases.general;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.SkipResourceValidityCheck;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.general.CaseLogEntryDTO;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.MessagesContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLog;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable.Data;

/**
 * JAVADOC
 */
public class CaseLogContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;
   
   @SkipResourceValidityCheck
   public LinksValue index()
   {
      LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
      ValueBuilder<CaseLogEntryDTO> builder = module.valueBuilderFactory().newValueBuilder( CaseLogEntryDTO.class );
      
      CaseLoggable.Data caseLog = RoleMap.role( CaseLoggable.Data.class );
      
      ResourceBundle bundle = ResourceBundle.getBundle( MessagesContext.class.getName(), RoleMap.role( Locale.class ) );
      Map<String, String> translations = new HashMap<String, String>();
      for (String key : bundle.keySet())
      {
         translations.put(key, bundle.getString(key));
      }
      
      for (CaseLogEntryValue entry : ((CaseLog.Data)caseLog.caselog().get()).entries().get())
      {
         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
         Describable user = uow.get( Describable.class, entry.createdBy().get().identity() );
         builder.prototype().creationDate().set( entry.createdOn().get() );
         builder.prototype().creator().set( user.getDescription() );
         builder.prototype().message().set( Translator.translate( entry.message().get(), translations));
         String id = "";
         if (entry.entity().get() != null)
         {
            id = EntityReference.getEntityReference( entry.entity().get() ).identity();
         } 
         builder.prototype().href().set( id );
         builder.prototype().id().set( id );
            
         builder.prototype().text().set( Translator.translate( entry.message().get(), translations));

         links.addLink( builder.newInstance() );
      }
      return links.newLinks();
   }

   @RequiresPermission(PermissionType.write)
   public void addmessage( StringValue message )
   {
      CaseLoggable.Data caseLog = RoleMap.role( CaseLoggable.Data.class );
      caseLog.caselog().get().addCustomEntry( message.string().get(), CaseLogEntryValue.AuthorizationType.user );
   }
}