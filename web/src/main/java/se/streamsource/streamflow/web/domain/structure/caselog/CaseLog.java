/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.caselog;

import java.util.Date;
import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;

@Mixins(CaseLog.Mixin.class)
public interface CaseLog extends Removable
{

   CaseLogEntryValue getLastMessage();
   
   void addCustomEntry(String message, Boolean availableOnMypages);

   void addTypedEntry(String message, CaseLogEntryTypes type);

   void modifyMyPagesVisibility( int index, boolean publish );
   
   interface Data
   {
      @UseDefaults
      Property<List<CaseLogEntryValue>> entries();

      void addedEntry(@Optional DomainEvent event, CaseLogEntryValue entry);

      void modifiedMyPagesVisibility( @Optional DomainEvent event, int index, boolean publish );
   }

   abstract class Mixin implements CaseLog, Data
   {

      @Structure
      Module module;

      @This
      Data data;

      public void addedEntry(DomainEvent event, CaseLogEntryValue entry)
      {
         List<CaseLogEntryValue> list = data.entries().get();
         list.add( entry );
         data.entries().set( list );
      }

      public void addCustomEntry(String message, Boolean availableOnMypages)
      {
         ValueBuilder<CaseLogEntryValue> builder = module.valueBuilderFactory().newValueBuilder(
               CaseLogEntryValue.class );
         builder.prototype().createdBy()
               .set( EntityReference.getEntityReference( RoleMap.role( ConversationParticipant.class ) ) );
         builder.prototype().createdOn().set( new Date() );
         builder.prototype().entryType().set( CaseLogEntryTypes.custom );
         builder.prototype().message().set( message );
         builder.prototype().availableOnMypages().set( availableOnMypages );

         addedEntry( null, builder.newInstance() );
      }

      public void addTypedEntry(String message, CaseLogEntryTypes type)
      {
         ValueBuilder<CaseLogEntryValue> builder = module.valueBuilderFactory().newValueBuilder(
               CaseLogEntryValue.class );
         builder.prototype().createdBy()
               .set( EntityReference.getEntityReference( RoleMap.role( ConversationParticipant.class ) ) );
         builder.prototype().createdOn().set( new Date() );
         builder.prototype().entryType().set( type );
         builder.prototype().message().set( message );

         addedEntry( null, builder.newInstance() );
      }

      public void modifyMyPagesVisibility( int index, boolean publish )
      {
         modifiedMyPagesVisibility( null, index, publish );
      }


      public void modifiedMyPagesVisibility( DomainEvent event, int index, boolean publish )
      {
         List<CaseLogEntryValue> list = data.entries().get();

         ValueBuilder<CaseLogEntryValue> valueBuilder = list.get( index ).buildWith();
         valueBuilder.prototype().availableOnMypages().set( publish );
         list.set( index, valueBuilder.newInstance() );
         
         data.entries().set( list );
      }

      public CaseLogEntryValue getLastMessage()
      {
         if (entries().get().size() > 0)
            return entries().get().get(entries().get().size()-1);
         else
            return null;
      }
   }
}
