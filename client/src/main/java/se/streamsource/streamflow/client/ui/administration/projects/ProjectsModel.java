/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration.projects;

import ca.odell.glazedlists.BasicEventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.resource.organizations.projects.ProjectClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.tasktypes.SelectedTaskTypesModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * List of projects in a OU
 */
public class ProjectsModel
      implements Refreshable
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   OrganizationalUnitAdministrationModel organizationModel;

   @Uses
   CommandQueryClient client;

   BasicEventList<ListItemValue> eventList = new BasicEventList<ListItemValue>();

   WeakModelMap<String, ProjectModel> projectModels = new WeakModelMap<String, ProjectModel>()
   {
      protected ProjectModel newModel( String key )
      {
         CommandQueryClient projectClient = client.getSubClient( key );
         SelectedLabelsModel selectedLabelsModel = obf.newObjectBuilder( SelectedLabelsModel.class ).use( projectClient.getSubClient( "labels" ) ).newInstance();
         SelectedTaskTypesModel selectedTaskTypesModel = obf.newObjectBuilder( SelectedTaskTypesModel.class ).use( projectClient.getSubClient( "tasktypes" ) ).newInstance();

         return obf.newObjectBuilder( ProjectModel.class ).use( project( key ).members(),
               selectedLabelsModel,
               selectedTaskTypesModel,
               project( key ).forms(),
               organizationModel ).newInstance();
      }
   };

   public BasicEventList<ListItemValue> getProjectList()
   {
      return eventList;
   }

   public void refresh()
   {
      try
      {
         // Get Project list
         eventList.clear();
         eventList.addAll( client.query( "projects", ListValue.class ).items().get() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void removeProject( String id )
   {
      try
      {
         project( id ).deleteCommand();
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
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( projectName );
         client.postCommand( "createProject", builder.newInstance() );
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

   public void changeDescription( int selectedIndex, String newName )
   {
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( newName );

      try
      {
         project( eventList.get( selectedIndex ).entity().get().identity() ).changeDescription( builder.newInstance() );
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
      for (ProjectModel projectModel : projectModels)
      {
         projectModel.notifyEvent( event );
      }
   }

   public ProjectClientResource project( String id )
   {
      return client.getSubResource( id, ProjectClientResource.class );
   }

   public ProjectModel getProjectModel( String id )
   {
      return projectModels.get( id );
   }
}