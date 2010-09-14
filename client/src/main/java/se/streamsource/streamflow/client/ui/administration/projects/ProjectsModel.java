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

package se.streamsource.streamflow.client.ui.administration.projects;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.LinkValueListModel;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.label.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;

/**
 * List of projects in a OU
 */
public class ProjectsModel
   extends LinkValueListModel
      implements EventListener, Refreshable
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   OrganizationalUnitAdministrationModel organizationModel;

   @Uses
   CommandQueryClient client;

   WeakModelMap<String, ProjectModel> projectModels = new WeakModelMap<String, ProjectModel>()
   {
      protected ProjectModel newModel( String key )
      {
         CommandQueryClient projectClient = client.getSubClient( key );
         LabelsModel labelsModel = obf.newObjectBuilder( LabelsModel.class ).use( projectClient.getSubClient( "labels" ) ).newInstance();
         SelectedLabelsModel selectedLabelsModel = obf.newObjectBuilder( SelectedLabelsModel.class ).use( projectClient.getSubClient( "selectedlabels" ) ).newInstance();
         FormsModel formsModel = obf.newObjectBuilder( FormsModel.class ).use( projectClient.getSubClient( "forms" ) ).newInstance();
         CaseTypesModel caseTypesModel = obf.newObjectBuilder( CaseTypesModel.class ).use( projectClient.getSubClient( "casetypes" ) ).newInstance();
         SelectedCaseTypesModel selectedCaseTypesModel = obf.newObjectBuilder( SelectedCaseTypesModel.class ).use( projectClient.getSubClient( "selectedcasetypes" ) ).newInstance();
         ProjectMembersModel projectMembersModel = obf.newObjectBuilder(ProjectMembersModel.class).use( projectClient.getSubClient( "members" ) ).newInstance();


         return obf.newObjectBuilder( ProjectModel.class ).use(
               projectMembersModel,
               formsModel,
               caseTypesModel,
               labelsModel,
               selectedLabelsModel,
               selectedCaseTypesModel,
               organizationModel ).newInstance();
      }
   };

   public EventList<LinkValue> getProjectList()
   {
      return linkValues;
   }

   public void refresh()
   {
      try
      {
         // Get Project list
         LinksValue projectsList = client.query( "index", LinksValue.class );
         EventListSynch.synchronize( projectsList.links().get(), linkValues );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void removeProject( String id )
   {
      try
      {
         client.getSubClient( id ).delete();
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_project, e );
      }
   }

   public void newProject( String projectName )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( projectName );
         client.postCommand( "createproject", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_create_project_name_already_exists, e );
         }
         throw new OperationException( AdministrationResources.could_not_create_project, e );
      }
   }

   public void changeDescription( LinkValue link, String newName )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newName );

      try
      {
         client.getSubClient( link.id().get() ).putCommand( "changedescription", builder.newInstance() );
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_rename_project_name_already_exists, e );
         }
         throw new OperationException( AdministrationResources.could_not_rename_project, e );
      }
      refresh();
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
      for (ProjectModel projectModel : projectModels)
      {
         projectModel.notifyEvent( event );
      }
   }

   public ProjectModel getProjectModel( String id )
   {
      return projectModels.get( id );
   }
}