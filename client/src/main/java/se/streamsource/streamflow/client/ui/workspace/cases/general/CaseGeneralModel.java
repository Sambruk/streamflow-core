/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.workspace.cases.general;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.api.workspace.cases.general.CaseGeneralDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.note.CaseNoteModel;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.Date;
import java.util.Observable;

import static org.qi4j.api.util.Iterables.*;
import static se.streamsource.dci.value.link.Links.*;

/**
 * Model for the general info about a case.
 */
public class CaseGeneralModel
      extends Observable
      implements Refreshable
{
   @Structure
   private Module module;

   private CommandQueryClient client;

   private CaseGeneralDTO general;

   private ResourceValue resourceValue;

   public CaseGeneralModel( @Uses CommandQueryClient client )
   {
      this.client = client;
   }

   public CaseGeneralDTO getGeneral()
   {
      if (general == null)
         module.valueBuilderFactory().newValue(CaseGeneralDTO.class);

      return general;
   }

   public void changeDescription( String newDescription )
   {
      if (newDescription.equals(general.description().get()))
         return; // No change

      general.description().set( newDescription );
      Form form = new Form();
      form.set("description", newDescription);
      client.postCommand( "changedescription", form );

   }

   public void changeDueOn( Date newDueOn )
   {
      if (newDueOn.equals(general.dueOn().get()))
         return; // No change

      Form form = new Form();
      form.set("date", DateFunctions.toUtcString(newDueOn));
      client.putCommand("changedueon", form.getWebRepresentation());
      general.dueOn().set( newDueOn );
   }

   public EventList<LinkValue> getPossibleCaseTypes()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         LinksValue listValue = client.query( "possiblecasetypes",
               LinksValue.class );
         list.addAll( listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public void refresh()
   {
      resourceValue = client.query();
      general = (CaseGeneralDTO) resourceValue.index().get().buildWith().prototype();

      setChanged();
      notifyObservers( resourceValue );
   }

   public void changeCaseType( LinkValue selected )
   {
      client.postLink( selected );
   }

   public void removeCaseType( )
   {
      client.postCommand("casetype", module.valueBuilderFactory().newValue(EntityValue.class));
   }

   public boolean getCommandEnabled( String commandName )
   {
      return matchesAny( withRel( commandName ), resourceValue.commands().get() );
   }

   public CaseStates getCaseStatus()
   {
      return general.status().get();
   }

   public CaseLabelsModel newLabelsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(CaseLabelsModel.class).use(client.getSubClient("labels")).newInstance();
   }

   public PossibleFormsModel newPossibleFormsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(PossibleFormsModel.class).use( client.getClient( "../possibleforms/" ) ).newInstance();
   }

   public CaseNoteModel newCaseNoteModel()
   {
      return module.objectBuilderFactory().newObjectBuilder( CaseNoteModel.class ).use(  client.getClient( "../note/" ) ).newInstance();

   }

   public EventComboBoxModel<PriorityValue> getCasePriorities()
   {
      try
      {
         BasicEventList<PriorityValue> list = new BasicEventList<PriorityValue>();

         LinksValue listValue = client.query( "priorities",
               LinksValue.class );
         EventListSynch.synchronize( listValue.links().get(), list );

         return new EventComboBoxModel<PriorityValue>( list );
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public void changePriority( String id )
   {
      Form form = new Form( );
      form.set( "id", "-1".equals( id ) ? "" : id );

      client.postCommand( "changepriority", form );
   }
}