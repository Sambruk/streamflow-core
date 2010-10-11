/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ContextValue;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.resource.caze.CaseGeneralDTO;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

import java.util.Date;
import java.util.Observable;

/**
 * Model for the general info about a case.
 */
public class CaseGeneralModel extends Observable implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   private CommandQueryClient client;

   CaseGeneralDTO general;

   private ContextValue contextValue;

   public CaseGeneralModel( @Uses CommandQueryClient client )
   {
      this.client = client;
   }

   public CaseGeneralDTO getGeneral()
   {
      if (general == null)
         vbf.newValue( CaseGeneralDTO.class );

      return general;
   }

   public void changeDescription( String newDescription )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf
               .newValueBuilder( StringValue.class );
         builder.prototype().string().set( newDescription );
         client.postCommand( "changedescription", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException(
               CaseResources.could_not_change_description, e );
      }
   }

   public void changeNote( String newNote )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf
               .newValueBuilder( StringValue.class );
         builder.prototype().string().set( newNote );
         client.postCommand( "changenote", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( CaseResources.could_not_change_note, e );
      }
   }

   public void changeDueOn( Date newDueOn )
   {
      try
      {
         ValueBuilder<DateDTO> builder = vbf.newValueBuilder( DateDTO.class );
         builder.prototype().date().set( newDueOn );
         client.putCommand( "changedueon", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( CaseResources.could_not_change_due_on,
               e );
      }
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
      try
      {
         contextValue = client.query( "", ContextValue.class );

         general = (CaseGeneralDTO) contextValue.index().get().buildWith().prototype();

         setChanged();
         notifyObservers();

      } catch (Exception e)
      {
         throw new OperationException( CaseResources.could_not_refresh, e );
      }
   }

   public void caseType( EntityReference selected )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf
               .newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( selected );
         client.postCommand( "casetype", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException(
               WorkspaceResources.could_not_perform_operation, e );
      }
   }

   public void changeCaseType( LinkValue selected )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf
               .newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( EntityReference.parseEntityReference( selected.id().get() ) );
         client.postCommand( "casetype", builder.newInstance() );

      } catch (ResourceException e)
      {
         throw new OperationException(
               WorkspaceResources.could_not_perform_operation, e );
      }
   }

   public Actions actions()
   {
      try
      {
         return client.query( "actions", Actions.class );
      } catch (ResourceException e)
      {
         throw new OperationException(
               WorkspaceResources.could_not_perform_operation, e );
      }
   }

   public boolean getCommandEnabled( String commandName )
   {
      for (String command : contextValue.commands().get())
      {
         if (command.equals( commandName )) return true;
      }
      return false;
   }

   public CaseStates getCaseStatus()
   {
      return general.status().get();
   }
}