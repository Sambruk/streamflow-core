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

import org.jdesktop.application.Application;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.organizations.organizationalunits.OrganizationalUnitClientResource;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * JAVADOC
 */
public class OrganizationalUnitAdministrationModel
      implements EventListener
{
   @Structure
   ValueBuilderFactory vbf;

   @Service
   Application application;

   private GroupsModel groupsModel;
   private ProjectsModel projectsModel;
   private AdministratorsModel administratorsModel;
   private OrganizationalUnitClientResource ou;

   public OrganizationalUnitAdministrationModel( @Structure ObjectBuilderFactory obf, @Uses OrganizationalUnitClientResource ou ) throws ResourceException
   {
      this.ou = ou;
      groupsModel = obf.newObjectBuilder( GroupsModel.class ).use( ou.groups() ).newInstance();
      projectsModel = obf.newObjectBuilder( ProjectsModel.class ).use( ou.getNext(), ou.projects(), this ).newInstance();
      administratorsModel = obf.newObjectBuilder( AdministratorsModel.class ).use( ou.administrators() ).newInstance();
   }

   public OrganizationalUnitClientResource getOrganizationalUnit()
   {
      return ou;
   }

   public GroupsModel groupsModel()
   {
      return groupsModel;
   }

   public ProjectsModel projectsModel()
   {
      return projectsModel;
   }

   public AdministratorsModel administratorsModel()
   {
      return administratorsModel;
   }

   public void changeDescription( String newDescription )
   {
      try
      {
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( newDescription );
         ou.changeDescription( builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_rename_organization, e );
      }
   }

   public void createOrganizationalUnit( String name )
   {
      try
      {
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( name );
         ou.organizationalUnits().createOrganizationalUnit( builder.newInstance() );
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
         ou.organizationalUnits().removeOrganizationalUnit( builder.newInstance() );
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

         ou.move( builder.newInstance() );
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

         ou.merge( builder.newInstance() );
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
