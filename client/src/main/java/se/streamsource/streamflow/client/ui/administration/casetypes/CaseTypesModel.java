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

package se.streamsource.streamflow.client.ui.administration.casetypes;

import ca.odell.glazedlists.BasicEventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.form.SelectedFormsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * List of casetypes in an Organization
 */
public class CaseTypesModel
      implements Refreshable, EventListener
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CommandQueryClient client;

   BasicEventList<LinkValue> eventList = new BasicEventList<LinkValue>();

   WeakModelMap<String, CaseTypeModel> caseTypeModels = new WeakModelMap<String, CaseTypeModel>()
   {
      protected CaseTypeModel newModel( String key )
      {
         CommandQueryClient caseTypeClient = client.getSubClient( key );
         SelectedLabelsModel selectedLabelsModel = obf.newObjectBuilder( SelectedLabelsModel.class ).use( caseTypeClient.getSubClient( "selectedlabels" ) ).newInstance();
         ResolutionsModel resolutionsModel = obf.newObjectBuilder( ResolutionsModel.class ).use( caseTypeClient.getSubClient( "resolutions" ) ).newInstance();
         SelectedResolutionsModel selectedResolutionsModel = obf.newObjectBuilder( SelectedResolutionsModel.class ).use( caseTypeClient.getSubClient( "selectedresolutions" ) ).newInstance();
         FormsModel formsModel = obf.newObjectBuilder( FormsModel.class ).use( caseTypeClient.getSubClient( "forms" ) ).newInstance();
         SelectedFormsModel selectedFormsModel = obf.newObjectBuilder( SelectedFormsModel.class ).use( caseTypeClient.getSubClient( "selectedforms" ) ).newInstance();

         return obf.newObjectBuilder( CaseTypeModel.class ).use(
               selectedLabelsModel,
               formsModel,
               selectedFormsModel,
               resolutionsModel,
               selectedResolutionsModel, 
               caseTypeClient ).newInstance();
      }
   };

   public BasicEventList<LinkValue> getCaseTypeList()
   {
      return eventList;
   }

   public void refresh()
   {
      try
      {
         // Get CaseType list
         EventListSynch.synchronize( client.query( "index", LinksValue.class ).links().get(), eventList );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void removeCaseType( String id )
   {
      getCaseTypeModel( id ).remove();
   }

   public void newCaseType( String caseTypeName )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( caseTypeName );
         client.postCommand( "createcasetype", builder.newInstance() );
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.description_cannot_be_more_than_50, e );
         }
         throw new OperationException( AdministrationResources.could_not_create_project, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      for (CaseTypeModel caseTypeModel : caseTypeModels)
      {
         caseTypeModel.notifyEvent( event );
      }
   }

   public CaseTypeModel getCaseTypeModel( String id )
   {
      return caseTypeModels.get( id );
   }
}