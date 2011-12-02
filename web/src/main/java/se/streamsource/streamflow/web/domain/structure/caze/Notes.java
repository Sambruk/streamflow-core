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

package se.streamsource.streamflow.web.domain.structure.caze;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.note.NoteValue;
import se.streamsource.streamflow.web.domain.structure.note.NotesTimeLine;

import java.util.List;

/**
 * Maintain a note. A note is a longer multi-line string.
 */

@Mixins(Notes.Mixin.class)
public interface Notes
{
   void addNote( String newNote );

   NoteValue getLastNote();

   void createNotes();

   List<NoteValue> getAllNotes();

   interface Data
   {
      @Optional
      Association<NotesTimeLine> notes();

      void createdNotes( @Optional DomainEvent event );

   }

   abstract class Mixin
      implements Notes, Data
   {
      @Structure
      Module module;

      @Service
      IdentityGenerator identityGenerator;

      @This
      Data state;

      public void addNote( String newNote )
      {
         state.notes().get().addNote( newNote );
      }


      public NoteValue getLastNote()
      {
         return state.notes().get().getLastNote();
      }

      public void createNotes()
      {
         createdNotes( null );
      }

      public void createdNotes( DomainEvent event )
      {
         NotesTimeLine notesEntity = module.unitOfWorkFactory().currentUnitOfWork().newEntity( NotesTimeLine.class, identityGenerator.generate( Identity.class ) );
         state.notes().set( notesEntity );
      }

      public List<NoteValue> getAllNotes()
      {
         return ((NotesTimeLine.Data)state.notes().get()).notes().get();
      }
   }
}