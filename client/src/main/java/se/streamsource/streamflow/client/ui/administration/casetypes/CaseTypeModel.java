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
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsModel;
import se.streamsource.streamflow.client.ui.administration.form.SelectedFormsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsModel;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * JAVADOC
 */
public class CaseTypeModel
      implements EventListener
{
   private
   @Structure
   ValueBuilderFactory vbf;

   private
   @Uses
   CommandQueryClient client;

   private
   @Uses
   SelectedLabelsModel selectedLabelsModel;

   private
   @Uses
   FormsModel formsModel;

   private
   @Uses
   ResolutionsModel resolutionsModel;

   private
   @Uses
   SelectedResolutionsModel selectedResolutionsModel;

   private
   @Uses
   SelectedFormsModel selectedFormsModel;

   public void changeDescription( String newName )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newName );

      try
      {
         client.putCommand( "changedescription", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_rename_project, e );
      }
   }

   public SelectedLabelsModel getSelectedLabelsModel()
   {
      return selectedLabelsModel;
   }

   public ResolutionsModel getResolutionsModel()
   {
      return resolutionsModel;
   }

   public SelectedResolutionsModel getSelectedResolutionsModel()
   {
      return selectedResolutionsModel;
   }

   public FormsModel getFormsModel()
   {
      return formsModel;
   }

   public SelectedFormsModel getSelectedFormsModel()
   {
      return selectedFormsModel;
   }

   public void notifyEvent( DomainEvent event )
   {
      selectedLabelsModel.notifyEvent( event );
      formsModel.notifyEvent( event );
      resolutionsModel.notifyEvent( event );
      selectedResolutionsModel.notifyEvent( event );
      selectedFormsModel.notifyEvent( event );
   }

   public void remove()
   {
      try
      {
         client.delete();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_casetype, e );
      }
   }

   public EventList<LinkValue> usages()
   {
      try
      {
         LinksValue usages = client.query( "usages", LinksValue.class );
         EventList<LinkValue> eventList = new BasicEventList<LinkValue>();
         EventListSynch.synchronize( usages.links().get(), eventList );

         return eventList;
      } catch (ResourceException e)
      {
         throw new OperationException(AdministrationResources.could_not_perform_query, e);
      }
   }

}
