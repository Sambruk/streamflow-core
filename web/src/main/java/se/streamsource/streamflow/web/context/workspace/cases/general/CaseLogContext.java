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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.general.CaseLogEntryDTO;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLog;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;

/**
 * JAVADOC
 */
public class CaseLogContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;
   
   public LinksValue index()
   {
      LinksBuilder links = new LinksBuilder( module.valueBuilderFactory() );
      ValueBuilder<CaseLogEntryDTO> builder = module.valueBuilderFactory().newValueBuilder( CaseLogEntryDTO.class );
      
      CaseLoggable.Data caseLog = RoleMap.role( CaseLoggable.Data.class );

      for (CaseLogEntryValue entry : ((CaseLog.Data)caseLog.caselog().get()).entries().get())
      {
         builder.prototype().creationDate().set( entry.createdOn().get() );
//         builder.prototype().creator().set( ((Describable) entry.createdBy().get()).getDescription() );
         builder.prototype().message().set( entry.message().get());
         builder.prototype().href().set( EntityReference.getEntityReference( entry.entity().get() ).identity() );
         builder.prototype().text().set( entry.message().get() );
         builder.prototype().id().set( EntityReference.getEntityReference( entry.entity().get() ).identity() );

         links.addLink( builder.newInstance() );
      }
      return links.newLinks();
   }

   public void createmessage( StringValue message )
   {
//      Messages messages = RoleMap.role( Messages.class );
//      messages.createMessage( message.string().get(), RoleMap.role( ConversationParticipant.class ) );
   }
}