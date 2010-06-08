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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

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
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.PageListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;

import java.util.List;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class FieldsModel
      implements Refreshable, EventListener, EventVisitor
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   WeakModelMap<String, FieldValueEditModel> fieldModels = new WeakModelMap<String, FieldValueEditModel>()
   {
      protected FieldValueEditModel newModel( String key )
      {
         PageListItemValue current = null;
         CommandQueryClient fieldClient = null;
         for (ListItemValue listItemValue : fieldsList)
         {
            if ( listItemValue instanceof PageListItemValue)
            {
               current = (PageListItemValue) listItemValue;
            } else if ( listItemValue.entity().get().identity().equals( key ))
            {
               if ( current == null)
               {
                  throw new OperationException( AdministrationResources.could_not_find_field, new IllegalArgumentException( key ));
               }
               fieldClient = client.getSubClient( current.entity().get().identity() ).getSubClient( "fields" ).getSubClient( key );
            }
         }

         return obf.newObjectBuilder( FieldValueEditModel.class )
               .use( fieldClient ).newInstance();
      }
   };

   WeakModelMap<String, PageEditModel> pageModels = new WeakModelMap<String, PageEditModel>()
   {
      protected PageEditModel newModel( String key )
      {
         return obf.newObjectBuilder( PageEditModel.class ).use( client.getSubClient( key )).newInstance();
      }
   };


   EventVisitorFilter eventFilter = new EventVisitorFilter( this, "changedDescription", "movedField", "movedPage", "removedField", "createdField", "removedPage", "createdPage" );

   private BasicEventList<ListItemValue> fieldsList = new BasicEventList<ListItemValue>();

   public void refresh()
   {
      try
      {
         List<ListItemValue> list = ((ListValue)client.query( "pagessummary", ListValue.class ).buildWith().prototype()).items().get();
         EventListSynch.synchronize( list, fieldsList );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_form_pages_and_fields, e );
      }
   }

   public EventList<ListItemValue> getPagesAndFieldsList()
   {
      return fieldsList;
   }

   public void addField( EntityReference page, String name, FieldTypes fieldType )
   {

      CommandQueryClient subClient = client.getSubClient( page.identity() ).getSubClient( "fields" );

      ValueBuilder<CreateFieldDTO> builder = vbf.newValueBuilder( CreateFieldDTO.class );
      builder.prototype().name().set( name );
      builder.prototype().fieldType().set( fieldType );

      try
      {
         subClient.putCommand( "add", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.description_cannot_be_more_than_50, e );
      }
   }

   public void addPage( String pageName )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( pageName );

      try
      {
         client.postCommand( "add", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.description_cannot_be_more_than_50, e );
      }
   }

   public void removeField( EntityReference field )
   {
      FieldValueEditModel model = getFieldModel( field.identity() );
      try
      {
         model.remove();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_field, e );
      }
   }

   public void removePage( EntityReference page )
   {
      PageEditModel model = getPageModel( page.identity() );

      try
      {
         model.remove();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_field, e );
      }
   }

   public void moveField( EntityReference field, String direction )
   {
      FieldValueEditModel model = getFieldModel( field.identity() );
      try
      {
         model.move( direction );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_move_field, e );
      }
   }

   public void movePage( EntityReference page, String direction )
   {
      PageEditModel model = getPageModel( page.identity() );
      try
      {
         model.move( direction );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_move_page, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
      for (PageEditModel pageModel : pageModels)
      {
         pageModel.notifyEvent( event );
      }
      for (FieldValueEditModel fieldModel : fieldModels)
      {
         fieldModel.notifyEvent( event );
      }
   }

   public boolean visit( DomainEvent event )
   {
      String eventName = event.name().get();
      int index = 0;
      for (ListItemValue value : fieldsList)
      {
         if (event.entity().get().equals( value.entity().get().identity() ))
         {
            if ( eventName.equals( "changedDescription" ) )
            {
               // only update element
               String description = EventParameters.getParameter( event, "param1" );
               fieldsList.get( index ).description().set( description );
            }
         }
         index++;
      }
      if ( !eventName.equals( "changedDescription" ))
      {
         Logger.getLogger( "adminitration" ).info( "Refresh field list" );
         refresh();
      }
      return false;
   }

   public FieldValueEditModel getFieldModel( String id )
   {
      return fieldModels.get( id );
   }

   public PageEditModel getPageModel( String id )
   {
      return pageModels.get( id );
   }
   }