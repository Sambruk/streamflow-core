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

package se.streamsource.streamflow.client.ui.administration;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

/**
 * JAVADOC
 */
public class OrganizationalUnitAdministrationModel
      implements EventListener
{

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   private GroupsModel groupsModel;
   private ProjectsModel projectsModel;
   private SelectedLabelsModel selectedLabelsModel;
   private AdministratorsModel administratorsModel;
   private CommandQueryClient client;

   public OrganizationalUnitAdministrationModel( @Structure ObjectBuilderFactory obf, @Uses CommandQueryClient client ) throws ResourceException
   {
      this.client = client;

      groupsModel = obf.newObjectBuilder( GroupsModel.class ).use( client.getSubClient( "groups" )).newInstance();
      projectsModel = obf.newObjectBuilder( ProjectsModel.class ).use( client.getSubClient( "projects" ), this).newInstance();
      selectedLabelsModel = obf.newObjectBuilder( SelectedLabelsModel.class ).use( client.getSubClient( "selectedlabels" ), this).newInstance();
      administratorsModel = obf.newObjectBuilder( AdministratorsModel.class ).use( client.getSubClient( "administrators" )).newInstance();
   }

   public CommandQueryClient getOrganizationalUnit()
   {
      return client;
   }

   public GroupsModel groupsModel()
   {
      return groupsModel;
   }

   public ProjectsModel projectsModel()
   {
      return projectsModel;
   }

   public SelectedLabelsModel selectedLabelsModel()
   {
      return selectedLabelsModel;
   }

   public AdministratorsModel administratorsModel()
   {
      return administratorsModel;
   }

   public void changeDescription( String newDescription )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( newDescription );
         client.putCommand( "changedescription", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_rename_organization, e );
      }
   }

   public void createOrganizationalUnit( String name )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( name );
         client.getSubClient("organizationalunits" ).postCommand( "createorganizationalunit", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_create_new_organization, e );
      }
   }

   public void removeOrganizationalUnit( EntityReference id )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( id );
         client.getSubClient("organizationalunits" ).getSubClient( id.identity() ).delete();
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_remove_organisation_with_open_projects, e );

         } else
         {
            throw new OperationException( AdministrationResources.could_not_remove_organization, e );
         }

      }
   }

   public void moveOrganizationalUnit( EntityReference toID )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         EntityReferenceDTO dto = builder.prototype();
         dto.entity().set( toID );

         client.postCommand( "move", builder.newInstance() );
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_move_organisation_with_conflicts, e );

         } else
         {
            throw new OperationException( AdministrationResources.could_not_move_organization, e );
         }
      }

   }

   public void mergeOrganizationalUnit( EntityReference toID )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         EntityReferenceDTO dto = builder.prototype();
         dto.entity().set( toID );

         client.postCommand( "merge",  builder.newInstance() );
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_merge_organisation_with_conflicts, e );

         } else
         {
            throw new OperationException( AdministrationResources.could_not_merge_organization, e );
         }
      }

   }

   public void notifyEvent( DomainEvent event )
   {
      groupsModel.notifyEvent( event );
      projectsModel.notifyEvent( event );
      administratorsModel.notifyEvent( event );
   }
}
