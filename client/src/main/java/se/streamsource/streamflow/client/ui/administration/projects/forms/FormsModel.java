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

package se.streamsource.streamflow.client.ui.administration.projects.forms;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.resource.organizations.projects.forms.ProjectFormDefinitionClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;

import javax.swing.AbstractListModel;
import java.util.List;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class FormsModel
      extends AbstractListModel
      implements Refreshable, EventListener, EventHandler

{
   @Uses
   CommandQueryClient client;

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private List<ListItemValue> formsList;

   private EventHandlerFilter eventFilter;

   public FormsModel()
   {
      eventFilter = new EventHandlerFilter( this, "createdForm", "removedForm", "changedDescription" );
   }

   WeakModelMap<String, FormModel> formModels = new WeakModelMap<String, FormModel>()
   {
      protected FormModel newModel( String key )
      {
         try
         {
            ListValue value = client.query( "forms", ListValue.class );
            int index = 0;
            for (ListItemValue listItemValue : value.items().get())
            {
               if (listItemValue.entity().get().identity().equals( key ))
               {
                  break;
               }
               index++;
            }

            ProjectFormDefinitionClientResource resource = client.getSubResource( "" + index, ProjectFormDefinitionClientResource.class );
            return obf.newObjectBuilder( FormModel.class ).use( resource ).newInstance();
         } catch (ResourceException e)
         {
            throw new OperationException( AdministrationResources.could_not_get_form, e );
         }
      }
   };


   public int getSize()
   {
      return formsList == null ? 0 : formsList.size();
   }

   public Object getElementAt( int index )
   {
      return formsList.get( index );
   }

   public void refresh()
   {
      try
      {
         formsList = client.query( "forms", ListValue.class ).items().get();
         fireContentsChanged( this, 0, getSize() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_members, e );
      }
   }

   public void createForm( String formName )
   {
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( formName );
      try
      {
         client.postCommand( "createForm", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_form_definition, e );
      }
   }

   public void addForm( EntityReference form )
   {
      ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
      builder.prototype().entity().set( form );
      try
      {
         client.postCommand( "addForm", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_form_definition, e );
      }
   }

   public void removeForm( EntityReference form )
   {
      ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
      builder.prototype().entity().set( form );
      try
      {
         client.postCommand( "removeForm", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_form_definition, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.handleEvent( event );
      for (FormModel model : formModels)
      {
         model.notifyEvent( event );
      }
   }

   public boolean handleEvent( DomainEvent event )
   {
      Logger.getLogger( "administration" ).info( "Refresh project form definitions" );
      refresh();
      return false;
   }

   public FormModel getFormModel( String identity )
   {
      return formModels.get( identity );
   }
}