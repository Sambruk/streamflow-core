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
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue.AuthorizationType;
import se.streamsource.streamflow.web.domain.structure.created.Creator;


@Mixins(CaseLog.Mixin.class)
public interface CaseLog
{

   void addCustomEntry( String message, CaseLogEntryValue.AuthorizationType authorizationType );

   
   interface Data
   {
      @UseDefaults
      Property<List<CaseLogEntryValue>> entries();
      
      void addedEntry( @Optional DomainEvent event, CaseLogEntryValue entry);
   }
   
   abstract class Mixin implements CaseLog, Data
   {

      @Structure
      Module module;
      
      @This
      Data data;

      public void addedEntry(DomainEvent event, CaseLogEntryValue entry)
      {
         data.entries().get().add( entry );
      }

      public void addCustomEntry(String message, AuthorizationType authorizationType)
      {
         ValueBuilder<CaseLogEntryValue> builder = module.valueBuilderFactory().newValueBuilder( CaseLogEntryValue.class );
         builder.prototype().createdBy().set( EntityReference.getEntityReference( RoleMap.role( Creator.class ) ));
         builder.prototype().createdOn().set( new Date());
         builder.prototype().entryType().set( CaseLogEntryValue.EntryType.custom );
         builder.prototype().message().set( message );
         builder.prototype().authorizationType().set( authorizationType );
         
         addedEntry( null, builder.newInstance() );
      }
      
   }
}
