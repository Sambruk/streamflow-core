/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.label.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.policy.AdministratorsModel;
import se.streamsource.streamflow.client.ui.administration.roles.RolesModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * JAVADOC
 */
public class OrganizationAdministrationModel
      implements EventListener
{

   @Structure
   ValueBuilderFactory vbf;

   private AdministratorsModel administratorsModel;
   private RolesModel rolesModel;
   private LabelsModel labelsModel;
   private SelectedLabelsModel selectedLabelsModel;
   private FormsModel formsModel;
   private CaseTypesModel caseTypesModel;
   private CommandQueryClient client;

   public OrganizationAdministrationModel( @Structure ObjectBuilderFactory obf, @Uses CommandQueryClient client)
         throws ResourceException
   {
      this.client = client;
      
      rolesModel = obf.newObjectBuilder( RolesModel.class ).use( client.getSubClient( "roles" )).newInstance();
      labelsModel = obf.newObjectBuilder( LabelsModel.class ).use( client.getSubClient( "labels")).newInstance();
      selectedLabelsModel = obf.newObjectBuilder( SelectedLabelsModel.class ).use( client.getSubClient( "selectedlabels")).newInstance();
      formsModel = obf.newObjectBuilder( FormsModel.class ).use( client.getSubClient( "forms")).newInstance();
      caseTypesModel = obf.newObjectBuilder( CaseTypesModel.class ).use( client.getSubClient( "casetypes" )).newInstance();
      administratorsModel = obf.newObjectBuilder( AdministratorsModel.class ).use( client.getSubClient( "administrators" )).newInstance();
   }

   public RolesModel rolesModel()
   {
      return rolesModel;
   }

   public LabelsModel labelsModel()
   {
      return labelsModel;
   }

   public SelectedLabelsModel selectedLabelsModel()
   {
      return selectedLabelsModel;
   }

   public FormsModel formsModel()
   {
      return formsModel;
   }

   public CaseTypesModel caseTypesModel()
   {
      return caseTypesModel;
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
         client.getSubClient("organizationalunits").postCommand( "createorganizationalunit", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_create_new_organization, e );
      }
   }

   public void removeOrganizationalUnit( EntityReference id )
   {
      try
      {
         client.getSubClient("organizationalunits").getSubClient( id.identity() ).delete();
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

   public void notifyEvent( DomainEvent event )
   {
      rolesModel.notifyEvent( event );
      labelsModel.notifyEvent( event );
      formsModel.notifyEvent( event );
      administratorsModel.notifyEvent( event );
      caseTypesModel.notifyEvent( event );
   }
}
