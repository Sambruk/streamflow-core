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

package se.streamsource.streamflow.client.ui.workspace.cases.general;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.api.workspace.cases.general.CaseGeneralDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.PossibleFormsModel;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.Date;
import java.util.Observable;

import static org.qi4j.api.util.Iterables.matchesAny;
import static se.streamsource.dci.value.link.Links.withRel;

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
      ValueBuilder<StringValue> builder = module.valueBuilderFactory()
            .newValueBuilder(StringValue.class);
      builder.prototype().string().set( newDescription );
      client.postCommand( "changedescription", builder.newInstance() );

   }

   public void changeNote( String newNote )
   {
      if (newNote.equals(general.note().get()))
         return; // No change

      ValueBuilder<StringValue> builder = module.valueBuilderFactory()
            .newValueBuilder(StringValue.class);
      builder.prototype().string().set( newNote );
      client.postCommand( "changenote", builder.newInstance() );
      general.note().set( newNote );
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
      resourceValue = client.queryResource();
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
}