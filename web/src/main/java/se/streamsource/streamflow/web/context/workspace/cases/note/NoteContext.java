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
package se.streamsource.streamflow.web.context.workspace.cases.note;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.general.NoteDTO;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Notes;
import se.streamsource.streamflow.web.domain.structure.note.NoteValue;

import java.util.Date;

import static se.streamsource.streamflow.api.workspace.cases.CaseStates.*;

/**
 * The context for a note.
 */
@RequiresPermission( PermissionType.read )
@Mixins(NoteContext.Mixin.class)
public interface NoteContext
   extends IndexContext<NoteDTO>, Context
{

   LinksValue allnotes();

   @RequiresStatus({DRAFT, OPEN})
   @RequiresPermission( PermissionType.write )
   void addnote(NoteDTO newNote );

   abstract class Mixin implements NoteContext
   {
      @Structure
      Module module;

      public NoteDTO index()
      {
         Notes notes = RoleMap.role( Notes.class );
         ValueBuilder<NoteDTO> builder = module.valueBuilderFactory().newValueBuilder( NoteDTO.class );
         builder.prototype().note().set( notes.getLastNote() != null ? notes.getLastNote().note().get() : "" );
         builder.prototype().createdOn().set( notes.getLastNote() != null ? notes.getLastNote().createdOn().get() : new Date() );
         builder.prototype().creator().set( notes.getLastNote() != null ?
               module.unitOfWorkFactory().currentUnitOfWork()
                     .get( Describable.class, notes.getLastNote().createdBy().get().toString() ).getDescription() : "");

         builder.prototype().id().set( "n/a" );
         builder.prototype().href().set( "addnote" );
         return builder.newInstance();
      }

      public LinksValue allnotes()
      {
         Notes notes = RoleMap.role( Notes.class );
         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         // reverse notes list to latest first
         for(NoteValue note : Iterables.reverse( notes.getAllNotes() ) )
         {
            ValueBuilder<NoteDTO> linkBuilder = module.valueBuilderFactory().newValueBuilder( NoteDTO.class );
            linkBuilder.prototype().note().set( note.note().get() );
            linkBuilder.prototype().createdOn().set( note.createdOn().get() );
            linkBuilder.prototype().creator().set( module.unitOfWorkFactory().currentUnitOfWork()
                     .get( Describable.class, note.createdBy().get().toString() ).getDescription() );

            linkBuilder.prototype().id().set( "n/a" );
            linkBuilder.prototype().href().set( "addnote" );

            linksBuilder.addLink( linkBuilder.newInstance() );
         }

         return linksBuilder.newLinks();
      }

      public void addnote( NoteDTO newNote )
      {
         Notes notes = RoleMap.role( Notes.class );
         notes.addNote( newNote.note().get() );
      }
   }
}
