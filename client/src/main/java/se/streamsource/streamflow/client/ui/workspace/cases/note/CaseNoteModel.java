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
package se.streamsource.streamflow.client.ui.workspace.cases.note;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.swing.EventListModel;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.Links;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.general.NoteDTO;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.Observable;

import static org.qi4j.api.util.Iterables.*;

/**
 * A model representing case notes.
 */
public class CaseNoteModel extends Observable
      implements Refreshable
{
   @Structure
   Module module;

   private CommandQueryClient client;


   private int selectedNoteIndex = 0;
   private NoteDTO note;
   private ResourceValue resource;

   public CaseNoteModel( @Uses CommandQueryClient client )
   {
      this.client = client;
   }

   public void refresh()
   {
      resource = client.query();
      note = (NoteDTO) resource.index().get().buildWith().prototype();
      setChanged();
      notifyObservers( resource );
   }

   public void addNote( String newNote )
   {
      if (newNote.equals(note.note().get()))
         return; // No change
      else
         note.note().set( newNote );

      ValueBuilder<NoteDTO> builder = module.valueBuilderFactory()
            .newValueBuilder( NoteDTO.class ).withPrototype( note );
      client.postCommand( "addnote", builder.newInstance() );
   }

   public NoteDTO getNote()
   {
      return note;
   }

   public EventListModel<LinkValue> getNotes()
   {
      BasicEventList<LinkValue> notes = new BasicEventList<LinkValue>(  );
      notes.addAll( client.query( "allnotes", LinksValue.class ).links().get() );
      return new EventListModel<LinkValue>( notes );
   }


   public boolean checkNoteEnabled()
   {
      return matchesAny( Links.withRel( "addnote" ), resource.commands().get() );
   }

   public void setSelectedNoteIndex(int index)
   {
      selectedNoteIndex = index;
   }
   
   public int getSelectedNoteIndex()
   {
      return selectedNoteIndex;
   }
}
