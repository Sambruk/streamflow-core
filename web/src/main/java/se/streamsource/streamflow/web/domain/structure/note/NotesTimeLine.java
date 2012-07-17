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
package se.streamsource.streamflow.web.domain.structure.note;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

import java.util.List;

/**
 * Marker interface for notes.
 */
@Mixins(NotesTimeLine.Mixin.class)
public interface NotesTimeLine
   extends Removable
{

   void addNote( String note );

   NoteValue getLastNote();

   interface Data
   {
      @UseDefaults
      Property<List<NoteValue>> notes();

      void addedNote( @Optional DomainEvent event, String note );
   }

   abstract class Mixin
      implements NotesTimeLine, Data
   {

      @Structure
      Module module;

      @This
      Data state;

      public void addNote(String note )
      {
         if( getLastNote() == null || !note.equals( getLastNote().note().get() ))
            addedNote( null, note );
      }

      public void addedNote( DomainEvent event, String note )
      {

         ValueBuilder<NoteValue> noteBuilder = module.valueBuilderFactory().newValueBuilder( NoteValue.class );
         noteBuilder.prototype().note().set( note );
         noteBuilder.prototype().createdOn().set( event.on().get() );
         noteBuilder.prototype().createdBy().set( EntityReference.getEntityReference( RoleMap.role( Creator.class ) ) );

         List<NoteValue> newNotesList = state.notes().get();
         newNotesList.add( noteBuilder.newInstance() );
         state.notes().set( newNotesList );
      }

      public NoteValue getLastNote()
      {
         return Iterables.last( state.notes().get() );
      }
   }
}
