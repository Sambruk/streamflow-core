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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;

import java.util.List;
import java.util.Observable;

/**
 * JAVADOC
 */
public class FieldsModel extends Observable
      implements Refreshable, EventListener, EventVisitor
{
   final Logger logger = LoggerFactory.getLogger( "administration" );

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
            if (listItemValue instanceof PageListItemValue)
            {
               current = (PageListItemValue) listItemValue;
            } else if (listItemValue.entity().get().identity().equals( key ))
            {
               if (current == null)
               {
                  throw new OperationException( AdministrationResources.could_not_find_field, new IllegalArgumentException( key ) );
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
         return obf.newObjectBuilder( PageEditModel.class ).use( client.getSubClient( key ) ).newInstance();
      }
   };


   EventVisitorFilter eventFilter = new EventVisitorFilter( this, "changedDescription", "movedField", "movedPage", "removedField", "createdField", "removedPage", "createdPage" );

   private BasicEventList<ListItemValue> fieldsList = new BasicEventList<ListItemValue>();

   public void refresh()
   {
      try
      {
         List<ListItemValue> list = ((ListValue) client.query( "pagessummary", ListValue.class ).buildWith().prototype()).items().get();
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
         subClient.postCommand( "add", builder.newInstance() );
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
         ListItemValue origin = getListItemValueById( field.identity() );
         ListItemValue target = null;

         int index = fieldsList.indexOf( origin );

         if ("up".equals( direction ))
         {
            target = fieldsList.get( index - 1 );
            if (!(target instanceof PageListItemValue))
            {
               fieldsList.set( index - 1, origin );
               fieldsList.set( index, target );
            }
         } else
         {
            if (index < fieldsList.size() - 1)
            {
               target = fieldsList.get( index + 1 );
               if (!(target instanceof PageListItemValue))
               {
                  fieldsList.set( index + 1, origin );
                  fieldsList.set( index, target );
               }
            }
         }
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
      ListItemValue updatedListItem = getListItemValue( event );
      String eventName = event.name().get();
      if (updatedListItem != null)
      {
         int index = fieldsList.indexOf( updatedListItem );

         if ("changedDescription".equals( eventName ))
         {
            // only update element
            String description = EventParameters.getParameter( event, "param1" );
            updatedListItem.description().set( description );
            setChanged();
            notifyObservers( updatedListItem );

         } else if ("movedField".equals( eventName ))
         {
            setChanged();
            notifyObservers( getListItemValueById( EventParameters.getParameter( event, "param1" ) ) );
         } else if ("removedField".equals( eventName ))
         {
            ListItemValue oldSelection = getListItemValueById( EventParameters.getParameter( event, "param1" ) );
            ListItemValue newSelection = fieldsList.get( fieldsList.indexOf( oldSelection ) - 1 );
            fieldsList.remove( oldSelection );
            setChanged();
            notifyObservers( newSelection );
         } else if ("createdField".equals( eventName ))
         {
            refresh();
            setChanged();
            notifyObservers( getListItemValueById( EventParameters.getParameter( event, "param1" ) ) );
         }
      } else
      {

         if ("movedPage".equals( eventName ))
         {
            logger.info( "Refresh field list" );
            String id = EventParameters.getParameter( event, "param1" );
            refresh();
            this.setChanged();
            this.notifyObservers( getListItemValueById( id ) );
         } else if ("removedPage".equals( eventName ))
         {
            ListItemValue oldSelection = getListItemValueById( EventParameters.getParameter( event, "param1" ) );
            int newIndex = findPrecedingPageIndex( fieldsList.indexOf( oldSelection ) );
            ListItemValue newSelection = fieldsList.get( newIndex == -1 ? 1 : newIndex );
            refresh();
            setChanged();
            notifyObservers( newSelection );
         } else if ("createdPage".equals( eventName ))
         {
            refresh();
            setChanged();
            notifyObservers( fieldsList.get( fieldsList.size() - 1 ) );
         }
      }
      return false;
   }

   private ListItemValue getListItemValue( DomainEvent event )
   {
      if (fieldsList == null)
         return null;

      for (ListItemValue listItemValue : fieldsList)
      {
         if (listItemValue.entity().get().identity().equals( event.entity().get() ))
         {
            return listItemValue;
         }
      }
      return null;
   }

   private ListItemValue getListItemValueById( String id )
   {
      if (fieldsList == null)
         return null;

      for (ListItemValue listItemValue : fieldsList)
      {
         if (listItemValue.entity().get().identity().equals( id ))
         {
            return listItemValue;
         }
      }
      return null;
   }

   private int findPrecedingPageIndex( int index )
   {
      int nearestPrecedingIndex = -1;

      for (ListItemValue listItemValue : fieldsList)
      {
         int tmpIndex = fieldsList.indexOf( listItemValue );
         if (listItemValue instanceof PageListItemValue && tmpIndex < index )
         {
            nearestPrecedingIndex = tmpIndex;
         }
      }
      return nearestPrecedingIndex;
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